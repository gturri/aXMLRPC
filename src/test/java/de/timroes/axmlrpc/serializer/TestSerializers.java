package de.timroes.axmlrpc.serializer;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.timroes.axmlrpc.xmlcreator.XmlElement;

public class TestSerializers {
	private TimeZone _previousTZ;

	@Before
	public void setUp(){
		_previousTZ = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@After
	public void tearDown(){
		TimeZone.setDefault(_previousTZ);
	}


	@Test
	public void canSerializeBase64(){
		assertExpectedSerialized("<base64>AQIDBA==</base64>", new Base64Serializer().serialize(new Byte[]{1, 2, 3, 4}));
	}

	@Test
	public void canSerializeBoolean(){
		assertExpectedSerialized("<boolean>1</boolean>", new BooleanSerializer().serialize(true));
		assertExpectedSerialized("<boolean>0</boolean>", new BooleanSerializer().serialize(false));
	}

	@Test
	public void canSerializeDouble(){
		assertExpectedSerialized("<double>3.2</double>", new DoubleSerializer().serialize(3.2));
	}

	@Test
	public void canSerializeDate(){
		assertExpectedSerialized("<dateTime.iso8601>19850503T122334</dateTime.iso8601>",
				new DateTimeSerializer().serialize(new Date(85, 4, 3, 12, 23, 34)));
	}

	@Test
	public void canSerializeInt(){
		assertExpectedSerialized("<int>4</int>", new IntSerializer().serialize(4));
	}

	@Test
	public void canSerializeLong(){
		assertExpectedSerialized("<i8>1234</i8>", new LongSerializer().serialize(1234L));
	}

	@Test
	public void canSerializeNullElement(){
		assertExpectedSerialized("<nil/>", new NullSerializer().serialize(null));
	}

	@Test
	public void canSerializeString(){
		boolean encodeString = true;
		assertExpectedSerialized("<string>te&lt;&amp;>st</string>", new StringSerializer(encodeString, true).serialize("te<&>st"));

		encodeString = false;
		assertExpectedSerialized("<string>te<&>st</string>", new StringSerializer(encodeString, true).serialize("te<&>st"));
	}

	private static void assertExpectedSerialized(String expected, XmlElement actual){
		assertEquals(expected, actual.toString().trim());
	}
}
