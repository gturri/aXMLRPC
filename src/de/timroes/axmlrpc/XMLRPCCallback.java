package de.timroes.axmlrpc;

/**
 * The XMLRPCCallback interface must be implemented by a listener for an
 * asynchronous call to a server method.
 * When the server responds, the corresponding method on the listener is called.
 *
 * @author Tim Roes
 */
public interface XMLRPCCallback {

	public void onResponse(long call, Object result);

	public void onError(long call, XMLRPCException error);

	public void onServerError(long call, XMLRPCServerException error);

}
