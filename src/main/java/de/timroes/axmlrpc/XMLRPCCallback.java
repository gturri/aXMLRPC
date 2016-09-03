package de.timroes.axmlrpc;

/**
 * The XMLRPCCallback interface must be implemented by a listener for an
 * asynchronous call to a server method.
 * When the server responds, the corresponding method on the listener is called.
 *
 * @author Tim Roes
 */
public interface XMLRPCCallback {

	/**
	 * This callback is called whenever the server successfully responds.
	 */
	void onResponse(Object tag, XMLRPCResponse response);

	/**
	 * This callback is called whenever an error occurs during the method call.
	 */
	void onError(Object tag, Throwable t);
}
