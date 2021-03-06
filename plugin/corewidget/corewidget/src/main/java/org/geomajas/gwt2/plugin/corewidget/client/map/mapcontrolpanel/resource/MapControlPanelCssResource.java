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

package org.geomajas.gwt2.plugin.corewidget.client.map.mapcontrolpanel.resource;


import com.google.gwt.resources.client.CssResource;
import org.geomajas.annotation.Api;

/**
 * CSS resource bundle for {@link org.geomajas.gwt2.plugin.corewidget.client.map.mapcontrolpanel.MapControlPanel}.
 *
 * @author Dosi Bingov
 * @since 2.1.0
 */
@Api(allMethods = true)
public interface MapControlPanelCssResource extends CssResource {

	/**
	 * The general style for the MapControlPanel widget.
	 * @return The general style for the MapControlPanel widget.
	 */
	@ClassName("gm-mapControlPanel")
	String mapControlPanel();
}
