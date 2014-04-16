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

package org.geomajas.gwt2.client.map.render.canvas;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.geomajas.gwt2.client.map.View;
import org.geomajas.gwt2.client.map.ViewPort;
import org.geomajas.gwt2.client.map.layer.AbstractTileBasedLayer;
import org.geomajas.gwt2.client.map.layer.Layer;
import org.geomajas.gwt2.client.map.layer.tile.TileBasedLayer;
import org.geomajas.gwt2.client.map.layer.tile.TileConfiguration;
import org.geomajas.gwt2.client.map.render.RenderMapEvent;
import org.geomajas.gwt2.client.map.render.RenderingInfo;
import org.geomajas.gwt2.client.map.render.TileLevelLayerRenderer;
import org.geomajas.gwt2.client.map.render.TileLevelRenderedEvent;
import org.geomajas.gwt2.client.map.render.TileLevelRenderedHandler;
import org.geomajas.gwt2.client.map.render.TileLevelRenderer;
import org.geomajas.gwt2.client.map.render.TileRenderer;
import org.geomajas.gwt2.client.map.render.canvas.container.CanvasGroup;
import org.geomajas.gwt2.client.map.render.canvas.container.CanvasTileGrid;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Canvas-based layer renderer implementation for layers that use renderers at fixed scales.
 * 
 * @author Jan De Moerloose
 */
public abstract class CanvasTileLevelLayerRenderer implements TileLevelLayerRenderer {

	private final ViewPort viewPort;

	private final Layer layer;

	private final Map<Integer, TileLevelRenderer> tileLevelRenderers;

	private final Map<Integer, CanvasTileGrid> tileLevelContainers;

	private CanvasGroup container;

	private TileLevelRenderer currentRenderer;

	private TileLevelRenderer targetRenderer;

	private int cacheSize = 3;

	private MapEventBus eventBus;

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	public CanvasTileLevelLayerRenderer(final ViewPort viewPort, final Layer layer, final MapEventBus eventBus) {
		this.viewPort = viewPort;
		this.layer = layer;
		this.eventBus = eventBus;
		this.tileLevelRenderers = new HashMap<Integer, TileLevelRenderer>();
		this.tileLevelContainers = new HashMap<Integer, CanvasTileGrid>();

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
				}
			}

			@Override
			public void onLayerAdded(LayerAddedEvent event) {
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
					render(new RenderingInfo(container, viewPort.getView(), null));
				}
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
		if (container == null) {
			if (!(renderingInfo.getWidget() instanceof Canvas)) {
				throw new IllegalArgumentException("This implementation requires Canvas to render.");
			}
			if (renderingInfo.getView() == null) {
				throw new IllegalArgumentException("No view is specified.");
			}
			setContainer(new CanvasGroup((Canvas) renderingInfo.getWidget()));
		}

		// Prepare the target view. Try to make sure it's rendered when the animation arrives there:
		View targetView = renderingInfo.getView();
		if (renderingInfo.getTrajectory() != null) {
			targetView = renderingInfo.getTrajectory().getView(1.0);
		}
		try {
			prepareView(container, targetView);
		} catch (Exception e) {
		}

		// Now render the current view:
		try {
			TileLevelRenderer renderer = getRendererForView(renderingInfo.getView());
			renderTileLevel(renderer, renderingInfo.getView());
			container.render(viewPort);
			cleanupCache();
		} catch (Exception e) {
		}
	}

	// ------------------------------------------------------------------------
	// OpacitySupported implementation:
	// ------------------------------------------------------------------------

	public void setOpacity(double opacity) {
		container.setOpacity(opacity);
		container.render(viewPort);
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
	public abstract TileLevelRenderer createNewScaleRenderer(int tileLevel, View view, CanvasTileGrid container);

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

	protected TileConfiguration getTileConfiguration() {
		if (layer instanceof TileBasedLayer) {
			return ((TileBasedLayer) layer).getTileConfiguration();
		} else {
			return null;
		}
	}

	protected int getResolutionCount() {
		if (layer instanceof TileBasedLayer) {
			return ((TileBasedLayer) layer).getTileConfiguration().getResolutionCount();
		}
		return viewPort.getResolutionCount();
	}

	protected TileLevelRenderer getRendererForView(View view) throws IllegalStateException {
		int tileLevel = getResolutionIndex(view.getResolution());

		// Do we have a renderer at the tileLevel that is rendered?
		TileLevelRenderer renderer = getOrCreateTileLevelRenderer(tileLevel, view);
		if (currentRenderer == null || renderer.isRendered(view)) {
			return renderer;
		}
		return currentRenderer;
	}

	protected void prepareView(IsWidget widget, View targetView) {
		// Given a trajectory, try to fetch the target tiles before rendering.
		int tileLevel = getResolutionIndex(targetView.getResolution());
		if (tileLevel < getResolutionCount()) {
			targetRenderer = getOrCreateTileLevelRenderer(tileLevel, targetView);
			targetRenderer.render(targetView);
		}
	}

	protected TileLevelRenderer getOrCreateTileLevelRenderer(int tileLevel, View view) {
		// Can we find it?
		if (tileLevelRenderers.containsKey(tileLevel)) {
			return tileLevelRenderers.get(tileLevel);
		}

		// If we can't find it, we create it:
		CanvasTileGrid tileLevelContainer = new CanvasTileGrid(getTileConfiguration(), getTileRenderer(), tileLevel);
		tileLevelContainer.setCallback(new Callback<String, String>() {

			@Override
			public void onFailure(String reason) {
			}

			@Override
			public void onSuccess(String result) {
				eventBus.fireEvent(new RenderMapEvent());				
			}
		});
		TileLevelRenderer renderer = createNewScaleRenderer(tileLevel, view, tileLevelContainer);
		if (renderer == null) {
			throw new IllegalStateException("Cannot create a TileLevelRenderer for layer " + layer.getTitle());
		}
		renderer.addTileLevelRenderedHandler(new TileLevelRenderedHandler() {

			@Override
			public void onTileLevelRendered(TileLevelRenderedEvent event) {
				TileLevelRenderer renderer = event.getRenderer();

				// See if we can replace the current renderer with the one that just rendered:
				int viewPortTileLevel = getResolutionIndex(viewPort.getResolution());
				if (renderer.getTileLevel() == viewPortTileLevel) {
					if (!renderer.isRendered(viewPort.getView())) {
						// TODO are we sure about this? Why else did we prepare this view?
						prepareView(tileLevelContainers.get(viewPortTileLevel), viewPort.getView());
					}

					// Render this tile level:
					renderTileLevel(renderer, viewPort.getView());
				}
			}
		});
		// container.insert(tileLevelContainer, 0);
		tileLevelRenderers.put(tileLevel, renderer);
		tileLevelContainers.put(tileLevel, tileLevelContainer);
		container.add(tileLevelContainer);
		return renderer;
	}

	private TileRenderer getTileRenderer() {
		return ((AbstractTileBasedLayer) layer).getTileRenderer();
	}

	protected void renderTileLevel(TileLevelRenderer renderer, View view) {
		// Set the current renderer:
		currentRenderer = renderer;
		setVisible(renderer.getTileLevel());
		renderer.render(view);
	}

	protected void setContainer(CanvasGroup container) {
		if (this.container == null || this.container != container) {
			this.container = container;
			// DomService.applyTransition(this.container.asWidget().getElement(), new String[] { "opacity" },
			// new Integer[] { 300 });
		}
	}

	protected void setVisible(int tileLevel) {
		// First set the correct level to visible, so as to make sure the map never gets white:
		tileLevelContainers.get(tileLevel).setVisible(true);

		// Now go over all containers (we could leave out the correct one here...):
		for (Entry<Integer, CanvasTileGrid> containerEntry : tileLevelContainers.entrySet()) {
			containerEntry.getValue().setVisible(tileLevel == containerEntry.getKey());
		}
	}

	protected void refresh() {
		clear();
		render(new RenderingInfo(container, viewPort.getView(), null));
	}

	private void clear() {
		currentRenderer = null;
		tileLevelRenderers.clear();
		tileLevelContainers.clear();
		if (container != null) {
			container.clear();
		}
	}

	private void cleanupCache() {
		while (tileLevelRenderers.size() > cacheSize) {
			int distance = -1;
			int tileLevel = -1;
			for (TileLevelRenderer renderer : tileLevelRenderers.values()) {
				if (renderer != currentRenderer && renderer != targetRenderer) {
					int d;
					if (targetRenderer != null) {
						d = Math.abs(targetRenderer.getTileLevel() - renderer.getTileLevel());
					} else {
						d = Math.abs(currentRenderer.getTileLevel() - renderer.getTileLevel());
					}
					if (d > distance) {
						distance = d;
						tileLevel = renderer.getTileLevel();
					}
				}
			}
			if (tileLevel < 0) {
				return;
			}
			removeTileLevel(tileLevel);
		}
	}

	private void removeTileLevel(int tileLevel) {
		if (container != null) {
			CanvasTileGrid toRemove = tileLevelContainers.get(tileLevel);
			container.remove(toRemove);
		}
		tileLevelRenderers.remove(tileLevel);
		tileLevelContainers.remove(tileLevel);
	}
}