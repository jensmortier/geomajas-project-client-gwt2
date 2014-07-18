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
package org.geomajas.plugin.print.configuration;

import java.io.IOException;
import java.util.List;

/**
 * Dao for accessing print templates.
 *
 * @author Jan De Moerloose
 */
public interface PrintTemplateDao {

	List<PrintTemplate> findAll() throws IOException;
	
	void merge(PrintTemplate template) throws IOException;
}