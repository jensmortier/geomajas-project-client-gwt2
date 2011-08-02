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
package org.geomajas.widget.searchandfilter.client.widget.search;

import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.widget.searchandfilter.client.widget.geometricsearch.GeometryUpdateHandler;
import org.geomajas.widget.searchandfilter.search.dto.Criterion;

import com.smartgwt.client.widgets.Canvas;

/**
 * @see {@link SearchWidgetRegistry}.
 * @author Kristof Heirwegh
 */
public abstract class SearchPanel extends Canvas {

	protected MapWidget mapWidget;
	protected GeometryUpdateHandler handler;

	public SearchPanel(MapWidget mapWidget) {
		super();
		if (mapWidget == null) {
			throw new IllegalArgumentException("Please provide a mapWidget.");
		}
		this.mapWidget = mapWidget;
	}

	/**
	 * Called before getFeatureSearchCriterion().
	 * <p>
	 * Search will be cancelled if you return false.
	 *
	 * @return true if a criterion can be returned.
	 */
	public abstract boolean validate();

	/**
	 * Should the "Add To Favourites" button be shown?
	 *
	 * @return true when "add to favourites" button is visible
	 */
	public boolean canAddToFavourites() {
		return true;
	}

	/**
	 * Should the "Reset" button be shown?
	 *
	 * @return true when reset button is visible
	 */
	public boolean canBeReset() {
		return true;
	}

	/**
	 * @return an object with the settings of your search, allowing the
	 *         specifics of the search to be stored (in favourites).
	 */
	public abstract Criterion getFeatureSearchCriterion();
	
	
	/**
	 * @return the vector layer to search in.
	 *       
	 */
	public abstract VectorLayer getFeatureSearchVectorLayer();

	public abstract void reset();

	/**
	 * Called to restore the settings previously requested through
	 * getSettings().
	 *
	 * @param featureSearch settings
	 */
	public abstract void initialize(Criterion featureSearch);

	/**
	 * Get the map widget to which this search panel applies.
	 *
	 * @return map widget
	 */
	public MapWidget getMapWidget() {
		return mapWidget;
	}
}
