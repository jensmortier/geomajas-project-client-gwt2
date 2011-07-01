/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.configuration.client;

import java.io.Serializable;

import org.geomajas.annotations.Api;
import org.geomajas.annotations.UserImplemented;

/**
 * Use this interface to define custom data classes to be passed to the client.
 * 
 * @see {@link ClientApplicationInfo}, {@link ClientMapInfo}, {@link ClientLayerInfo}
 * 
 * @author Kristof Heirwegh
 * @since 1.6.0
 */
@Api(allMethods = true)
@UserImplemented
public interface ClientUserDataInfo extends Serializable {

}
