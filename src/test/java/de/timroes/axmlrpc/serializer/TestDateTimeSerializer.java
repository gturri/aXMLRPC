package de.timroes.axmlrpc.serializer;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Date;
import java.util.TimeZone;

public class TestDateTimeSerializer {
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
	public void canParseBasicFormatDate() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4), "19850304");
	}

	@Test
	public void canParseExtendedFormatDate() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4), "1985-03-04");
	}

	@Test
	public void canParseDateWithoutDay() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 1), "1985-03");
	}

	@Test
	public void canParseDateWithoutMonth() throws Exception {
		assertDeserializeEquals(new Date(85, 0, 1), "1985");
	}

	@Test
	public void canParseBasicFormatHour() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 12, 21, 36), "19850304T122136");
	}

	@Test
	public void canParseExtendedFormatHour() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 12, 21, 36), "1985-03-04T12:21:36");
	}

	@Test
	public void canParseHourWithoutMinute() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 12, 0), "1985-03-04T12");
	}

	@Test
	public void canParseHourWithoutSecond() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 12, 10), "1985-03-04T12:10");
	}

	@Test
	public void canParseDecimalPartOfAMinute() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 12, 21, 15), "1985-03-04T12:21.25");
		assertDeserializeEquals(new Date(85, 2, 4, 12, 21, 30), "1985-03-04T12:21.5");
	}

	@Test
	public void canParseDecimalPartOfAnHour() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 12, 15), "1985-03-04T12.25");
		assertDeserializeEquals(new Date(85, 2, 4, 12, 0, 36), "1985-03-04T12.01");
	}

	@Test
	public void canParseMilliseconds() throws Exception {
		Date ms500 = (Date) new DateTimeSerializer().deserialize("1985-03-04T12:21:36.5");
		assertEquals(500, ms500.getTime() - new Date(85, 2, 4, 12, 21, 36).getTime());
	}

	@Test
	public void canParseUTCHour() throws Exception {
		Date expected = new Date(85, 2, 4, 12, 21, 36);
		assertDeserializeEquals(expected, "1985-03-04T12:21:36Z");

		// When timezone is explicit, we should get the exact same date regardless of this computer default timezone
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
		assertDeserializeEquals(expected, "1985-03-04T12:21:36Z");
	}

	@Test
	public void canParseBasicFormatTimeOffset() throws Exception {
		Date expected = new Date(85, 2, 4, 1, 40, 31);
		assertDeserializeEquals(expected, "19850304T131031+1130");

		// When timezone is explicit, we should get the exact same date regardless of this computer default timezone
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
		assertDeserializeEquals(expected, "19850304T131031+1130");
	}

	@Test
	public void canParseExtendedFormatTimeOffset() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 1, 40, 31), "1985-03-04T13:10:31+11:30");
	}

	@Test
	public void canParseTimeOffsetWithoutMinute() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 2, 10, 31), "1985-03-04T13:10:31+11");
	}

	@Test
	public void canParseNegativeTimeOffset() throws Exception {
		assertDeserializeEquals(new Date(85, 2, 4, 22, 30, 31), "1985-03-04T13:10:31-09:20");
	}

	@Test
	public void canHandleLimitCases() throws Exception {
		assertDeserializeEquals(new Date(80, 0, 1), "1980-01-01T00:00:00.0Z");
		assertDeserializeEquals(new Date(80, 0, 1), "1980-01-01T00Z");
		assertDeserializeEquals(new Date(81, 11, 31, 23, 59, 59), "1981-12-31T23:59:59Z");
	}

	private void assertDeserializeEquals(Date expected, String toDeserialize) throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize(toDeserialize);
		assertEquals(expected, date);
	}
}
