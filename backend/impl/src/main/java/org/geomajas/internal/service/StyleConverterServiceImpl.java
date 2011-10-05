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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.geomajas.configuration.AttributeInfo;
import org.geomajas.configuration.CircleInfo;
import org.geomajas.configuration.FeatureInfo;
import org.geomajas.configuration.FeatureStyleInfo;
import org.geomajas.configuration.FontStyleInfo;
import org.geomajas.configuration.ImageInfo;
import org.geomajas.configuration.LabelStyleInfo;
import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.configuration.PrimitiveAttributeInfo;
import org.geomajas.configuration.PrimitiveType;
import org.geomajas.configuration.RectInfo;
import org.geomajas.configuration.SymbolInfo;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.internal.service.sld.ResourceAwareSLDParser;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.LayerType;
import org.geomajas.service.FilterService;
import org.geomajas.service.ResourceService;
import org.geomajas.service.StyleConverterService;
import org.geomajas.sld.CssParameterInfo;
import org.geomajas.sld.ExternalGraphicInfo;
import org.geomajas.sld.FeatureTypeStyleInfo;
import org.geomajas.sld.FillInfo;
import org.geomajas.sld.FontInfo;
import org.geomajas.sld.GraphicInfo;
import org.geomajas.sld.GraphicInfo.ChoiceInfo;
import org.geomajas.sld.LineSymbolizerInfo;
import org.geomajas.sld.MarkInfo;
import org.geomajas.sld.NamedLayerInfo;
import org.geomajas.sld.ParameterValueTypeInfo;
import org.geomajas.sld.PointSymbolizerInfo;
import org.geomajas.sld.PolygonSymbolizerInfo;
import org.geomajas.sld.RuleInfo;
import org.geomajas.sld.StrokeInfo;
import org.geomajas.sld.StyledLayerDescriptorInfo;
import org.geomajas.sld.SymbolizerTypeInfo;
import org.geomajas.sld.TextSymbolizerInfo;
import org.geomajas.sld.UserStyleInfo;
import org.geomajas.sld.expression.ExpressionInfo;
import org.geomajas.sld.expression.LiteralTypeInfo;
import org.geomajas.sld.filter.AndInfo;
import org.geomajas.sld.filter.BBOXTypeInfo;
import org.geomajas.sld.filter.BeyondInfo;
import org.geomajas.sld.filter.BinaryComparisonOpTypeInfo;
import org.geomajas.sld.filter.BinaryLogicOpTypeInfo;
import org.geomajas.sld.filter.BinarySpatialOpTypeInfo;
import org.geomajas.sld.filter.ComparisonOpsTypeInfo;
import org.geomajas.sld.filter.ContainsInfo;
import org.geomajas.sld.filter.CrossesInfo;
import org.geomajas.sld.filter.DWithinInfo;
import org.geomajas.sld.filter.DisjointInfo;
import org.geomajas.sld.filter.DistanceBufferTypeInfo;
import org.geomajas.sld.filter.EqualsInfo;
import org.geomajas.sld.filter.FeatureIdTypeInfo;
import org.geomajas.sld.filter.FilterTypeInfo;
import org.geomajas.sld.filter.IntersectsInfo;
import org.geomajas.sld.filter.LogicOpsTypeInfo;
import org.geomajas.sld.filter.OrInfo;
import org.geomajas.sld.filter.OverlapsInfo;
import org.geomajas.sld.filter.PropertyIsBetweenTypeInfo;
import org.geomajas.sld.filter.PropertyIsEqualToInfo;
import org.geomajas.sld.filter.PropertyIsGreaterThanInfo;
import org.geomajas.sld.filter.PropertyIsGreaterThanOrEqualToInfo;
import org.geomajas.sld.filter.PropertyIsLessThanInfo;
import org.geomajas.sld.filter.PropertyIsLessThanOrEqualToInfo;
import org.geomajas.sld.filter.PropertyIsLikeTypeInfo;
import org.geomajas.sld.filter.PropertyIsNotEqualToInfo;
import org.geomajas.sld.filter.PropertyIsNullTypeInfo;
import org.geomajas.sld.filter.SpatialOpsTypeInfo;
import org.geomajas.sld.filter.TouchesInfo;
import org.geomajas.sld.filter.UnaryLogicOpTypeInfo;
import org.geomajas.sld.filter.WithinInfo;
import org.geomajas.sld.geometry.AbstractGeometryCollectionInfo;
import org.geomajas.sld.geometry.AbstractGeometryInfo;
import org.geomajas.sld.geometry.CoordTypeInfo;
import org.geomajas.sld.geometry.CoordinatesTypeInfo;
import org.geomajas.sld.geometry.GeometryMemberInfo;
import org.geomajas.sld.geometry.InnerBoundaryIsInfo;
import org.geomajas.sld.geometry.LineStringTypeInfo;
import org.geomajas.sld.geometry.LinearRingTypeInfo;
import org.geomajas.sld.geometry.MultiGeometryInfo;
import org.geomajas.sld.geometry.MultiLineStringInfo;
import org.geomajas.sld.geometry.MultiPointInfo;
import org.geomajas.sld.geometry.MultiPolygonInfo;
import org.geomajas.sld.geometry.OuterBoundaryIsInfo;
import org.geomajas.sld.geometry.PointTypeInfo;
import org.geomajas.sld.geometry.PolygonTypeInfo;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicalSymbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Default implementation of {@link StyleConverterService}. Supports named layers and user styles only.
 * 
 * @author Jan De Moerloose
 * 
 */
@Component
public class StyleConverterServiceImpl implements StyleConverterService {

	private StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);

	private FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

	private StyleBuilder styleBuilder;

	private final Logger log = LoggerFactory.getLogger(StyleConverterServiceImpl.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private FilterService filterService;

	@Autowired
	private ResourceService resourceService;

	public StyleFactory getStyleFactory() {
		return styleFactory;
	}

	public void setStyleFactory(StyleFactory styleFactory) {
		this.styleFactory = styleFactory;
	}

	public FilterFactory getFilterFactory() {
		return filterFactory;
	}

	public void setFilterFactory(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}

	public NamedStyleInfo convert(UserStyleInfo userStyle, FeatureInfo featureInfo) {
		NamedStyleInfo namedStyleInfo = new NamedStyleInfo();
		LabelStyleInfo labelStyleInfo = new LabelStyleInfo();
		List<FeatureStyleInfo> featureStyleInfos = new ArrayList<FeatureStyleInfo>();
		for (FeatureTypeStyleInfo featureTypeStyleInfo : userStyle.getFeatureTypeStyleList()) {
			int styleIndex = 0;
			for (RuleInfo ruleInfo : featureTypeStyleInfo.getRuleList()) {
				FeatureStyleInfo featureStyleInfo = new FeatureStyleInfo();
				if (ruleInfo.getChoice() != null) {
					if (ruleInfo.getChoice().ifFilter()) {
						featureStyleInfo.setFormula(convertFormula(ruleInfo.getChoice().getFilter(), featureInfo));
					}
				}
				for (SymbolizerTypeInfo symbolizerTypeInfo : ruleInfo.getSymbolizerList()) {
					if (symbolizerTypeInfo instanceof PointSymbolizerInfo) {
						PointSymbolizerInfo pointInfo = (PointSymbolizerInfo) symbolizerTypeInfo;
						convertSymbol(featureStyleInfo, pointInfo);
					} else if (symbolizerTypeInfo instanceof LineSymbolizerInfo) {
						LineSymbolizerInfo lineInfo = (LineSymbolizerInfo) symbolizerTypeInfo;
						convertStroke(featureStyleInfo, lineInfo.getStroke());
					} else if (symbolizerTypeInfo instanceof PolygonSymbolizerInfo) {
						PolygonSymbolizerInfo polygonInfo = (PolygonSymbolizerInfo) symbolizerTypeInfo;
						convertFill(featureStyleInfo, polygonInfo.getFill());
						convertStroke(featureStyleInfo, polygonInfo.getStroke());
					} else if (symbolizerTypeInfo instanceof TextSymbolizerInfo) {
						TextSymbolizerInfo textInfo = (TextSymbolizerInfo) symbolizerTypeInfo;
						labelStyleInfo.setFontStyle(convertFont(textInfo.getFont()));
						for (ExpressionInfo expr : textInfo.getLabel().getExpressionList()) {
							labelStyleInfo.setLabelAttributeName(expr.getValue());
						}
						convertFontFill(labelStyleInfo.getFontStyle(), textInfo.getFill());
						FeatureStyleInfo background = new FeatureStyleInfo();
						if (textInfo.getHalo() != null) {
							convertFill(background, textInfo.getHalo().getFill());
						}
						labelStyleInfo.setBackgroundStyle(background);
					}
				}
				if (featureStyleInfo.getStrokeColor() == null && featureStyleInfo.getFillColor() != null) {
					// avoid default stroke by setting invisible
					featureStyleInfo.setStrokeColor("black");
					featureStyleInfo.setStrokeOpacity(0);
					featureStyleInfo.setStrokeWidth(0);
				}
				featureStyleInfo.setIndex(styleIndex++);
				featureStyleInfo.setName(ruleInfo.getTitle() != null ? ruleInfo.getTitle() : ruleInfo.getName());
				featureStyleInfos.add(featureStyleInfo);
			}
		}
		namedStyleInfo.setName(userStyle.getTitle() != null ? userStyle.getTitle() : userStyle.getName());
		namedStyleInfo.setFeatureStyles(featureStyleInfos);
		namedStyleInfo.setLabelStyle(labelStyleInfo);
		return namedStyleInfo;
	}

	public Style convert(UserStyleInfo userStyleInfo) throws LayerException {
		IBindingFactory bindingFactory;
		try {
			// create a dummy SLD root
			StyledLayerDescriptorInfo sld = new StyledLayerDescriptorInfo();
			sld.setVersion("1.0.0");
			StyledLayerDescriptorInfo.ChoiceInfo choice = new StyledLayerDescriptorInfo.ChoiceInfo();
			NamedLayerInfo namedLayerInfo = new NamedLayerInfo();
			namedLayerInfo.setName("Dummy");
			NamedLayerInfo.ChoiceInfo userChoice = new NamedLayerInfo.ChoiceInfo();
			userChoice.setUserStyle(userStyleInfo);
			namedLayerInfo.getChoiceList().add(userChoice);
			choice.setNamedLayer(namedLayerInfo);
			sld.getChoiceList().add(choice);

			// force through Geotools parser
			bindingFactory = BindingDirectory.getFactory(StyledLayerDescriptorInfo.class);
			IMarshallingContext marshallingContext = bindingFactory.createMarshallingContext();
			StringWriter sw = new StringWriter();
			marshallingContext.setOutput(sw);
			marshallingContext.marshalDocument(sld);

			ResourceAwareSLDParser parser = new ResourceAwareSLDParser(styleFactory, resourceService);
			parser.setInput(new StringReader(sw.toString()));

			Style[] styles = parser.readXML();

			// SLDConfiguration configuration = sldFactory.createConfiguration();
			// Parser parser = new Parser(configuration);
			// StyledLayerDescriptor styledLayerDescriptor = (StyledLayerDescriptor) parser.parse(new StringReader(sw
			// .toString()));
			// NamedLayer namedLayer = (NamedLayer) styledLayerDescriptor.getStyledLayers()[0];
			// Style[] styles = namedLayer.getStyles();
			if (styles.length != 0) {
				return styles[0];
			} else {
				throw new LayerException(ExceptionCode.INVALID_USER_STYLE, userStyleInfo.getName());
			}
		} catch (Exception e) {
			throw new LayerException(e, ExceptionCode.INVALID_USER_STYLE, userStyleInfo.getName());
		}
	}

	public Rule convert(RuleInfo ruleInfo) throws LayerException {
		UserStyleInfo styleInfo = new UserStyleInfo();
		FeatureTypeStyleInfo fts = new FeatureTypeStyleInfo();
		fts.getRuleList().add(ruleInfo);
		styleInfo.getFeatureTypeStyleList().add(fts);
		Style style = convert(styleInfo);
		return style.featureTypeStyles().get(0).rules().get(0);
	}

	public UserStyleInfo convert(NamedStyleInfo namedStyleInfo, String geometryName) throws LayerException {
		StyleBuilder styleBuilder = new StyleBuilder(styleFactory, filterFactory);
		Style style = styleBuilder.createStyle();

		// list of rules
		List<Rule> rules = new ArrayList<Rule>();
		for (FeatureStyleInfo featureStyle : namedStyleInfo.getFeatureStyles()) {
			// create the filter
			Filter styleFilter = null;
			if (featureStyle.getFormula() != null && featureStyle.getFormula().length() > 0) {
				try {
					styleFilter = filterService.parseFilter(featureStyle.getFormula());
				} catch (GeomajasException e) {
					throw new LayerException(ExceptionCode.FILTER_PARSE_PROBLEM, featureStyle.getFormula());
				}
			} else {
				styleFilter = Filter.INCLUDE;
			}
			Rule rule = createRule(styleFilter, featureStyle, geometryName);
			// add the label symbolizer to the rule
			TextSymbolizer textSymbolizer = createTextSymbolizer(namedStyleInfo.getLabelStyle(),
					featureStyle.getLayerType());
			rule.symbolizers().add(textSymbolizer);
			rules.add(rule);
		}
		// create the style
		FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle(null, rules.toArray(new Rule[rules.size()]));
		style.featureTypeStyles().add(fts);
		// parse and to dto

		try {
			StyledLayerDescriptor styledLayerDescriptor = styleFactory.createStyledLayerDescriptor();
			UserLayer layer = styleFactory.createUserLayer();
			layer.setLayerFeatureConstraints(new FeatureTypeConstraint[] { null });
			styledLayerDescriptor.addStyledLayer(layer);
			layer.addUserStyle(style);

			SLDTransformer styleTransform = new SLDTransformer();
			String xml = styleTransform.transform(styledLayerDescriptor);
			IBindingFactory bindingFactory = BindingDirectory.getFactory(StyledLayerDescriptorInfo.class);
			IUnmarshallingContext unmarshallingContext = bindingFactory.createUnmarshallingContext();
			StyledLayerDescriptorInfo sld = (StyledLayerDescriptorInfo) unmarshallingContext
					.unmarshalDocument(new StringReader(xml));
			return sld.getChoiceList().get(0).getUserLayer().getUserStyleList().get(0);
		} catch (Exception e) {
			throw new LayerException(e, ExceptionCode.INVALID_USER_STYLE, namedStyleInfo.getName());
		}
	}

	private void convertSymbol(FeatureStyleInfo featureStyleInfo, PointSymbolizerInfo pointInfo) {
		GraphicInfo graphic = pointInfo.getGraphic();
		SymbolInfo symbol = new SymbolInfo();

		if (graphic.getChoiceList().size() > 0) {
			ChoiceInfo choice = graphic.getChoiceList().get(0);
			if (choice.ifExternalGraphic()) {
				ExternalGraphicInfo externalGraphic = choice.getExternalGraphic();
				String href = externalGraphic.getOnlineResource().getHref().getHref();
				ImageInfo image = new ImageInfo();
				image.setHref(href);
				// SLD has no selection concept + no default: what to do ?
				image.setSelectionHref(href);
				image.setHeight((int) Float.parseFloat(getParameterValue(graphic.getSize())));
				symbol.setImage(image);
			} else if (choice.ifMark()) {
				MarkInfo mark = choice.getMark();
				String name = mark.getWellKnownName().getWellKnownName();
				if (name.equalsIgnoreCase("square")) {
					RectInfo rect = new RectInfo();
					rect.setH(Float.parseFloat(getParameterValue(graphic.getSize())));
					rect.setW(Float.parseFloat(getParameterValue(graphic.getSize())));
					symbol.setRect(rect);
				} else {
					// should treat everything else as circle ?!
					CircleInfo circle = new CircleInfo();
					circle.setR(0.5F * Float.parseFloat(getParameterValue(graphic.getSize())));
					symbol.setCircle(circle);
				}
				convertFill(featureStyleInfo, mark.getFill());
				convertStroke(featureStyleInfo, mark.getStroke());
			}
		}
		featureStyleInfo.setSymbol(symbol);
	}

	private void convertFontFill(FontStyleInfo fontStyle, FillInfo fill) {
		if (fill != null) {
			Map<String, String> cssMap = getLiteralMap(fill.getCssParameterList());
			fontStyle.setColor(cssMap.get("fill"));
			if (cssMap.containsKey("fill-opacity")) {
				fontStyle.setOpacity(Float.parseFloat(cssMap.get("fill-opacity")));
			}
		}
	}

	private FontStyleInfo convertFont(FontInfo font) {
		FontStyleInfo fontStyle = new FontStyleInfo();
		if (font == null) {
			fontStyle.applyDefaults();
		} else {
			Map<String, String> cssMap = getLiteralMap(font.getCssParameterList());
			fontStyle.setFamily(cssMap.get("font-family"));
			if (cssMap.containsKey("font-size")) {
				fontStyle.setSize(Integer.parseInt(cssMap.get("font-size")));
			}
			fontStyle.setStyle(cssMap.get("font-style"));
			fontStyle.setWeight(cssMap.get("font-weight"));
		}
		return fontStyle;
	}

	private void convertFill(FeatureStyleInfo featureStyleInfo, FillInfo fill) {
		if (fill != null) {
			Map<String, String> cssMap = getLiteralMap(fill.getCssParameterList());
			if (cssMap.containsKey("fill")) {
				featureStyleInfo.setFillColor(cssMap.get("fill"));
			}
			if (cssMap.containsKey("fill-opacity")) {
				featureStyleInfo.setFillOpacity(Float.parseFloat(cssMap.get("fill-opacity")));
			}
			if (fill.getGraphicFill() != null) {
				GraphicInfo graphic = fill.getGraphicFill().getGraphic();
				for (GraphicInfo.ChoiceInfo choice : graphic.getChoiceList()) {
					if (choice.ifExternalGraphic()) {
						// can't handle this
					} else if (choice.ifMark()) {
						MarkInfo mark = (MarkInfo) choice.getMark();
						if (mark.getFill() != null) {
							convertFill(featureStyleInfo, mark.getFill());
						}
						if (mark.getStroke() != null) {
							convertStroke(featureStyleInfo, mark.getStroke());
						}
					}
				}
			}
		}
	}

	private void convertStroke(FeatureStyleInfo featureStyleInfo, StrokeInfo stroke) {
		if (stroke != null) {
			Map<String, String> cssMap = getLiteralMap(stroke.getCssParameterList());
			// not supported are "stroke-linejoin", "stroke-linecap", and "stroke-dashoffset"
			featureStyleInfo.setStrokeColor(cssMap.get("stroke"));
			if (cssMap.containsKey("stroke-opacity")) {
				featureStyleInfo.setStrokeOpacity(Float.parseFloat(cssMap.get("stroke-opacity")));
			}
			if (cssMap.containsKey("stroke-width")) {
				featureStyleInfo.setStrokeWidth((int) Float.parseFloat(cssMap.get("stroke-width")));
			}
			if (cssMap.containsKey("stroke-dasharray")) {
				featureStyleInfo.setDashArray(cssMap.get("stroke-dasharray"));
			}
		}
	}

	private String convertFormula(FilterTypeInfo filter, FeatureInfo featureInfo) {
		if (filter.ifComparisonOps()) {
			return toComparison(filter.getComparisonOps(), featureInfo);
		} else if (filter.ifFeatureIdList()) {
			return toFeatureIds(filter.getFeatureIdList());
		} else if (filter.ifLogicOps()) {
			return toLogic(filter.getLogicOps(), featureInfo);
		} else if (filter.ifSpatialOps()) {
			return toSpatial(filter.getSpatialOps());
		}
		return null;
	}

	private String toLogic(LogicOpsTypeInfo logicOps, FeatureInfo featureInfo) {
		if (logicOps instanceof UnaryLogicOpTypeInfo) {
			UnaryLogicOpTypeInfo unary = (UnaryLogicOpTypeInfo) logicOps;
			if (unary.ifComparisonOps()) {
				return "NOT " + toComparison(unary.getComparisonOps(), featureInfo);
			} else if (unary.ifLogicOps()) {
				return "NOT " + toLogic(unary.getLogicOps(), featureInfo);
			} else if (unary.ifSpatialOps()) {
				return "NOT " + toSpatial(unary.getSpatialOps());
			}

		} else if (logicOps instanceof BinaryLogicOpTypeInfo) {
			BinaryLogicOpTypeInfo binary = (BinaryLogicOpTypeInfo) logicOps;
			String[] expressions = new String[2];
			for (int i = 0; i < 2; i++) {
				if (binary.getChoiceList().get(i).ifComparisonOps()) {
					expressions[i] = toComparison(binary.getChoiceList().get(i).getComparisonOps(), featureInfo);
				} else if (binary.getChoiceList().get(i).ifLogicOps()) {
					expressions[i] = toLogic(binary.getChoiceList().get(i).getLogicOps(), featureInfo);
				} else if (binary.getChoiceList().get(i).ifSpatialOps()) {
					expressions[i] = toSpatial(binary.getChoiceList().get(i).getSpatialOps());
				}
			}
			if (binary instanceof OrInfo) {
				return "(" + expressions[0] + ") OR (" + expressions[1] + ")";
			} else if (binary instanceof AndInfo) {
				return "(" + expressions[0] + ") AND (" + expressions[1] + ")";
			}
		}
		return null;
	}

	private String toSpatial(SpatialOpsTypeInfo spatialOps) {
		if (spatialOps instanceof BBOXTypeInfo) {
			BBOXTypeInfo bbox = (BBOXTypeInfo) spatialOps;
			String propertyName = bbox.getPropertyName().getValue();
			String coordinates = null;
			if (bbox.getBox().ifCoordinates()) {
				String cs = bbox.getBox().getCoordinates().getCs();
				String ts = bbox.getBox().getCoordinates().getTs();
				String ds = bbox.getBox().getCoordinates().getDecimal();
				coordinates = bbox.getBox().getCoordinates().getString().trim().replace(ds, "ds").replace(ts, "ts")
						.replace(cs, "cs").replace("ds", ".").replace("ts", ",").replace("cs", ",");
			} else {
				for (CoordTypeInfo coord : bbox.getBox().getCoordList()) {
					coordinates += coord.getX() + "," + coord.getY() + (coordinates == null ? "," : "");
				}
			}
			return "BBOX (" + propertyName + "," + coordinates + ")";
		} else if (spatialOps instanceof BinarySpatialOpTypeInfo) {
			BinarySpatialOpTypeInfo binary = (BinarySpatialOpTypeInfo) spatialOps;
			String propertyName = binary.getPropertyName().getValue();
			if (binary.ifGeometry()) {
				Geometry geometry = null;
				WKTWriter writer = new WKTWriter();
				GeometryFactory factory = new GeometryFactory();
				AbstractGeometryInfo geom = binary.getGeometry();
				geometry = toGeometry(factory, geom);
				String wkt = writer.write(geometry);
				if (binary instanceof ContainsInfo) {
					return "CONTAINS(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof CrossesInfo) {
					return "CROSSES(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof DisjointInfo) {
					return "DISJOINT(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof EqualsInfo) {
					return "EQUALS(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof IntersectsInfo) {
					return "INTERSECTS(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof OverlapsInfo) {
					return "OVERLAPS(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof TouchesInfo) {
					return "TOUCHES(" + propertyName + "," + wkt + ")";
				} else if (binary instanceof WithinInfo) {
					return "WITHIN(" + propertyName + "," + wkt + ")";
				}
			} else if (spatialOps instanceof DistanceBufferTypeInfo) {
				DistanceBufferTypeInfo distanceBuffer = (DistanceBufferTypeInfo) spatialOps;
				AbstractGeometryInfo geom = distanceBuffer.getGeometry();
				GeometryFactory factory = new GeometryFactory();
				Geometry geometry = toGeometry(factory, geom);
				WKTWriter writer = new WKTWriter();
				String wkt = writer.write(geometry);
				String units = distanceBuffer.getDistance().getUnits();
				String distance = distanceBuffer.getDistance().getValue();
				if (distanceBuffer instanceof DWithinInfo) {
					return "DWITHIN(" + propertyName + "," + wkt + "," + distance + "," + units + ")";
				} else if (distanceBuffer instanceof BeyondInfo) {
					return "BEYOND(" + propertyName + "," + wkt + "," + distance + "," + units + ")";
				}
			}
		}
		return null;
	}

	private Geometry toGeometry(GeometryFactory factory, AbstractGeometryInfo geom) {
		Geometry geometry = null;
		if (geom instanceof AbstractGeometryCollectionInfo) {
			AbstractGeometryCollectionInfo geomCollection = (AbstractGeometryCollectionInfo) geom;
			List<GeometryMemberInfo> members = geomCollection.getGeometryMemberList();
			if (geom instanceof MultiPointInfo) {
				Point[] points = new Point[members.size()];
				for (int i = 0; i < members.size(); i++) {
					points[i] = (Point) toSimpleGeometry(factory, members.get(i).getGeometry());
				}
				geometry = factory.createMultiPoint(points);
			} else if (geom instanceof MultiLineStringInfo) {
				LineString[] lines = new LineString[members.size()];
				for (int i = 0; i < members.size(); i++) {
					lines[i] = (LineString) toSimpleGeometry(factory, members.get(i).getGeometry());
				}
				geometry = factory.createMultiLineString(lines);
			} else if (geom instanceof MultiPolygonInfo) {
				Polygon[] polygons = new Polygon[members.size()];
				for (int i = 0; i < members.size(); i++) {
					polygons[i] = (Polygon) toSimpleGeometry(factory, members.get(i).getGeometry());
				}
				geometry = factory.createMultiPolygon(polygons);
			} else if (geom instanceof MultiGeometryInfo) {
				Geometry[] geometries = new Geometry[members.size()];
				for (int i = 0; i < members.size(); i++) {
					geometries[i] = toSimpleGeometry(factory, members.get(i).getGeometry());
				}
				geometry = factory.createGeometryCollection(geometries);
			}
		} else {
			geometry = toSimpleGeometry(factory, geom);
		}
		return geometry;
	}

	private Geometry toSimpleGeometry(GeometryFactory factory, AbstractGeometryInfo geom) {
		Geometry geometry = null;
		if (geom instanceof PointTypeInfo) {
			PointTypeInfo point = (PointTypeInfo) geom;
			if (point.ifCoord()) {
				geometry = factory.createPoint(getCoordinates(Collections.singletonList(point.getCoord()))[0]);
			} else if (point.ifCoordinates()) {
				geometry = factory.createPoint(getCoordinates(point.getCoordinates())[0]);
			}
		} else if (geom instanceof LineStringTypeInfo) {
			LineStringTypeInfo linestring = (LineStringTypeInfo) geom;
			if (linestring.ifCoordList()) {
				geometry = factory.createLineString(getCoordinates(linestring.getCoordList()));
			} else if (linestring.ifCoordinates()) {
				geometry = factory.createLineString(getCoordinates(linestring.getCoordinates()));
			}
		} else if (geom instanceof PolygonTypeInfo) {
			PolygonTypeInfo polygon = (PolygonTypeInfo) geom;
			OuterBoundaryIsInfo outer = polygon.getOuterBoundaryIs();
			LinearRing shell = toLinearRing(factory, outer.getLinearRing());
			LinearRing[] holes = new LinearRing[polygon.getInnerBoundaryIList().size()];
			int i = 0;
			for (InnerBoundaryIsInfo inner : polygon.getInnerBoundaryIList()) {
				holes[i++] = toLinearRing(factory, inner.getLinearRing());
			}
			geometry = factory.createPolygon(shell, holes);
		}
		return geometry;
	}

	private LinearRing toLinearRing(GeometryFactory factory, LinearRingTypeInfo linearRing) {
		LinearRing ring = null;
		if (linearRing.ifCoordList()) {
			ring = factory.createLinearRing(getCoordinates(linearRing.getCoordList()));
		} else if (linearRing.ifCoordinates()) {
			ring = factory.createLinearRing(getCoordinates(linearRing.getCoordinates()));
		}
		return ring;
	}

	private Coordinate[] getCoordinates(List<CoordTypeInfo> coords) {
		Coordinate[] result = new Coordinate[coords.size()];
		int i = 0;
		for (CoordTypeInfo coordinate : coords) {
			result[i++] = new Coordinate(coordinate.getX().doubleValue(), coordinate.getY().doubleValue());
		}
		return result;
	}

	private Coordinate[] getCoordinates(CoordinatesTypeInfo coords) {
		String[] coordinates = coords.getString().split(
				Pattern.quote(coords.getCs()) + "|" + Pattern.quote(coords.getTs()));
		Coordinate[] result = new Coordinate[coordinates.length / 2];
		for (int i = 0; i < coordinates.length; i += 2) {
			double x = Double.parseDouble(coordinates[i].replace(coords.getDecimal(), "."));
			double y = Double.parseDouble(coordinates[i + 1].replace(coords.getDecimal(), "."));
			result[i / 2] = new Coordinate(x, y);
		}
		return result;
	}

	private String toFeatureIds(List<FeatureIdTypeInfo> featureIds) {
		StringBuilder stringBuilder = new StringBuilder("IN (");
		for (Iterator<FeatureIdTypeInfo> it = featureIds.iterator(); it.hasNext();) {
			stringBuilder.append("'").append(it.next().getFid()).append("'");
			stringBuilder.append(it.hasNext() ? "," : ")");
		}
		return stringBuilder.toString();
	}

	private String toComparison(ComparisonOpsTypeInfo coOps, FeatureInfo featureInfo) {
		if (coOps instanceof BinaryComparisonOpTypeInfo) {
			BinaryComparisonOpTypeInfo binary = (BinaryComparisonOpTypeInfo) coOps;
			String propertyName = binary.getExpressionList().get(0).getValue();
			String propertyValue = binary.getExpressionList().get(1).getValue();
			PrimitiveType type = PrimitiveType.STRING;
			for (AttributeInfo attributeInfo : featureInfo.getAttributes()) {
				if (attributeInfo.getName().equals(propertyName)) {
					if (attributeInfo instanceof PrimitiveAttributeInfo) {
						type = ((PrimitiveAttributeInfo) attributeInfo).getType();
					}
				}
			}
			switch (type) {
				case BOOLEAN:
					propertyValue = propertyValue.toUpperCase();
					break;
				case DATE:
				case IMGURL:
				case STRING:
				case URL:
					propertyValue = "'" + propertyValue + "'";
					break;

			}
			if (binary instanceof PropertyIsEqualToInfo) {
				return propertyName + " = " + propertyValue;
			} else if (binary instanceof PropertyIsGreaterThanInfo) {
				return propertyName + " > " + propertyValue;
			} else if (binary instanceof PropertyIsGreaterThanOrEqualToInfo) {
				return propertyName + " >= " + propertyValue;
			} else if (binary instanceof PropertyIsLessThanInfo) {
				return propertyName + " < " + propertyValue;
			} else if (binary instanceof PropertyIsLessThanOrEqualToInfo) {
				return propertyName + " <= " + propertyValue;
			} else if (binary instanceof PropertyIsNotEqualToInfo) {
				return propertyName + " != " + propertyValue;
			}
		} else {
			if (coOps instanceof PropertyIsBetweenTypeInfo) {
				PropertyIsBetweenTypeInfo isBetween = (PropertyIsBetweenTypeInfo) coOps;
				String lower = isBetween.getLowerBoundary().getExpression().getValue();
				String upper = isBetween.getUpperBoundary().getExpression().getValue();
				String propertyName = isBetween.getExpression().getValue();
				return propertyName + " BETWEEN " + lower + " AND " + upper;
			} else if (coOps instanceof PropertyIsLikeTypeInfo) {
				PropertyIsLikeTypeInfo isLike = (PropertyIsLikeTypeInfo) coOps;
				String propertyName = isLike.getPropertyName().getValue();
				return propertyName + " LIKE '" + isLike.getLiteral().getValue() + "'";
			} else if (coOps instanceof PropertyIsNullTypeInfo) {
				PropertyIsNullTypeInfo isNull = (PropertyIsNullTypeInfo) coOps;
				if (isNull.ifLiteral()) {
					String literal = isNull.getLiteral().getValue();
					return "'" + literal + "' IS NULL ";
				} else {
					String propertyName = isNull.getPropertyName().getValue();
					return propertyName + " IS NULL ";
				}
			}
		}
		return null;
	}

	private Map<String, String> getLiteralMap(List<CssParameterInfo> css) {
		HashMap<String, String> result = new HashMap<String, String>();
		if (css != null) {
			for (CssParameterInfo cssParameter : css) {
				if (cssParameter.getValue() != null) {
					result.put(cssParameter.getName(), cssParameter.getValue());
				} else if (cssParameter.getExpressionList().size() > 0) {
					ExpressionInfo expression = cssParameter.getExpressionList().get(0);
					if (expression instanceof LiteralTypeInfo) {
						result.put(cssParameter.getName(), expression.getValue());
					}
				}
			}
		}
		return result;
	}

	private String getParameterValue(ParameterValueTypeInfo parameter) {
		if (parameter.getValue() != null) {
			return parameter.getValue();
		} else if (parameter.getExpressionList().size() > 0) {
			ExpressionInfo expression = parameter.getExpressionList().get(0);
			if (expression instanceof LiteralTypeInfo) {
				return expression.getValue();
			}
		}
		return null;
	}

	private Rule createRule(Filter filter, FeatureStyleInfo featureStyle, String geometryName) throws LayerException {
		List<Rule> rules = new ArrayList<Rule>();
		Rule rule = styleBuilder.createRule(createGeometrySymbolizer(featureStyle));
		if (filter.equals(Filter.INCLUDE)) {
			rule.setElseFilter(true);
		} else {
			rule.setFilter(filter);
		}
		rule.setName(featureStyle.getName());
		rule.setTitle(featureStyle.getName());
		rules.add(rule);
		return rule;
	}

	private Symbolizer createGeometrySymbolizer(FeatureStyleInfo featureStyle) throws LayerException {
		Symbolizer symbolizer = null;
		switch (featureStyle.getLayerType()) {
			case MULTIPOLYGON:
			case POLYGON:
				symbolizer = styleBuilder.createPolygonSymbolizer(createStroke(featureStyle), createFill(featureStyle));
				break;
			case MULTILINESTRING:
			case LINESTRING:
				symbolizer = styleBuilder.createLineSymbolizer(createStroke(featureStyle));
				break;
			case POINT:
			case MULTIPOINT:
				PointSymbolizer ps = styleBuilder.createPointSymbolizer();
				GraphicalSymbol symbol = createSymbol(featureStyle);
				if (symbol instanceof Mark) {
					ps.getGraphic().setSize(((Mark) symbol).getSize());
				} else {
					Expression size = styleBuilder.literalExpression(featureStyle.getSymbol().getImage().getHeight());
					ps.getGraphic().setSize(size);
				}
				ps.getGraphic().graphicalSymbols().clear();
				ps.getGraphic().graphicalSymbols().add(createSymbol(featureStyle));
				symbolizer = ps;
				break;
		}
		return symbolizer;
	}

	private TextSymbolizer createTextSymbolizer(LabelStyleInfo labelStyle, LayerType layerType) {
		Fill fontFill = styleBuilder.createFill(styleBuilder.literalExpression(labelStyle.getFontStyle().getColor()),
				styleBuilder.literalExpression(labelStyle.getFontStyle().getOpacity()));
		TextSymbolizer symbolizer = styleBuilder.createTextSymbolizer();
		symbolizer.setFill(fontFill);
		FontStyleInfo fontInfo = labelStyle.getFontStyle();
		symbolizer.setFont(styleBuilder.createFont(styleBuilder.literalExpression(fontInfo.getFamily()),
				styleBuilder.literalExpression(fontInfo.getStyle()),
				styleBuilder.literalExpression(fontInfo.getWeight()),
				styleBuilder.literalExpression(fontInfo.getSize())));
		symbolizer.setLabel(styleBuilder.attributeExpression(labelStyle.getLabelAttributeName()));
		Fill haloFill = styleBuilder.createFill(
				styleBuilder.literalExpression(labelStyle.getBackgroundStyle().getFillColor()),
				styleBuilder.literalExpression(labelStyle.getBackgroundStyle().getFillOpacity()));
		symbolizer.setHalo(styleBuilder.createHalo(haloFill, 1));
		// label placement : point at bottom-center of label (same as vectorized)
		switch (layerType) {
			case MULTIPOINT:
			case POINT:
				symbolizer.setLabelPlacement(styleBuilder.createPointPlacement(0.5, 0, 0));
				break;
			default:
				break;
		}
		return symbolizer;
	}

	private GraphicalSymbol createSymbol(FeatureStyleInfo featureStyle) throws LayerException {
		SymbolInfo info = featureStyle.getSymbol();
		if (info.getImage() != null) {

			return styleBuilder.createExternalGraphic(getURL(info.getImage().getHref()), getFormat(info.getImage()
					.getHref()));
		} else {
			Mark mark = null;
			if (info.getRect() != null) {
				// TODO: do rectangles by adding custom factory ?
				mark = styleBuilder.createMark("square");
				mark.setSize(styleBuilder.literalExpression((int) info.getRect().getW()));
			} else if (info.getCircle() != null) {
				mark = styleBuilder.createMark("circle");
				mark.setSize(styleBuilder.literalExpression(2 * (int) info.getCircle().getR()));
			}
			mark.setFill(createFill(featureStyle));
			mark.setStroke(createStroke(featureStyle));
			return mark;
		}
	}

	private Stroke createStroke(FeatureStyleInfo featureStyle) {
		Stroke stroke = styleBuilder.createStroke(styleBuilder.literalExpression(featureStyle.getStrokeColor()),
				styleBuilder.literalExpression(featureStyle.getStrokeWidth()),
				styleBuilder.literalExpression(featureStyle.getStrokeOpacity()));
		if (featureStyle.getDashArray() != null) {
			String[] strs = featureStyle.getDashArray().split(",");
			float[] nrs = new float[strs.length];
			for (int i = 0; i < strs.length; i++) {
				try {
					nrs[i] = Float.parseFloat(strs[i]);
				} catch (NumberFormatException e) {
					log.warn("unparseable dash array " + featureStyle.getDashArray(), e);
				}
			}
			stroke.setDashArray(nrs);
		}
		return stroke;
	}

	private Fill createFill(FeatureStyleInfo featureStyle) {
		return styleBuilder.createFill(styleBuilder.literalExpression(featureStyle.getFillColor()),
				styleBuilder.literalExpression(featureStyle.getFillOpacity()));
	}

	private URL getURL(String resourceLocation) throws LayerException {
		Resource resource = applicationContext.getResource(resourceLocation);
		if (resource.exists()) {
			try {
				return resource.getURL();
			} catch (IOException e) {
				log.warn("missing resource {}", resourceLocation);
				throw new LayerException(ExceptionCode.RESOURCE_NOT_FOUND, resourceLocation);
			}
		} else {
			String gwtResource = "classpath:" + resourceLocation;
			try {
				Resource[] matching = applicationContext.getResources(gwtResource);
				if (matching.length > 0) {
					return matching[0].getURL();
				} else {
					log.warn("missing resource {}", gwtResource);
					throw new LayerException(ExceptionCode.RESOURCE_NOT_FOUND, gwtResource);
				}
			} catch (IOException e) {
				log.warn("missing resource {}", resourceLocation);
				throw new LayerException(ExceptionCode.RESOURCE_NOT_FOUND, resourceLocation);
			}
		}
	}

	private String getFormat(String href) {
		return "image/" + StringUtils.getFilenameExtension(href);
	}

	@PostConstruct
	private void postConstruct() {
		styleBuilder = new StyleBuilder(styleFactory, filterFactory);
	}

}
