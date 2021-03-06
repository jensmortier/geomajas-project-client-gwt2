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
package org.geomajas.gwt2.client.gfx;

import org.vaadin.gwtgraphics.client.Image;

/**
 * A non-scaling image that is anchored to its world space location on a specific pixel or anchor location (useful for
 * location markers).
 * 
 * @author Jan De Moerloose
 * 
 */
public class AnchoredImage extends Image {

	private int anchorX;

	private int anchorY;

	/**
	 * Creates an image at the specified world location with a specified size and anchor point. E.g., if
	 * (anchorX,anchorY)=(width/2, height/2), the center of the image will be positioned the world location.
	 * 
	 * @param userX x-location in world coordinates
	 * @param userY y-location in world coordinates
	 * @param width width in pixels
	 * @param height height in pixels
	 * @param href URL of the image (use absolute URLs, not GWT-based !)
	 * @param anchorX x-location of the anchor point (image-relative)
	 * @param anchorY y-location of the anchor point (image-relative)
	 */
	public AnchoredImage(double userX, double userY, int width, int height, String href, int anchorX, int anchorY) {
		super(userX, userY, width, height, href);
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}

	@Override
	protected void drawTransformed() {
		getImpl().setX(getElement(), (int) Math.round(getUserX() * getScaleX() + getDeltaX()) - anchorX, isAttached());
		getImpl().setY(getElement(), (int) Math.round(getUserY() * getScaleY() + getDeltaY()) - anchorY, isAttached());
		// don't scale, but have to set width/height here !
		setWidth((int) getUserWidth());
		setHeight((int) getUserHeight());
	}
}