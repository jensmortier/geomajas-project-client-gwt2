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

package org.geomajas.gwt2.plugin.corewidget.example.client.sample.feature.featureinfo.control;

/**
 * View factory for the
 * {@link FeatureInfoControl}.
 *
 * @author Youri Flement
 */
public class FeatureInfoControlViewFactory {

	public FeatureInfoControlView createFeatureInfoControlView() {
		return new FeatureInfoControlViewImpl();
	}

}
