/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.internal.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.Layer;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.VectorLayer;
import org.geomajas.service.ConfigurationService;
import org.geomajas.service.LegendGraphicService;
import org.geomajas.service.StyleConverterService;
import org.geomajas.service.legend.LegendGraphicMetadata;
import org.geomajas.sld.UserStyleInfo;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.LiteShape2;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.renderer.lite.MetaBufferEstimator;
import org.geotools.renderer.lite.StyledShapePainter;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style2D;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.style.RasterSymbolizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.io.WKTReader;

/**
 * Default implementation of {@link LegendGraphicService}.
 * 
 * @author Jan De Moerloose
 * 
 */
@Component
public class LegendGraphicServiceImpl implements LegendGraphicService {

	@Autowired
	private StyleConverterService styleConverterService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ApplicationContext applicationContext;

	private SLDStyleFactory styleFactory = new SLDStyleFactory();

	private StyledShapePainter shapePainter = new StyledShapePainter();

	private int defaultWidth = DEFAULT_ICON_SIZE;

	private int defaultHeight = DEFAULT_ICON_SIZE;
	
	private String rasterImagePath = DEFAULT_RASTER_IMAGE_PATH;

	private final Logger log = LoggerFactory.getLogger(LegendGraphicServiceImpl.class);

	public int getDefaultWidth() {
		return defaultWidth;
	}

	public void setDefaultWidth(int defaultWidth) {
		this.defaultWidth = defaultWidth;
	}

	public int getDefaultHeight() {
		return defaultHeight;
	}

	public void setDefaultHeight(int defaultHeight) {
		this.defaultHeight = defaultHeight;
	}
	
	public String getRasterImagePath() {
		return rasterImagePath;
	}
	
	public void setRasterImagePath(String rasterImagePath) {
		this.rasterImagePath = rasterImagePath;
	}

	public RenderedImage getLegendGraphic(LegendGraphicMetadata legendMetadata) throws GeomajasException {
		Style style = null;
		VectorLayer vectorLayer = null;
		Layer<?> layer = configurationService.getLayer(legendMetadata.getLayerId());
		if (layer instanceof VectorLayer) {
			vectorLayer = (VectorLayer) layer;
		}
		UserStyleInfo userStyleInfo = legendMetadata.getUserStyle();
		if (userStyleInfo != null) {
			style = styleConverterService.convert(userStyleInfo);
		} else {
			// try to get style from layer
			String styleName = null;
			if (legendMetadata.getNamedStyle() != null) {
				styleName = legendMetadata.getNamedStyle().getName();
			}
			if (vectorLayer != null) {
				NamedStyleInfo namedStyle;
				if (styleName != null) {
					namedStyle = vectorLayer.getLayerInfo().getNamedStyleInfo(styleName);
					if (namedStyle == null) {
						throw new LayerException(ExceptionCode.STYLE_NOT_FOUND, styleName, legendMetadata.getLayerId());
					}
				} else {
					namedStyle = vectorLayer.getLayerInfo().getNamedStyleInfos().get(0);
				}
				style = styleConverterService.convert(namedStyle.getUserStyle());
			}
		}
		Rule rule = null;
		if (legendMetadata.getRule() != null) {
			rule = styleConverterService.convert(legendMetadata.getRule());
		} else if (style != null) {
			rule = style.featureTypeStyles().get(0).rules().get(0);
		}

		int width = legendMetadata.getWidth() > 0 ? legendMetadata.getWidth() : defaultWidth;
		int height = legendMetadata.getHeight() > 0 ? legendMetadata.getHeight() : defaultHeight;
		if (rule != null) {
			SimpleFeature sampleFeature = createSampleFeature(vectorLayer);

			Symbolizer[] symbolizers = rule.getSymbolizers();

			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			NumberRange<Double> scaleRange = NumberRange.create(0, Double.MAX_VALUE);

			for (Symbolizer symbolizer : symbolizers) {
				if (symbolizer instanceof RasterSymbolizer || symbolizer instanceof TextSymbolizer) {
					// ignore for now
				} else {
					Style2D style2d = styleFactory.createStyle(sampleFeature, symbolizer, scaleRange);
					LiteShape2 shape = getSampleShape(symbolizer, width, height);
					if (style2d != null && shape != null) {
						shapePainter.paint(graphics, shape, style2d, 1.0);
					}
				}
			}
			graphics.dispose();
			return image;
		} else if (layer instanceof RasterLayer) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			try {
				graphics.drawImage(getImage(getRasterImagePath()), 0, 0, width, height, null);
			} catch (IOException e) {
				throw new GeomajasException(e, ExceptionCode.LEGEND_GRAPHIC_RENDERING_PROBLEM,
						legendMetadata.getLayerId());
			}
			graphics.dispose();
			return image;
		} else {
			throw new GeomajasException(ExceptionCode.LAYER_NOT_FOUND, legendMetadata.getLayerId());
		}
	}

	private SimpleFeature createSampleFeature(VectorLayer vectorLayer) {
		SimpleFeatureType type;
		try {
			String geomName = "the_geom";
			if (vectorLayer != null) {
				geomName = vectorLayer.getLayerInfo().getFeatureInfo().getGeometryType().getName();
			}
			type = DataUtilities.createType("Sample", geomName + ":Geometry");
		} catch (SchemaException e) {
			throw new RuntimeException(e);
		}
		return SimpleFeatureBuilder.template(type, null);
	}

	private LiteShape2 getSampleShape(Symbolizer symbolizer, int width, int height) {
		MetaBufferEstimator estimator = new MetaBufferEstimator();
		estimator.visit(symbolizer);
		// add an extra pixel to the margin (odd line widths were not always shown)
		int margin = estimator.getBuffer() + 1;
		// we define a shape in WKT of size 1 x 1 and transform it to a rectangle of (width-margin) x (height-margin),
		// to make sure we capture thick strokes
		MathTransform transform = new AffineTransform2D(width - margin, 0, 0, height - margin, 0.5 * margin,
				0.5 * margin);
		try {
			WKTReader wktReader = new WKTReader();
			if (symbolizer instanceof LineSymbolizer) {
				return new LiteShape2(wktReader.read("LINESTRING (0 0, 0.66 0.33, 0.33 0.66, 1 1)"), transform, null,
						false);
			} else if (symbolizer instanceof PolygonSymbolizer) {
				return new LiteShape2(wktReader.read("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"), transform, null, false);
			} else if (symbolizer instanceof PointSymbolizer) {
				return new LiteShape2(wktReader.read("POINT (0.5 0.5)"), transform, null, false);
			} else {
				return null;
			}
		} catch (Exception e) {
			// should not happen
			log.warn("Could not create sample shapes", e);
			return null;
		}
	}

	private BufferedImage getImage(String href) throws IOException {
		if (href == null || "".equals(href)) {
			return null;
		} else if (href.startsWith("http://")) {
			return ImageIO.read(new URL(href));
		} else {
			String path = href;
			if (href.startsWith("/")) {
				path = href.substring(1);
			}
			InputStream is = null;
			try {
				Resource resource = applicationContext.getResource(path);
				if (resource != null && resource.exists()) {
					is = resource.getInputStream();
				} else {
					// conveniencecheck so clients can use same url client- as
					// serverside
					resource = applicationContext.getResource("images/" + path);
					if (resource != null && resource.exists()) {
						is = resource.getInputStream();
					} else {
						is = ClassLoader.getSystemResourceAsStream(href);
					}
				}
				if (is == null) {
					throw new IOException("Resource missing in classpath : " + path);
				}
				return ImageIO.read(is);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
	}

}
