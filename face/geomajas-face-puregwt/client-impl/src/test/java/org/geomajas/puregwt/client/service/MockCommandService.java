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
package org.geomajas.puregwt.client.service;

import java.util.Stack;

import org.geomajas.command.CommandResponse;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.Deferred;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.TokenRequestHandler;
import org.geomajas.gwt.client.command.event.TokenChangedHandler;
import org.junit.Assert;

import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.core.Function;

public class MockCommandService implements CommandService {

	/** Default token assigned when logging in. */
	public static final String TOKEN_DEFAULT = "token";
	/** Token value when nog logged in. */
	public static final String TOKEN_NONE = "none";

	Stack<CommandResponse> responseStack = new Stack<CommandResponse>();
	private String userToken = TOKEN_NONE;
	private TokenChangedHandler handler;
	private TokenRequestHandler tokenRequestHandler;

	public Deferred execute(GwtCommand command, CommandCallback... callback) {
		if (responseStack.isEmpty()) {
			Assert.fail("Expected response for " + command.getCommandName());
		}
		for (CommandCallback commandCallback : callback) {
			commandCallback.execute(responseStack.pop());
		}
		return new ForbiddenDeferred();
	}

	public void pushResponse(CommandResponse item) {
		responseStack.push(item);
	}

	public void clear() {
		responseStack.clear();
	}

	public class ForbiddenDeferred extends Deferred {

		@Override
		public void addCallback(CommandCallback callback) {
			Assert.fail("Can't add callback to command service !");
		}

		@Override
		public void addSuccessCallback(CommandCallback callback) {
			Assert.fail("Can't add callback to command service !");
		}

		@Override
		public void addErrorCallback(Function onError) {
			Assert.fail("Can't add callback to command service !");
		}

	}

	/** {@inheritDoc} */
	public void login() {
		this.userToken = TOKEN_DEFAULT;
	}

	/** {@inheritDoc} */
	public void logout() {
		this.userToken = TOKEN_NONE;
	}

	/** {@inheritDoc} */
	public String getUserToken() {
		return userToken;
	}

	/** {@inheritDoc} */
	public HandlerRegistration addTokenChangedHandler(TokenChangedHandler handler) {
		this.handler = handler;
		return null;
	}

	/** {@inheritDoc} */
	public void setTokenRequestHandler(TokenRequestHandler tokenRequestHandler) {
		this.tokenRequestHandler = tokenRequestHandler;
	}
}