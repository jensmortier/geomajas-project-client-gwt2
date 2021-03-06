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

package org.geomajas.gwt2.client.map.feature;

import java.util.List;
import java.util.Map;

import org.geomajas.gwt2.client.map.layer.FeaturesSupported;

/**
 * Call-back interface for implementing methods on lists of features per layer.
 * 
 * @author Pieter De Graef
 */
public interface FeatureMapFunction {

	/**
	 * Do something with the provided feature map.
	 * 
	 * @param featureMap
	 *            Map of features per layer.
	 */
	void execute(Map<FeaturesSupported, List<Feature>> featureMap);
}