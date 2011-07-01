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
package org.geomajas.layer.feature.attribute;

import org.geomajas.annotations.Api;
import org.geomajas.configuration.PrimitiveType;

/**
 * Attribute with value of type <code>PrimitiveType.SHORT</code>.
 * 
 * @author Jan De Moerloose
 * @since 1.6.0
 */
@Api(allMethods = true)
public class ShortAttribute extends PrimitiveAttribute<Short> {

	private static final long serialVersionUID = 151L;

	/**
	 * Create attribute without value (needed for GWT).
	 */
	public ShortAttribute() {
		this(null);
	}

	/**
	 * Create attribute with specified value.
	 * 
	 * @param value
	 *            value for attribute
	 */
	public ShortAttribute(Short value) {
		super(PrimitiveType.SHORT);
		setValue(value);
	}

	/**
	 * Create a clone of this attribute object.
	 * 
	 * @since 1.7.0
	 * @return A copy of this short attribute.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "CN_IDIOM_NO_SUPER_CALL",
			justification = "needed for GWT")
	public Object clone() {
		ShortAttribute clone = new ShortAttribute();
		if (getValue() != null) {
			clone.setValue(Short.valueOf(getValue()));
		}
		clone.setEditable(isEditable());
		return clone;
	}
}
