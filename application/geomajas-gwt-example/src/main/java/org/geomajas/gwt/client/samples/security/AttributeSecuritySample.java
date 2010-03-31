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

package org.geomajas.gwt.client.samples.security;

import org.geomajas.command.CommandResponse;
import org.geomajas.command.dto.SearchFeatureRequest;
import org.geomajas.command.dto.SearchFeatureResponse;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.map.feature.Feature;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.samples.base.SamplePanel;
import org.geomajas.gwt.client.samples.base.SamplePanelFactory;
import org.geomajas.gwt.client.samples.i18n.I18nProvider;
import org.geomajas.gwt.client.widget.FeatureAttributeWindow;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.layer.feature.SearchCriterion;
import org.geomajas.plugin.springsecurity.client.Authentication;

import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * <p>
 * Sample that tests security on attribute level.
 * </p>
 * 
 * @author Pieter De Graef
 */
public class AttributeSecuritySample extends SamplePanel {

	public static final String TITLE = "AttributeSecurity";

	private MapWidget map;

	private VectorLayer layer;

	private FeatureAttributeWindow featureAttributeWindow;

	public static final SamplePanelFactory FACTORY = new SamplePanelFactory() {

		public SamplePanel createPanel() {
			return new AttributeSecuritySample();
		}
	};

	public Canvas getViewPanel() {
		final VLayout layout = new VLayout();
		layout.setMembersMargin(10);
		layout.setWidth100();
		layout.setHeight100();

		// Create horizontal layout for login buttons:
		HLayout buttonLayout = new HLayout();
		buttonLayout.setMembersMargin(10);
		buttonLayout.setHeight(20);

		// Map with ID duisburgMap is defined in the XML configuration. (mapDuisburg.xml)
		map = new MapWidget("beansMap", "gwt-samples");
		map.setVisible(false);
		layout.addMember(map);
		map.init();

		// Create login handler that re-initializes the map on a successful login:
		final BooleanCallback initMapCallback = new BooleanCallback() {

			public void execute(Boolean value) {
				if (value) {
					map.destroy();
					map = new MapWidget("beansMap", "gwt-samples");
					map.setVisible(false);
					layout.addMember(map);
					map.init();
				}
			}
		};

		// Create a button that logs in user "elvis":
		IButton loginButtonMarino = new IButton(I18nProvider.getSampleMessages().securityLogInWith("elvis"));
		loginButtonMarino.setWidth(150);
		loginButtonMarino.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Authentication.getInstance().login("elvis", "elvis", initMapCallback);
				if (null != featureAttributeWindow) {
					featureAttributeWindow.destroy();
					featureAttributeWindow = null;
				}
			}
		});
		buttonLayout.addMember(loginButtonMarino);

		// Create a button that logs in user "luc":
		IButton loginButtonLuc = new IButton(I18nProvider.getSampleMessages().securityLogInWith("luc"));
		loginButtonLuc.setWidth(150);
		loginButtonLuc.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Authentication.getInstance().login("luc", "luc", initMapCallback);
				if (null != featureAttributeWindow) {
					featureAttributeWindow.destroy();
					featureAttributeWindow = null;
				}
			}
		});
		buttonLayout.addMember(loginButtonLuc);

		// Set up the search command, that will fetch a feature:
		// Searches for ID=1, but we might as well have created a filter on one of the attributes...
		SearchFeatureRequest request = new SearchFeatureRequest();
		request.setBooleanOperator("AND");
		request.setCrs("EPSG:900913"); // Can normally be acquired from the MapModel.
		request.setLayerId("beans");
		request.setMax(1);
		request.setCriteria(new SearchCriterion[] { new SearchCriterion("id", "=", "1") });
		final GwtCommand command = new GwtCommand("command.feature.Search");
		command.setCommandRequest(request);

		// Create a button that executes the search command:
		IButton editFeatureButton = new IButton(I18nProvider.getSampleMessages().attributeSecurityButtonTitle());
		editFeatureButton.setWidth(200);
		editFeatureButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				layer = (VectorLayer) map.getMapModel().getLayer("beansLayer");
				GwtCommandDispatcher.getInstance().execute(command, new CommandCallback() {

					public void execute(CommandResponse response) {
						if (response instanceof SearchFeatureResponse) {
							SearchFeatureResponse resp = (SearchFeatureResponse) response;
							for (org.geomajas.layer.feature.Feature dtoFeature : resp.getFeatures()) {
								Feature feature = new Feature(dtoFeature, layer);
								if (null != featureAttributeWindow) {
									featureAttributeWindow.destroy();
									featureAttributeWindow = null;
								}
								featureAttributeWindow = new FeatureAttributeWindow(feature, true);
								featureAttributeWindow.setWidth(400);
								layout.addMember(featureAttributeWindow);
							}
						}
					}
				});
			}
		});

		layout.addMember(buttonLayout);
		layout.addMember(editFeatureButton);
		return layout;
	}

	public String getDescription() {
		return I18nProvider.getSampleMessages().attributeSecurityDescription();
	}

	public String getSourceFileName() {
		return "classpath:org/geomajas/gwt/client/samples/security/AttributeSecuritySample.txt";
	}

	public String[] getConfigurationFiles() {
		return new String[] { "classpath:org/geomajas/gwt/samples/security/security.xml",
				"classpath:org/geomajas/gwt/samples/mapwidget/layerBeans.xml",
				"classpath:org/geomajas/gwt/samples/mapwidget/mapBeans.xml" };
	}

	public String ensureUserLoggedIn() {
		return "luc";
	}
}
