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

import java.util.Map;

import org.geomajas.geometry.Geometry;
import org.geomajas.gwt2.client.map.attribute.Attribute;
import org.geomajas.gwt2.client.map.layer.FeaturesSupported;

/**
 * Default implementation of the Feature interface. Represents the individual objects of vector layers.
 * 
 * @author Pieter De Graef
 */
public class FeatureImpl implements Feature {

	private final FeaturesSupported layer;

	private final String id;

	private final Map<String, Attribute<?>> attributes;

	private final Geometry geometry;

	private final String label;

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	FeatureImpl(FeaturesSupported layer, String id, Map<String, Attribute<?>> attributes, Geometry geometry,
			String label) {
		this.layer = layer;
		this.id = id;
		this.attributes = attributes;
		this.geometry = geometry;
		this.label = label;
	}

	// ------------------------------------------------------------------------
	// Feature implementation:
	// ------------------------------------------------------------------------

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Object getAttributeValue(String attributeName) {
		Attribute<?> attribute = attributes.get(attributeName);
		if (attribute != null) {
			return attribute.getValue();
		}
		return null;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Map<String, Attribute<?>> getAttributes() {
		return attributes;
	}

	@Override
	public Geometry getGeometry() {
		return geometry;
	}

	@Override
	public boolean isSelected() {
		return layer.isFeatureSelected(id);
	}

	@Override
	public FeaturesSupported getLayer() {
		return layer;
	}
}