/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geomajas.plugin.geocoder.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import org.geomajas.command.CommandResponse;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.map.MapView;
import org.geomajas.gwt.client.spatial.Bbox;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.plugin.geocoder.client.event.SelectAlternativeEvent;
import org.geomajas.plugin.geocoder.client.event.SelectAlternativeHandler;
import org.geomajas.plugin.geocoder.client.event.SelectLocationEvent;
import org.geomajas.plugin.geocoder.client.event.SelectLocationHandler;
import org.geomajas.plugin.geocoder.command.dto.GetLocationForStringAlternative;
import org.geomajas.plugin.geocoder.command.dto.GetLocationForStringRequest;
import org.geomajas.plugin.geocoder.command.dto.GetLocationForStringResponse;

import java.util.List;

/**
 * Widget for starting a geocoder location search.
 *
 * @author Joachim Van der Auwera
 */
public class GeocoderPresenter implements SelectLocationHandler, SelectAlternativeHandler {

	private static final String COMMAND = "command.geocoder.GetLocationForString";
	private MapWidget map;
	private GeocoderWidget geocoderWidget;
	private Window altWindow;
	private GeocoderAlternativesGrid altGrid;
	private String servicePattern = ".*";
	private GeocoderMessages messages = GWT.create(GeocoderMessages.class);
	private HandlerManager handlerManager;

	/**
	 * Create geocoder widget which allows searching a location from a string.
	 *
	 * @param map map to apply search results
	 * @param geocoderWidget geocoder widget
	 */
	GeocoderPresenter(MapWidget map, GeocoderWidget geocoderWidget) {

		this.map = map;
		this.geocoderWidget = geocoderWidget;

		handlerManager = new HandlerManager(this);
		setSelectAlternativeHandler(this);
		setSelectLocationHandler(this);
	}

	public void clearLocation() {
		geocoderWidget.setValue("");
	}

	public void goToLocation(final String location) {
		GwtCommand command = new GwtCommand(COMMAND);
		GetLocationForStringRequest request = new GetLocationForStringRequest();
		request.setCrs(map.getMapModel().getCrs());
		request.setLocation(location);
		request.setServicePattern(servicePattern);
		if (GWT.isClient()) {
			// causes NPE when run as junit test
			String locale = LocaleInfo.getCurrentLocale().getLocaleName();
			if (!"default".equals(locale)) {
				request.setLocale(locale);
			}
		}
		command.setCommandRequest(request);
		GwtCommandDispatcher.getInstance().execute(command, new CommandCallback() {

			public void execute(CommandResponse commandResponse) {
				goToLocation(commandResponse, location);
			}
		});
	}

	public void goToLocation(final CommandResponse commandResponse, final String location) {
		if (commandResponse instanceof GetLocationForStringResponse) {
			GetLocationForStringResponse response = (GetLocationForStringResponse) commandResponse;
			if (response.isLocationFound()) {
				removeAltWindow();
				handlerManager.fireEvent(new SelectLocationEvent(map, response));
			} else {
				List<GetLocationForStringAlternative> alternatives = response.getAlternatives();
				if (null != alternatives && alternatives.size() > 0) {
					handlerManager.fireEvent(new SelectAlternativeEvent(map, alternatives));
				} else {
					SC.say(messages.locationNotFound(location));
				}
			}
		}
	}

	private void removeAltWindow() {
		if (null != altWindow) {
			altWindow.destroy();
			altWindow = null;
		}
	}

	/**
	 * Get the regular expression which is used to select which geocoder services to use.
	 *
	 * @return geocoder selection regular expression
	 */
	public String getServicePattern() {
		return servicePattern;
	}

	/**
	 * Set the regular expression which is used to select which geocoder services to use.
	 *
	 * @param servicePattern geocoder selection regular expression
	 */
	public void setServicePattern(String servicePattern) {
		this.servicePattern = servicePattern;
	}

	/**
	 * Set the select alternative handler.
	 * <p/>
	 * There can only be one handler, the default displays the alternatives in a window on the map widget.
	 *
	 * @param handler select alternative handler
	 * @return handler registration.
	 */
	public HandlerRegistration setSelectAlternativeHandler(SelectAlternativeHandler handler) {
		if (handlerManager.getHandlerCount(SelectAlternativeHandler.TYPE) > 0) {
			SelectAlternativeHandler previous = handlerManager.getHandler(SelectAlternativeHandler.TYPE, 0);
			handlerManager.removeHandler(SelectAlternativeHandler.TYPE, previous);
		}
		return handlerManager.addHandler(SelectAlternativeHandler.TYPE, handler);
	}

	/**
	 * Set the select location handler.
	 * <p/>
	 * There can only be one handler, the default zooms the map widget to the selected location.
	 *
	 * @param handler select location handler
	 * @return handler registration.
	 */
	public HandlerRegistration setSelectLocationHandler(SelectLocationHandler handler) {
		if (handlerManager.getHandlerCount(SelectLocationHandler.TYPE) > 0) {
			SelectLocationHandler previous = handlerManager.getHandler(SelectLocationHandler.TYPE, 0);
			handlerManager.removeHandler(SelectLocationHandler.TYPE, previous);
		}
		return handlerManager.addHandler(SelectLocationHandler.TYPE, handler);
	}

	// @extract-start DefaultSelectAlternative, Default implementation for SelectAlternativeHandler
	public void onSelectAlternative(SelectAlternativeEvent event) {
		if (null == altWindow) {
			altGrid = new GeocoderAlternativesGrid(geocoderWidget, event.getAlternatives());

			altWindow = new Window();
			altWindow.setAutoSize(true);
			altWindow.setTitle(messages.alternativeSelectTitle());
			altWindow.setAutoSize(true);
			altWindow.setLeft(20);
			altWindow.setTop(20);
			altWindow.setCanDragReposition(true);
			altWindow.setCanDragResize(true);
			altWindow.addItem(altGrid);
			altWindow.addCloseClickHandler(new CloseClickHandler() {
				public void onCloseClick(CloseClientEvent closeClientEvent) {
					removeAltWindow();
				}
			});

			map.addChild(altWindow);
		} else {
			altGrid.update(event.getAlternatives());
		}
	}
	// @extract-end

	// @extract-start DefaultSelectLocation, Default implementation for SelectLocationHandler
	public void onSelectLocation(SelectLocationEvent event) {
		org.geomajas.geometry.Bbox bbox = event.getBbox();
		map.getMapModel().getMapView().applyBounds(new Bbox(bbox), MapView.ZoomOption.LEVEL_FIT);
		geocoderWidget.setValue(event.getCanonicalLocation());
	}
	// @extract-end

	/**
	 * Fire an event.
	 *
	 * @param event event to fire
	 */
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}
}
