package de.timroes.axmlrpc

import groovy.net.xmlrpc.*

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
class TestXmlRpcServer {
	
	private XMLRPCServer server;
	
	private final def ALL_METHODS = [
		/*
		 * Returns the first parameter the way it is.
		 */
		echo: { return it },
		
		/*
		 * Returns the string "pong" as a response.
		 */
		ping: { return "pong" },
		
		/*
		 * Returns the first and second argument in uppercase concatenated.
		 */
		uppercase: { first, second ->
			return first.toString().toUpperCase() + second.toString().toUpperCase()
		},
		
		/*
		 * Returns an integer.
		 */
		getNumber: { return 42 },
		
		/*
		 * Returns the boolean value true.
		 */
		getTrue: { return true },
		
		/*
		 * Returns the boolean value false.
		 */
		getFalse: { return false }
	];
	
	public TestXmlRpcServer() {
		server = new XMLRPCServer();
		
		ALL_METHODS.each { name, action ->
			server[name] = action
		}
	}
	
	public int start() {
		def socket = new ServerSocket(0)
		println "Starting XML-RPC test server on port ${socket.localPort}"
		server.startServer(socket)
		return socket.localPort
	}
	
	public void stop() {
		server.stopServer()
	}
		
}
