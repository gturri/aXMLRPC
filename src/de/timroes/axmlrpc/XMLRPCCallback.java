package de.timroes.axmlrpc;

/**
 *
 * @author Tim Roes
 */
public interface XMLRPCCallback {

	public void onResponse(long call, Object result);

	public void onError(long call, XMLRPCException error);

	public void onServerError(long call, XMLRPCServerException error);

}
