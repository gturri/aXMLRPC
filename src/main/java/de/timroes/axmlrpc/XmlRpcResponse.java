package de.timroes.axmlrpc;

/**
 *
 * @author Tim Roes - mail@timroes.de
 */
public class XmlRpcResponse {
	
	private final Object value;
	
	protected XmlRpcResponse(Object value) {
		this.value = value;
	}
	
	public String asString() {
		return String.valueOf(value);
	}

}
