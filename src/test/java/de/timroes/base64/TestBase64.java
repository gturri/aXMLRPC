package de.timroes.base64;

import static org.junit.Assert.*;

import org.junit.Test;

import de.timroes.base64.Base64;

public class TestBase64 {

	@Test
	public void canEncode(){
		assertEquals("TWFu", Base64.encode("Man"));
		assertEquals("TWE=", Base64.encode("Ma"));
		assertEquals("TQ==", Base64.encode("M"));
	}

	@Test
	public void canDecode(){
		assertEquals("Man", Base64.decodeAsString("TWFu"));
		assertEquals("Ma", Base64.decodeAsString("TWE="));
		assertEquals("M", Base64.decodeAsString("TQ=="));
	}
}
