package de.timroes.axmlrpc;

/**
 * Will be thrown when a call to the server times out. The timeout can be 
 * set via {@link XMLRPCClient#setTimeout(int)}.
 * 
 * @author Tim Roes
 */
public class XMLRPCTimeoutException extends XMLRPCException {

	XMLRPCTimeoutException(String ex) {
		super(ex);
	}

}
