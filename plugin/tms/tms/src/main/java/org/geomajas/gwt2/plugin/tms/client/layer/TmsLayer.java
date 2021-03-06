/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2014 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.gwt2.plugin.tms.client.layer;

import org.geomajas.annotation.Api;
import org.geomajas.gwt2.client.map.layer.AbstractTileBasedLayer;
import org.geomajas.gwt2.client.map.layer.tile.TileConfiguration;
import org.geomajas.gwt2.client.map.render.TileRenderer;
import org.geomajas.gwt2.plugin.tms.client.configuration.TileMapInfo;
import org.geomajas.gwt2.plugin.tms.client.configuration.TileSetInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A layer based upon a TMS service. A TmsLayer is a {@link org.geomajas.gwt2.client.map.layer.tile.TileBasedLayer}. TMS
 * layers have no other functionality then just displaying themselves.
 *
 * @author Pieter De Graef
 * @since 2.1.0
 */
@Api(allMethods = true)
public class TmsLayer extends AbstractTileBasedLayer {

	private final TmsLayerConfiguration layerConfiguration;

	private final TmsTileRenderer tileRenderer;

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	/**
	 * Create a new layer that belongs to the given map model, using the given meta-data.
	 *
	 * @param id The unique ID for this layer.
	 * @param tileConfiguration The configuration object defining the tile structure.
	 * @param layerConfiguration The general TMS layer configuration object.
	 */
	public TmsLayer(String id, TileConfiguration tileConfiguration, TmsLayerConfiguration layerConfiguration) {
		this(id, id, tileConfiguration, layerConfiguration);
	}

	/**
	 * Create a new layer that belongs to the given map model, using the given meta-data.
	 *
	 * @param id The unique ID for this layer.
	 * @param title The title for this layer. It's the title that is usually used in the GUI as label.
	 * @param tileConfiguration The configuration object defining the tile structure.
	 * @param layerConfiguration The general TMS layer configuration object.
	 */
	public TmsLayer(String id, String title, TileConfiguration tileConfiguration, TmsLayerConfiguration
			layerConfiguration) {
		super(id, tileConfiguration);
		this.title = title;
		this.layerConfiguration = layerConfiguration;
		this.tileRenderer = new TmsTileRenderer(layerConfiguration);
	}

	/**
	 * Create a new layer using a TileMapInfo object.
	 *
	 * @param tileMapInfo The configuration object to create a layer for. Both the ID and title will be taken from this
	 *                    configuration object.
	 */
	public TmsLayer(TileMapInfo tileMapInfo) {
		this(tileMapInfo.getTitle(), tileMapInfo);
	}

	/**
	 * Create a new layer using a TileMapInfo object.
	 *
	 * @param id The unique ID for this layer.
	 * @param tileMapInfo The configuration object to create a layer for. The layer title will be taken from this
	 *                    configuration object.
	 */
	public TmsLayer(String id, TileMapInfo tileMapInfo) {
		super(tileMapInfo.getTitle(), new TileConfiguration(tileMapInfo.getTileFormat().getWidth(),
				tileMapInfo.getTileFormat().getHeight(), tileMapInfo.getOrigin(), new ArrayList<Double>()));
		this.layerConfiguration = new TmsLayerConfiguration();
		this.layerConfiguration.setBaseUrl(tileMapInfo.getHref());
		this.layerConfiguration.setFileExtension(tileMapInfo.getTileFormat().getExtension());
		this.tileRenderer = new TmsTileRenderer(layerConfiguration);

		List<Double> resolutions = new ArrayList<Double>();
		for (TileSetInfo tileSetInfo : tileMapInfo.getTileSets()) {
			resolutions.add(tileSetInfo.getUnitsPerPixel());
		}
		getTileConfiguration().setResolutions(resolutions);
	}

	// ------------------------------------------------------------------------
	// AbstractTileBasedLayer implementation:
	// ------------------------------------------------------------------------

	@Override
	public TileRenderer getTileRenderer() {
		return tileRenderer;
	}

	// ------------------------------------------------------------------------
	// Other public methods:
	// ------------------------------------------------------------------------

	/**
	 * Get the configuration object that is used to build URLs to the tiles.
	 *
	 * @return The layer configuration object.
	 */
	public TmsLayerConfiguration getConfiguration() {
		return layerConfiguration;
	}
}
