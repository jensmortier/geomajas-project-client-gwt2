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
package org.geomajas.gwt2.plugin.print.client.event;


import org.geomajas.gwt2.plugin.print.client.util.PrintConfiguration;
import org.geomajas.plugin.printing.command.dto.PrintTemplateInfo;

/**
 * Info object, containing info of the print request.
 * 
 * @author Jan Venstermans
 * 
 */
public class PrintRequestInfo {
	private PrintTemplateInfo printTemplateInfo;
	private String fileName;
	private PrintConfiguration.PostPrintAction postPrintAction;
	private boolean sync;
	private int dpi;

	public PrintTemplateInfo getPrintTemplateInfo() {
		return printTemplateInfo;
	}

	public void setPrintTemplateInfo(PrintTemplateInfo printTemplateInfo) {
		this.printTemplateInfo = printTemplateInfo;
	}

	public PrintConfiguration.PostPrintAction getPostPrintAction() {
		return postPrintAction;
	}

	public void setPostPrintAction(PrintConfiguration.PostPrintAction postPrintAction) {
		this.postPrintAction = postPrintAction;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public boolean isSync() {
		return sync;
	}
	
	public void setSync(boolean sync) {
		this.sync = sync;
	}
	
	public int getDpi() {
		return dpi;
	}
	
	public void setDpi(int dpi) {
		this.dpi = dpi;
	}

}
