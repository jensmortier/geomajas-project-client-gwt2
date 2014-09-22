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

package org.geomajas.gwt2.client.map.render.dom;

import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Matrix;
import org.geomajas.gwt2.client.animation.Trajectory;
import org.geomajas.gwt2.client.event.LayerAddedEvent;
import org.geomajas.gwt2.client.event.LayerHideEvent;
import org.geomajas.gwt2.client.event.LayerRefreshedEvent;
import org.geomajas.gwt2.client.event.LayerRefreshedHandler;
import org.geomajas.gwt2.client.event.LayerRemovedEvent;
import org.geomajas.gwt2.client.event.LayerShowEvent;
import org.geomajas.gwt2.client.event.LayerStyleChangedEvent;
import org.geomajas.gwt2.client.event.LayerStyleChangedHandler;
import org.geomajas.gwt2.client.event.LayerVisibilityHandler;
import org.geomajas.gwt2.client.event.LayerVisibilityMarkedEvent;
import org.geomajas.gwt2.client.event.MapCompositionHandler;
import org.geomajas.gwt2.client.map.MapEventBus;
import org.geomajas.gwt2.client.map.MapPresenterImpl;
import org.geomajas.gwt2.client.map.View;
import org.geomajas.gwt2.client.map.ViewPort;
import org.geomajas.gwt2.client.map.layer.Layer;
import org.geomajas.gwt2.client.map.layer.tile.TileBasedLayer;
import org.geomajas.gwt2.client.map.render.LayerRenderer;
import org.geomajas.gwt2.client.map.render.RenderMapEvent;
import org.geomajas.gwt2.client.map.render.RenderingInfo;
import org.geomajas.gwt2.client.map.render.TileLevelRenderer;
import org.geomajas.gwt2.client.map.render.TileQueue;
import org.geomajas.gwt2.client.map.render.dom.container.HtmlContainer;
import org.geomajas.gwt2.client.map.render.dom.container.HtmlGroup;
import org.geomajas.gwt2.client.service.DomService;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Layer renderer implementation for layers that use renderers at fixed scales.
 * 
 * @author Jan De Moerloose
 */
public abstract class DomTileLevelLayerRenderer implements LayerRenderer {

	private static Logger logger = Logger.getLogger(DomTileLevelLayerRenderer.class.getName());

	private final ViewPort viewPort;

	private final Layer layer;

	private final Map<Integer, TileLevelRenderer> tileLevelRenderers;

	private final Map<Integer, HtmlContainer> tileLevelContainers;

	private HtmlContainer container;

	private TileQueue queue;

	private TileLevelRenderer targetRenderer;
	
	private Trajectory trajectory;

	private MapEventBus eventBus;

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	public DomTileLevelLayerRenderer(final ViewPort viewPort, final Layer layer, final MapEventBus eventBus) {
		this.viewPort = viewPort;
		this.layer = layer;
		this.eventBus = eventBus;
		this.tileLevelRenderers = new HashMap<Integer, TileLevelRenderer>();
		this.tileLevelContainers = new HashMap<Integer, HtmlContainer>();
		// Refresh the contents of this renderer when the layer is refreshed:
		eventBus.addLayerRefreshedHandler(new LayerRefreshedHandler() {

			@Override
			public void onLayerRefreshed(LayerRefreshedEvent event) {
				refresh();
			}
		}, layer);

		eventBus.addMapCompositionHandler(new MapCompositionHandler() {

			@Override
			public void onLayerRemoved(LayerRemovedEvent event) {
				if (event.getLayer() == layer) {
					clear();
					refresh();
				}
			}

			@Override
			public void onLayerAdded(LayerAddedEvent event) {
				if (event.getLayer() == layer) {
					refresh();
				}
			}
		});

		// When a layers visibility status changes, the rendering must change accordingly:
		eventBus.addLayerVisibilityHandler(new LayerVisibilityHandler() {

			@Override
			public void onVisibilityMarked(LayerVisibilityMarkedEvent event) {
			}

			@Override
			public void onShow(LayerShowEvent event) {
				if (container != null) {
					container.setVisible(true);
				}
				refresh();
			}

			@Override
			public void onHide(LayerHideEvent event) {
				if (container != null) {
					container.setVisible(false);
				}
			}
		}, layer);

		// Refresh the rendering when the style changes:
		eventBus.addLayerStyleChangedHandler(new LayerStyleChangedHandler() {

			@Override
			public void onLayerStyleChanged(LayerStyleChangedEvent event) {
				refresh();
			}
		}, layer);
	}

	// ------------------------------------------------------------------------
	// LayerRenderer implementation:
	// ------------------------------------------------------------------------

	@Override
	public Layer getLayer() {
		return layer;
	}

	@Override
	public void render(RenderingInfo renderingInfo) {
		this.queue = renderingInfo.getHintValue(MapPresenterImpl.QUEUE);
		if (!(renderingInfo.getWidget() instanceof HtmlContainer)) {
			throw new IllegalArgumentException("This implementation requires HtmlContainers to render.");
		}
		if (renderingInfo.getView() == null) {
			throw new IllegalArgumentException("No view is specified.");
		}
		setContainer((HtmlContainer) renderingInfo.getWidget());

		// We prepare the tiles that need to be loaded here. A tile level will only be rendered when all its tiles
		// are loaded (this avoids that tiles are rendered doubly (= at 2 levels) for transparent layers).
		// When a trajectory is busy, we render the closest level that is already loaded (normally the level
		// we were at when the trajectory was started).
		// TODO: make this simpler for the opaque case !!!
		try {
			// find the visible level
			int visibleLevel = -1;
			View view = renderingInfo.getView();
			if (renderingInfo.getTrajectory() != null && trajectory == null) {
				// we are entering a new trajectory, prepare the target view
				trajectory = renderingInfo.getTrajectory();
				targetRenderer = getRendererForView(trajectory.getView(1.0), queue);
				// add the tiles
				targetRenderer.render(renderingInfo, trajectory.getView(1.0));
			}
			if (trajectory != null) {
				// we are in a trajectory
				if (targetRenderer.isRendered(renderingInfo.getView())) {
					// we have all tiles, transform and show the target level
					transformView(targetRenderer, view.getResolution());
					visibleLevel = targetRenderer.getTileLevel();
					// make sure we never get back here !
					trajectory = null;
				} else {
					// find the rendered level that is closest
					View startView = trajectory.getView(0);
					int startLevel = getResolutionIndex(startView.getResolution());
					int tileLevel = getResolutionIndex(view.getResolution());
					// make sure we have a different level
					if (startLevel == tileLevel) {
						startLevel = startLevel + (startView.getResolution() > view.getResolution() ? -1 : 1);
					}
					// find the closest rendered level
					int step = (startLevel < tileLevel ? -1 : 1);
					for (int i = (tileLevel + step); i != (startLevel + step); i += step) {
						if (tileLevelRenderers.containsKey(i)) {
							TileLevelRenderer tr = tileLevelRenderers.get(i);
							if (tr.isRendered(startView)) {
								// closest rendered level, render this level instead
								// transform is sufficient, don't load any extra tiles
								transformView(tr, view.getResolution());
								visibleLevel = tr.getTileLevel();
								break;
							}
						}
					}
				}
			} 
			if (visibleLevel < 0) {
				// normal case, just try to render
				TileLevelRenderer renderer = getRendererForView(view, queue);
				// add the tiles
				renderer.render(renderingInfo, view);
				// and transform
				transformView(renderer, view.getResolution());
				visibleLevel = renderer.getTileLevel();
			}
			// show the level
			setVisible(visibleLevel);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "could not render view", e);
		}

	}

	// ------------------------------------------------------------------------
	// OpacitySupported implementation:
	// ------------------------------------------------------------------------

	public void setOpacity(double opacity) {
		container.setOpacity(opacity);
	}

	public double getOpacity() {
		return container.getOpacity();
	}

	// ------------------------------------------------------------------------
	// Public methods:
	// ------------------------------------------------------------------------

	/**
	 * Create a renderer for a certain fixed tile level.
	 * 
	 * @param tileLevel The tile level to create a new renderer for.
	 * @param view The view that will be initially visible on the tile level.
	 * @param container The container that has been created for the tile renderer to render in.
	 * @return Return the new tile renderer.
	 */
	public abstract TileLevelRenderer createNewScaleRenderer(int tileLevel, View view, HtmlContainer container);

	// ------------------------------------------------------------------------
	// Protected & private methods:
	// ------------------------------------------------------------------------

	protected int getResolutionIndex(double resolution) {
		if (layer instanceof TileBasedLayer) {
			return ((TileBasedLayer) layer).getTileConfiguration().getResolutionIndex(resolution);
		}
		return viewPort.getResolutionIndex(resolution);
	}

	protected double getResolution(int tileLevel) {
		try {
			if (layer instanceof TileBasedLayer) {
				return ((TileBasedLayer) layer).getTileConfiguration().getResolution(tileLevel);
			}
			return viewPort.getResolution(tileLevel);
		} catch (IllegalArgumentException iae) {
			return 0;
		}
	}

	protected int getResolutionCount() {
		if (layer instanceof TileBasedLayer) {
			return ((TileBasedLayer) layer).getTileConfiguration().getResolutionCount();
		}
		return viewPort.getResolutionCount();
	}

	protected TileLevelRenderer getRendererForView(View view, TileQueue queue) throws IllegalStateException {
		int tileLevel = getResolutionIndex(view.getResolution());
		// Do we have a renderer at the tileLevel that is rendered?
		TileLevelRenderer renderer = getOrCreateTileLevelRenderer(tileLevel, view, queue);
		return renderer;
	}

	protected TileLevelRenderer getOrCreateTileLevelRenderer(int tileLevel, View view, final TileQueue queue) {
		// Can we find it?
		if (tileLevelRenderers.containsKey(tileLevel)) {
			return tileLevelRenderers.get(tileLevel);
		}

		// If we can't find it, we create it:
		HtmlContainer tileLevelContainer = new HtmlGroup();
		tileLevelContainer.asWidget().getElement().setId("TileLevel-" + tileLevel);

		// Set origin:
		Matrix translation = viewPort.getTransformationService().getTranslationMatrix(view);
		tileLevelContainer.setOrigin(new Coordinate(translation.getDx(), translation.getDy()));

		TileLevelRenderer renderer = createNewScaleRenderer(tileLevel, view, tileLevelContainer);
		if (renderer == null) {
			throw new IllegalStateException("Cannot create a TileLevelRenderer for layer " + layer.getTitle());
		}
		container.insert(tileLevelContainer, 0);
		tileLevelRenderers.put(tileLevel, renderer);
		tileLevelContainers.put(tileLevel, tileLevelContainer);
		return renderer;
	}

	protected void transformView(TileLevelRenderer renderer, double currentResolution) {
		// Apply the correct transformation on the container:
		double rendererResolution = getResolution(renderer.getTileLevel());
		Matrix transformation = viewPort.getTransformationService().getTranslationMatrix(currentResolution);
		HtmlContainer tileLevelContainer = tileLevelContainers.get(renderer.getTileLevel());
		Coordinate origin = tileLevelContainer.getOrigin();

		// apply scale
		tileLevelContainer.applyScale(rendererResolution / currentResolution, 0, 0);

		// apply translation
		double left = transformation.getDx() - origin.getX() * tileLevelContainer.getScale();
		double top = transformation.getDy() - origin.getY() * tileLevelContainer.getScale();
		tileLevelContainer.setLeft((int) Math.round(left));
		tileLevelContainer.setTop((int) Math.round(top));
	}

	protected void setContainer(HtmlContainer container) {
		if (this.container == null || this.container != container) {
			this.container = container;
			DomService.applyTransition(this.container.asWidget().getElement(), new String[] { "opacity" },
					new Integer[] { 300 });
		}
	}
	
	protected void refresh() {
		clear();
		eventBus.fireEvent(new RenderMapEvent());
	}

	protected void clear() {
		targetRenderer = null;
		tileLevelRenderers.clear();
		tileLevelContainers.clear();
		if (container != null) {
			container.clear();
		}
	}

	protected void setVisible(int tileLevel) {
		// First set the correct level to visible, so as to make sure the map never gets white:
		tileLevelContainers.get(tileLevel).setVisible(true);

		// Now go over all containers (we could leave out the correct one here...):
		for (Entry<Integer, HtmlContainer> containerEntry : tileLevelContainers.entrySet()) {
			containerEntry.getValue().setVisible(tileLevel == containerEntry.getKey());
		}
	}
}