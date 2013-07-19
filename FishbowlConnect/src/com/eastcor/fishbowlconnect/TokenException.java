package com.eastcor.fishbowlconnect;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TokenException extends WebApplicationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2488048048811520061L;

	TokenException(String message) {
        super(Response.status(Response.Status.FORBIDDEN)
                .entity(message).type(MediaType.TEXT_PLAIN).build());
	}

}
