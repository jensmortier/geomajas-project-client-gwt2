/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.smartgwt.example.client.sample.toolbar;

import com.google.gwt.core.client.GWT;
import org.geomajas.smartgwt.client.util.WidgetLayout;
import org.geomajas.smartgwt.example.base.SamplePanel;
import org.geomajas.smartgwt.example.base.SamplePanelFactory;
import org.geomajas.smartgwt.client.widget.MapWidget;
import org.geomajas.smartgwt.client.widget.Toolbar;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;
import org.geomajas.smartgwt.example.client.sample.i18n.SampleMessages;

/**
 * <p>
 * Sample that shows how a toolbar can be added to the map. The toolbar contains some buttons the user can use to
 * navigate the map (zoom, pan, zoom to rectangle)
 * </p>
 * 
 * @author Frank Wynants
 */
public class ToolbarNavigationSample extends SamplePanel {

	private static final SampleMessages MESSAGES = GWT.create(SampleMessages.class);

	public static final String TITLE = "ToolbarNavigation";

	public static final SamplePanelFactory FACTORY = new SamplePanelFactory() {

		public SamplePanel createPanel() {
			return new ToolbarNavigationSample();
		}
	};

	/**
	 * @return The viewPanel Canvas
	 */
	public Canvas getViewPanel() {
		VLayout layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();

		// Map with ID osmNavigationToolbarMap is defined in the XML configuration.
		final MapWidget map = new MapWidget("mapOsmNavigationToolbar", "gwtExample");

		final Toolbar toolbar = new Toolbar(map);
		toolbar.setButtonSize(WidgetLayout.toolbarLargeButtonSize);

		layout.addMember(toolbar);
		layout.addMember(map);
		return layout;
	}

	public String getDescription() {
		return MESSAGES.toolbarNavigationDescription();
	}

	public String[] getConfigurationFiles() {
		return new String[] {
				"classpath:org/geomajas/smartgwt/example/context/mapNavigation.xml",
				"classpath:org/geomajas/smartgwt/example/base/layerOsm.xml" };
	}

	public String ensureUserLoggedIn() {
		return "luc";
	}
}