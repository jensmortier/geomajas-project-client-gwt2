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

package org.geomajas.plugin.editing.client.event.state;

import org.geomajas.annotation.Api;
import org.geomajas.annotation.UserImplemented;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Interface for event handlers that catch {@link GeometryIndexHighlightEndEvent}s.
 * 
 * @author Pieter De Graef
 * @since 2.0.0
 */
@Api(allMethods = true)
@UserImplemented
public interface GeometryIndexHighlightEndHandler extends EventHandler {

	/** The type of the handler. */
	GwtEvent.Type<GeometryIndexHighlightEndHandler> TYPE = new GwtEvent.Type<GeometryIndexHighlightEndHandler>();

	/**
	 * Called when highlighting a part of a geometry has ended.
	 * 
	 * @param event
	 *            {@link GeometryIndexHighlightEndEvent}
	 */
	void onGeometryIndexHighlightEnd(GeometryIndexHighlightEndEvent event);
}