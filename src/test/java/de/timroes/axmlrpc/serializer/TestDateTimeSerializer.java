package de.timroes.axmlrpc.serializer;

import static org.junit.Assert.*;

import java.util.Date;

public class TestDateTimeSerializer {
	private static final Date EXPECTED_DATE = new Date(85, 2, 4, 12, 13, 14);

	@org.junit.Test
	public void canParseLegacyDates() throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize("19850304T12:13:14");
		assertDatesCloseEnough(EXPECTED_DATE, date);
	}

	@org.junit.Test
	public void canParseDateWithoutSemiColon() throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize("1985-03-04T12:13:14+0100");
		assertDatesCloseEnough(EXPECTED_DATE, date);
	}

	@org.junit.Test
	public void canParseDateWithSemiColon() throws Exception {
		Date date = (Date) new DateTimeSerializer().deserialize("1985-03-04T12:13:14+01:00");
		assertDatesCloseEnough(EXPECTED_DATE, date);
	}

	//Because I don't want the tests to fail if the user isn't in my timezone
	private void assertDatesCloseEnough(Date expected, Date actual){
		long differenceInMs = Math.abs(expected.getTime() - actual.getTime());
		assertTrue(differenceInMs < 24 * 60 * 60 * 1000);
	}
}
