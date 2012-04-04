package de.timroes.axmlrpc;

/**
 * This exception will be thrown if the server returned an UNAUTHORIZED or
 * FORBIDDEN response code.
 * 
 * @author Tim Roes <mail@timroes.de>
 */
public class XMLRPCAuthorizationException extends XMLRPCException {

	private final int statusCode;
	
	public XMLRPCAuthorizationException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	/**
	 * Get the status code, that has been returned by the server.
	 * 
	 * @return The status code, that caused this exception.
	 */
	public final int getStatusCode() {
		return statusCode;
	}	

}
