package de.timroes.axmlrpc.serializer;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.TimeZone;

public class TestDateTimeSerializer {
	private Date expectedDate = new Date(85, 2, 4, 12, 13, 14);
	private TimeZone _previousTZ;

	@org.junit.Before
	public void setUp(){
		_previousTZ = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		expectedDate = new Date(85, 2, 4, 12, 13, 14);
	}

	@org.junit.After
	public void tearDown(){
		TimeZone.setDefault(_previousTZ);
	}

	@org.junit.Test
	public void canParseLegacyDates() throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize("19850304T12:13:14");
		assertEquals(expectedDate, date);
	}

	@org.junit.Test
	public void canParseDateWithoutSemiColon() throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize("1985-03-04T13:13:14+0100");
		assertEquals(expectedDate, date);
	}

	@org.junit.Test
	public void canParseDateWithSemiColon() throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize("1985-03-04T13:13:14+01:00");
		assertEquals(expectedDate, date);
	}
}
