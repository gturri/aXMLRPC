package de.timroes.axmlrpc.suites.tests;

import de.timroes.axmlrpc.XmlRpcClient;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public class XmlRpcClientCreationTests {
	
	@Test
	public void creationForUrl() throws MalformedURLException {
		URL url = new URL("http://localhost/");
		assertEquals(url, XmlRpcClient.forUrl(url).getUrl());
	}
	
	@Test
	public void creationWithStringUrl() throws MalformedURLException {
		assertEquals(new URL("http://localhost"), XmlRpcClient.forUrl("http://localhost").getUrl());
	}
	
	@Test(expected = MalformedURLException.class)
	public void creationWithInvalidStringUrl() throws MalformedURLException {
		XmlRpcClient.forUrl("foobar");
	}
	
}
