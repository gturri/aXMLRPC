package de.timroes.axmlrpc.suites;

import de.timroes.axmlrpc.TestXmlRpcServer;
import de.timroes.axmlrpc.suites.tests.XmlRpcClientCallTests;
import de.timroes.axmlrpc.suites.tests.XmlRpcClientCreationTests;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This test suite contains all tests, that need a running XML-RPC Server.
 * The suite will start up the test server (see {@link TestXmlRpcServer})
 * and stop it before and after running the test classes.
 * 
 * @author Tim Roes - mail@timroes.de
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	XmlRpcClientCreationTests.class,
	XmlRpcClientCallTests.class
})
public class TestServerSuite {
	
	private static TestXmlRpcServer testServer;
	private static URL testServerUrl;

	@BeforeClass
	public static void startXmlRpcServer() throws MalformedURLException {
		testServer = new TestXmlRpcServer();
		int port = testServer.start();
		testServerUrl = new URL("http://127.0.0.1:" + port);
	}
	
	@AfterClass
	public static void endXmlRpcServer() {
		testServer.stop();
	}
	
	/**
	 * Returns the port on which the test server is listening.
	 * 
	 * @return port of the test server
	 */
	public static URL getServerUrl() {
		return testServerUrl;
	}
	
}
