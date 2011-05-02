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

package org.geomajas.internal.layer.vector.lazy;

import org.geomajas.layer.LayerException;
import org.geomajas.layer.feature.Attribute;
import org.geomajas.layer.feature.FeatureModel;
import org.geomajas.layer.feature.attribute.AssociationValue;
import org.geomajas.layer.feature.attribute.OneToManyAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lazy variant of a {@link OneToManyAttribute}. The value is only converted at first use, not at instantiation.
 *
 * @author Joachim Van der Auwera
 */
public class LazyOneToManyAttribute extends OneToManyAttribute implements LazyAttribute<List<AssociationValue>> {

	private static final long serialVersionUID = 190L;

	private FeatureModel featureModel;
	private Object pojo;
	private String name;
	private boolean gotValue;

	public LazyOneToManyAttribute(FeatureModel featureModel, Object pojo, String attribute) {
		super();
		this.featureModel = featureModel;
		this.pojo = pojo;
		this.name = attribute;
	}

	@SuppressWarnings("unchecked")
	public Attribute<List<AssociationValue>> instantiate() {
		return new OneToManyAttribute(getValue());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<AssociationValue> getValue() {
		if (!gotValue) {
			try {
				Attribute<List<AssociationValue>> attribute = featureModel.getAttribute(pojo, name);
				super.setValue(attribute.getValue());
			} catch (LayerException le) {
				Logger log = LoggerFactory.getLogger(LazyPrimitiveAttribute.class);
				log.error("Could not lazily get attribute " + name, le);
			}
		}
		return super.getValue();
	}

	@Override
	public void setValue(List<AssociationValue> value) {
		gotValue = true;
		super.setValue(value);
	}

	@Override
	public LazyOneToManyAttribute clone() { // NOSONAR
		return new LazyOneToManyAttribute(featureModel, pojo, name);
	}

}
