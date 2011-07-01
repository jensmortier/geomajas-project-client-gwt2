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
package org.geomajas.configuration;

import javax.validation.constraints.NotNull;

import org.geomajas.annotations.Api;
import org.geomajas.configuration.validation.ValidatorInfo;

/**
 * Attribute information class.
 * 
 * @author Jan De Moerloose
 * @since 1.6.0
 */
@Api(allMethods = true)
public class AttributeInfo extends AttributeBaseInfo {

	private static final long serialVersionUID = 152L;

	@NotNull
	private String label;

	private boolean identifying;

	private boolean hidden;

	private ValidatorInfo validator = new ValidatorInfo();

	private boolean includedInForm = true;

	private String formInputType;

	/** Default constructor for GWT. */
	public AttributeInfo() {
		this(null, null);
	}

	/**
	 * Creates a non-editable, non-identifying, non-hidden attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @param label
	 *            attribute label
	 */
	public AttributeInfo(String name, String label) {
		this(false, false, false, name, label);
	}

	/**
	 * Full-option constructor.
	 * 
	 * @param editable
	 *            editable status
	 * @param hidden
	 *            hidden status
	 * @param identifying
	 *            is attribute identifying?
	 * @param label
	 *            attribute label
	 * @param name
	 *            attribute name
	 */
	public AttributeInfo(boolean editable, boolean hidden, boolean identifying, String label, String name) {
		setEditable(editable);
		setHidden(hidden);
		setIdentifying(identifying);
		setLabel(label);
		setName(name);
	}

	/**
	 * Get label for attribute.
	 * 
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set label for attribute.
	 * 
	 * @param label
	 *            label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Is this an identifying attribute? Is it part of the feature id?
	 * 
	 * @return true when attribute is part of the feature id
	 */
	public boolean isIdentifying() {
		return identifying;
	}

	/**
	 * Set whether the attribute is part of the feature id.
	 * 
	 * @param identifying
	 *            true when attribute is part of the feature id
	 */
	public void setIdentifying(boolean identifying) {
		this.identifying = identifying;
	}

	/**
	 * Get whether the attribute should be hidden.
	 * 
	 * @return true when hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Set whether the attribute should be hidden.
	 * 
	 * @param hidden
	 *            hidden status
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Get validator for attribute.
	 * 
	 * @return validator
	 */
	public ValidatorInfo getValidator() {
		return validator;
	}

	/**
	 * Set validator for attribute.
	 * 
	 * @param validator
	 *            validator
	 */
	public void setValidator(ValidatorInfo validator) {
		this.validator = validator;
	}

	/**
	 * This value determines whether or not this attribute definition should be included in editing forms on the client.
	 * This is an optional value that only makes sense when the attribute in question is not hidden.<br/>
	 * The default value for this setting is 'true'.
	 * 
	 * @return Include in forms or not?
	 * @since 1.9.0
	 */
	public boolean isIncludedInForm() {
		return includedInForm;
	}

	/**
	 * Determine whether or not this attribute definition should be included in editing forms on the client. This is an
	 * optional value that only makes sense when the attribute in question is not hidden.<br/>
	 * The default value for this setting is 'true'.
	 * 
	 * @param includedInForm
	 *            The new value.
	 * @since 1.9.0
	 */
	public void setIncludedInForm(boolean includedInForm) {
		this.includedInForm = includedInForm;
	}

	/**
	 * Get the type of input field that should be used in client side forms when editing this attribute. This value
	 * should only be used when diverting from the default values (which are based upon the attribute type itself).
	 * Using this field though enables users to specify their own client-side form field types and use them through
	 * configuration.
	 * 
	 * @return Returns the form input type to be used, or null when the default behavior is desired.
	 * @since 1.9.0
	 */
	public String getFormInputType() {
		return formInputType;
	}

	/**
	 * Set the type of input field that should be used in client side forms when editing this attribute. This value
	 * should only be used when diverting from the default values (which are based upon the attribute type itself).
	 * Using this field though enables users to specify their own client-side form field types and use them through
	 * configuration.
	 * 
	 * @param formInputType
	 *            The form input type to be used. Leave this empty (null) when the default behavior is desired.
	 * @since 1.9.0
	 */
	public void setFormInputType(String formInputType) {
		this.formInputType = formInputType;
	}
}