/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2012 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.plugin.deskmanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.geomajas.configuration.client.ClientLayerInfo;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;


/**
 * @author Oliver May
 *
 */
@Entity
public class Layer {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id; // UUID

	@Type(type = "org.geomajas.plugin.deskmanager.domain.types.XmlSerialisationType")
	private ClientLayerInfo clientLayerInfo;

	@ManyToOne(fetch = FetchType.LAZY)
	private LayerModel layerModel;
	
	@Column(nullable = false)
	private String clientLayerIdReference;

	/**
	 * @param clientLayerInfo the clientLayerInfo to set
	 */
	public void setClientLayerInfo(ClientLayerInfo clientLayerInfo) {
		this.clientLayerInfo = clientLayerInfo;
	}

	/**
	 * @return the clientLayerInfo
	 */
	public ClientLayerInfo getClientLayerInfo() {
		return clientLayerInfo;
	}

	/**
	 * @param layerModel the layerModel to set
	 */
	public void setLayerModel(LayerModel layerModel) {
		this.layerModel = layerModel;
	}

	/**
	 * @return the layerModel
	 */
	public LayerModel getLayerModel() {
		return layerModel;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param clientLayerIdReference the clientLayerIdReference to set
	 */
	public void setClientLayerIdReference(String clientLayerId) {
		this.clientLayerIdReference = clientLayerId;
	}

	/**
	 * @return the clientLayerIdReference
	 */
	public String getClientLayerIdReference() {
		return clientLayerIdReference;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientLayerIdReference == null) ? 0 : clientLayerIdReference.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Layer other = (Layer) obj;
		if (clientLayerIdReference == null) {
			if (other.clientLayerIdReference != null) {
				return false;
			}
		} else if (!clientLayerIdReference.equals(other.clientLayerIdReference)) {
			return false;
		}
		return true;
	}
	
}