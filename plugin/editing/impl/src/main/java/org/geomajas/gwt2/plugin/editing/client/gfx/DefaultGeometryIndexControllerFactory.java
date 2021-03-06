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

package org.geomajas.gwt2.plugin.editing.client.gfx;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.gwt2.client.controller.MapController;
import org.geomajas.gwt2.client.map.MapPresenter;
import org.geomajas.plugin.editing.client.handler.AbstractGeometryIndexMapHandler;
import org.geomajas.plugin.editing.client.handler.EdgeMapHandlerFactory;
import org.geomajas.plugin.editing.client.handler.GeometryIndexDragSelectionHandler;
import org.geomajas.plugin.editing.client.handler.GeometryIndexHighlightHandler;
import org.geomajas.plugin.editing.client.handler.GeometryIndexInsertHandler;
import org.geomajas.plugin.editing.client.handler.GeometryIndexSelectHandler;
import org.geomajas.plugin.editing.client.handler.GeometryIndexSnapToDeleteHandler;
import org.geomajas.plugin.editing.client.handler.GeometryIndexStopInsertingHandler;
import org.geomajas.plugin.editing.client.handler.VertexMapHandlerFactory;
import org.geomajas.plugin.editing.client.service.GeometryEditService;
import org.geomajas.plugin.editing.client.service.GeometryEditState;
import org.geomajas.plugin.editing.client.service.GeometryIndex;
import org.geomajas.plugin.editing.client.service.GeometryIndexNotFoundException;
import org.geomajas.gwt2.plugin.editing.client.controller.CompositeGeometryIndexController;
import org.geomajas.gwt2.plugin.editing.client.controller.EdgeMarkerHandler;

/**
 * Default implementation of the {@link GeometryIndexControllerFactory}. Provides the default editing behavior. It is
 * possible to add custom handlers on top of the default ones.
 * 
 * @author Pieter De Graef
 * @author Jan Venstermans
 */
public class DefaultGeometryIndexControllerFactory implements GeometryIndexControllerFactory {

	private final List<VertexMapHandlerFactory> vertexFactories = new ArrayList<VertexMapHandlerFactory>();

	private final List<EdgeMapHandlerFactory> edgeFactories = new ArrayList<EdgeMapHandlerFactory>();

	private final MapPresenter mapPresenter;

	/**
	 * Private factory definition for create a handler for vertices.
	 * 
	 * @author Pieter De Graef
	 */
	private interface VertexHandlerFactory extends VertexMapHandlerFactory {

		AbstractGeometryIndexMapHandler create();
	}

	/**
	 * Private factory definition for create a handler for edges.
	 * 
	 * @author Pieter De Graef
	 */
	private interface EdgeHandlerFactory extends EdgeMapHandlerFactory {

		AbstractGeometryIndexMapHandler create();
	}

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	public DefaultGeometryIndexControllerFactory(MapPresenter mapPresenter) {
		this.mapPresenter = mapPresenter;

		// Create all the default vertex handler factories:
		vertexFactories.add(new VertexHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexHighlightHandler();
			}
		});
		vertexFactories.add(new VertexHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexSelectHandler();
			}
		});
		vertexFactories.add(new VertexHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexDragSelectionHandler();
			}
		});
		vertexFactories.add(new VertexHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexSnapToDeleteHandler();
			}
		});
		vertexFactories.add(new VertexHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexStopInsertingHandler();
			}
		});

		// Create all the default edge handler factories:
		edgeFactories.add(new EdgeHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexHighlightHandler();
			}
		});
		edgeFactories.add(new EdgeHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexInsertHandler();
			}
		});
		edgeFactories.add(new EdgeHandlerFactory() {

			public AbstractGeometryIndexMapHandler create() {
				return new GeometryIndexSnapToDeleteHandler();
			}
		});
	}

	public void addVertexHandlerFactory(VertexMapHandlerFactory factory) {
		vertexFactories.add(factory);
	}

	public void addEdgeHandlerFactory(EdgeMapHandlerFactory factory) {
		edgeFactories.add(factory);
	}

	// ------------------------------------------------------------------------
	// GeometryIndexControllerFactory implementation:
	// ------------------------------------------------------------------------

	@Override
	public MapController create(GeometryEditService editService, GeometryIndex index)
			throws GeometryIndexNotFoundException {
		if (index == null) {
			return null;
		}
		switch (editService.getIndexService().getType(index)) {
			case TYPE_VERTEX:
				return createVertexController(editService, index);
			case TYPE_EDGE:
				return createEdgeController(editService, index);
			default:
				return null;
		}
	}

	// ------------------------------------------------------------------------
	// Private methods:
	// ------------------------------------------------------------------------

	private MapController createVertexController(GeometryEditService editService, GeometryIndex index) {
		CompositeGeometryIndexController controller = new CompositeGeometryIndexController(editService, index,
				editService.getEditingState() == GeometryEditState.DRAGGING);
		for (VertexMapHandlerFactory factory : vertexFactories) {
			controller.addMapHandler(factory.create());
		}
		return controller;
	}

	private MapController createEdgeController(GeometryEditService editService, GeometryIndex index) {
		CompositeGeometryIndexController controller = new CompositeGeometryIndexController(editService, index,
				editService.getEditingState() == GeometryEditState.DRAGGING);
		for (EdgeMapHandlerFactory factory : edgeFactories) {
			controller.addMapHandler(factory.create());
		}

		EdgeMarkerHandler edgeMarkerHandler = new EdgeMarkerHandler(mapPresenter, editService, controller);
		controller.addMouseOutHandler(edgeMarkerHandler);
		controller.addMouseMoveHandler(edgeMarkerHandler);
		controller.addMapDownHandler(edgeMarkerHandler);
		return controller;
	}
}