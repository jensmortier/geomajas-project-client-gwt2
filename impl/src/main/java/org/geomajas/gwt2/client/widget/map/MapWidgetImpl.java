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

package org.geomajas.gwt2.client.widget.map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.GestureChangeEvent;
import com.google.gwt.event.dom.client.GestureChangeHandler;
import com.google.gwt.event.dom.client.GestureEndEvent;
import com.google.gwt.event.dom.client.GestureEndHandler;
import com.google.gwt.event.dom.client.GestureStartEvent;
import com.google.gwt.event.dom.client.GestureStartHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import org.geomajas.gwt2.client.GeomajasImpl;
import org.geomajas.gwt2.client.gfx.CanvasContainer;
import org.geomajas.gwt2.client.gfx.CanvasContainerImpl;
import org.geomajas.gwt2.client.gfx.TransformableWidgetContainer;
import org.geomajas.gwt2.client.gfx.TransformableWidgetContainerImpl;
import org.geomajas.gwt2.client.gfx.VectorContainer;
import org.geomajas.gwt2.client.gfx.VectorGroup;
import org.geomajas.gwt2.client.map.MapPresenterImpl.MapWidget;
import org.geomajas.gwt2.client.map.render.dom.container.HtmlContainer;
import org.geomajas.gwt2.client.map.render.dom.container.HtmlGroup;
import org.geomajas.gwt2.client.widget.control.watermark.Watermark;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.Transformable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p> Implementation of the MapWidget interface. It represents the MVP 'view' of the map's presenter (aka
 * MapPresenter). </p> <p> This widget is able to render all required objects that the MapPresenter supports, and does
 * this in the following order: <ol> <li>Raster layers & rasterized vector layers.</li> <li>Vector layers (SVG/VML)</li>
 * <li>All vectorcontainers</li> <li>All map gadgets</li> </ol> </p>
 *
 * @author Pieter De Graef
 * @author Jan De Moerloose
 */
public final class MapWidgetImpl extends AbsolutePanel implements MapWidget {

	// Container for raster layers or rasterized layers:
	private HtmlGroup layerHtmlContainer;

	// Container for vector layers (SVG/VML):
	private VectorGroup layerVectorContainer;

	// Parent container for all SVG/VML:
	private DrawingArea drawingArea;

	// Parent container for all canvases:
	private AbsolutePanel canvasPanel;

	// Parent container for all transformable widget containers
	private FlowPanel widgetPanel;

	// List of all screen containers:
	private List<VectorContainer> screenContainers = new ArrayList<VectorContainer>();

	// List of all world containers:
	private List<VectorContainer> worldContainers = new ArrayList<VectorContainer>();

	// List of all world canvas containers:
	private List<CanvasContainer> worldCanvases = new ArrayList<CanvasContainer>();

	// List of all widget containers:
	private List<TransformableWidgetContainer> widgetContainers = new ArrayList<TransformableWidgetContainer>();

	// List of all world transformables (canvas + vector):
	private List<Transformable> worldTransformables = new ArrayList<Transformable>();

	private final List<Widget> widgets = new ArrayList<Widget>();

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	public MapWidgetImpl() {
		super();

		// Attach an HtmlContainer inside the clipping area (used for rendering layers):
		layerHtmlContainer = new HtmlGroup();
		layerHtmlContainer.asWidget().getElement().getStyle().setZIndex(0);
		add(layerHtmlContainer, 0, 0);

		// Add a panel to hold the canvases (this should come before vectors or it catches all events !)
		canvasPanel = new AbsolutePanel();
		add(canvasPanel, 0, 0);

		// Attach a DrawingArea inside the clipping area (used for vector rendering):
		drawingArea = new DrawingArea(100, 100);
		add(drawingArea, 0, 0);

		// First child within the vector drawing area is a group for the map to render it's non-HTML layers:
		layerVectorContainer = new VectorGroup();
		drawingArea.add(layerVectorContainer);

		widgetPanel = new FlowPanel();
		add(widgetPanel, 0, 0);

		// Firefox and Chrome allow for DnD of images. This default behavior is not wanted.
		addMouseDownHandler(new MouseDownHandler() {

			public void onMouseDown(MouseDownEvent event) {
				event.preventDefault();
			}
		});
		addMouseMoveHandler(new MouseMoveHandler() {

			public void onMouseMove(MouseMoveEvent event) {
				event.preventDefault();
			}
		});

		// We don't want scrolling on the page and zooming at the same time.
		// TODO: make this optional. When no zoom on scroll is used, scrolling the page should be possible.
		addMouseWheelHandler(new MouseWheelHandler() {

			public void onMouseWheel(MouseWheelEvent event) {
				event.preventDefault();
			}
		});

		// Apply the correct style name:
		MapWidgetResource resource = GeomajasImpl.getClientBundleFactory().createMapWidgetResource();
		resource.css().ensureInjected();
		this.setStyleName(resource.css().mapBackground());
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public HtmlContainer getMapHtmlContainer() {
		return layerHtmlContainer;
	}

	@Override
	public List<Transformable> getWorldTransformables() {
		return worldTransformables;
	}

	@Override
	public VectorContainer getNewScreenContainer() {
		VectorGroup container = new VectorGroup();
		drawingArea.add(container);
		screenContainers.add(container);
		return container;
	}

	@Override
	public VectorContainer getNewWorldContainer() {
		VectorGroup container = new VectorGroup();
		drawingArea.add(container);
		worldContainers.add(container);
		worldTransformables.add(container);
		return container;
	}

	@Override
	public CanvasContainer getNewWorldCanvas() {
		CanvasContainer container = new CanvasContainerImpl(getWidth(), getHeight());
		canvasPanel.add(container, 0, 0);
		worldCanvases.add(container);
		worldTransformables.add(container);
		return container;
	}

	@Override
	public TransformableWidgetContainer getNewWorldWidgetContainer() {
		TransformableWidgetContainerImpl container = new TransformableWidgetContainerImpl();
		widgetPanel.add(container);
		widgetContainers.add(container);
		worldTransformables.add(container);
		return container;
	}

	@Override
	public boolean removeVectorContainer(VectorContainer container) {
		if (container instanceof Group) {
			if (worldContainers.contains(container)) {
				drawingArea.remove((Group) container);
				worldContainers.remove(container);
				worldTransformables.remove(container);
				return true;
			} else if (screenContainers.contains(container)) {
				drawingArea.remove((Group) container);
				screenContainers.remove(container);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeWorldWidgetContainer(TransformableWidgetContainer container) {
		if (worldContainers.contains(container)) {
			widgetPanel.remove(container);
			widgetContainers.remove(container);
			worldTransformables.remove(container);
			return true;
		}
		return false;
	}

	@Override
	public boolean bringToFront(VectorContainer container) {
		if (container instanceof Group) {
			if (worldContainers.contains(container)) {
				drawingArea.bringToFront((Group) container);
				return true;
			} else if (screenContainers.contains(container)) {
				drawingArea.bringToFront((Group) container);
				return true;
			}
		}
		return false;
	}

	@Override
	public HasWidgets getWidgetContainer() {
		return new HasWidgets() {

			@Override
			public void add(Widget w) {
				w.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
				MapWidgetImpl.this.add(w);
				if (!(w instanceof Watermark)) {
					widgets.add(w);
				}
			}

			@Override
			public void clear() {
				while (widgets.size() > 0) {
					remove(widgets.get(0));
				}
			}

			@Override
			public Iterator<Widget> iterator() {
				return widgets.iterator();
			}

			@Override
			public boolean remove(Widget w) {
				widgets.remove(w);
				return MapWidgetImpl.this.remove(w);
			}
		};
	}

	@Override
	public void onResize() {
		for (Widget child : getChildren()) {
			if (child instanceof RequiresResize) {
				((RequiresResize) child).onResize();
			}
		}
	}

	// ------------------------------------------------------------------------
	// Overriding resize methods:
	// ------------------------------------------------------------------------

	public void setPixelSize(int width, int height) {
		layerHtmlContainer.setPixelSize(width, height);
		drawingArea.setWidth(width);
		drawingArea.setHeight(height);
		canvasPanel.setPixelSize(width, height);
		for (CanvasContainer container : worldCanvases) {
			container.setPixelSize(width, height);
		}
		super.setPixelSize(width, height);
	}

	public void setSize(String width, String height) {
		layerHtmlContainer.setSize(width, height);
		drawingArea.setWidth(width);
		drawingArea.setHeight(height);
		canvasPanel.setWidth(width);
		canvasPanel.setHeight(height);
		for (CanvasContainer container : worldCanvases) {
			container.setPixelSize(drawingArea.getWidth(), drawingArea.getHeight());
		}
		super.setSize(width, height);
	}

	public int getWidth() {
		return getOffsetWidth();
	}

	public void setWidth(int width) {
		setWidth(width + "px");
	}

	public void setWidth(String width) {
		layerHtmlContainer.setWidth(width);
		drawingArea.setWidth(width);
		canvasPanel.setWidth(width);
		for (CanvasContainer container : worldCanvases) {
			container.setPixelSize(drawingArea.getWidth(), drawingArea.getHeight());
		}
		super.setWidth(width);
	}

	public int getHeight() {
		return getOffsetHeight();
	}

	public void setHeight(int height) {
		setHeight(height + "px");
	}

	public void setHeight(String height) {
		layerHtmlContainer.setHeight(height);
		drawingArea.setHeight(height);
		canvasPanel.setHeight(height);
		for (CanvasContainer container : worldCanvases) {
			container.setPixelSize(drawingArea.getWidth(), drawingArea.getHeight());
		}
		super.setHeight(height);
	}

	// ------------------------------------------------------------------------
	// Add mouse event catch methods:
	// ------------------------------------------------------------------------

	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return addDomHandler(handler, MouseDownEvent.getType());
	}

	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		return addDomHandler(handler, MouseUpEvent.getType());
	}

	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}

	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());

	}

	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return addDomHandler(handler, MouseMoveEvent.getType());
	}

	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
		return addDomHandler(handler, MouseWheelEvent.getType());
	}

	public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
		return addDomHandler(handler, DoubleClickEvent.getType());
	}

	// ------------------------------------------------------------------------
	// Touch event catch methods:
	// ------------------------------------------------------------------------

	@Override
	public HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
		return addDomHandler(handler, TouchStartEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
		return addDomHandler(handler, TouchEndEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchCancelHandler(TouchCancelHandler handler) {
		return addDomHandler(handler, TouchCancelEvent.getType());
	}

	@Override
	public HandlerRegistration addTouchMoveHandler(TouchMoveHandler handler) {
		return addDomHandler(handler, TouchMoveEvent.getType());
	}

	// ------------------------------------------------------------------------
	// Gesture event catch methods:
	// ------------------------------------------------------------------------
	@Override
	public HandlerRegistration addGestureStartHandler(GestureStartHandler handler) {
		return addDomHandler(handler, GestureStartEvent.getType());
	}

	@Override
	public HandlerRegistration addGestureChangeHandler(GestureChangeHandler handler) {
		return addDomHandler(handler, GestureChangeEvent.getType());
	}

	@Override
	public HandlerRegistration addGestureEndHandler(GestureEndHandler handler) {
		return addDomHandler(handler, GestureEndEvent.getType());
	}
}