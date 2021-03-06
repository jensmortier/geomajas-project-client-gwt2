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

package org.geomajas.plugin.editing.client.event;

import org.geomajas.annotation.Api;
import org.geomajas.geometry.Geometry;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that reports the editing of a geometry has begun.
 * 
 * @author Pieter De Graef
 * @since 2.0.0
 */
@Api(allMethods = true)
public class GeometryEditStartEvent extends GwtEvent<GeometryEditStartHandler> {

	private final Geometry geometry;

	/**
	 * Create a new event.
	 * 
	 * @param geometry
	 *            The geometry being edited.
	 */
	public GeometryEditStartEvent(Geometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public Type<GeometryEditStartHandler> getAssociatedType() {
		return GeometryEditStartHandler.TYPE;
	}

	@Override
	protected void dispatch(GeometryEditStartHandler geometryEditWorkflowHandler) {
		geometryEditWorkflowHandler.onGeometryEditStart(this);
	}

	/**
	 * Get the geometry that will be edited.
	 * 
	 * @return The geometry that is to be edited.
	 */
	public Geometry getGeometry() {
		return geometry;
	}
}
