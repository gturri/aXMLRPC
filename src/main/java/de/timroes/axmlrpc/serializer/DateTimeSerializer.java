package de.timroes.axmlrpc.serializer;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLUtil;
import de.timroes.axmlrpc.xmlcreator.XmlElement;

/**
 *
 * @author timroes
 */
public class DateTimeSerializer implements Serializer {

	private static final String DATETIME_FORMAT = "yyyyMMdd'T'HH:mm:ss";
	private static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat(DATETIME_FORMAT);
	private static final Pattern PATTERN_WITHOUT_COLON = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{2})(\\d{2})");
	private static final Pattern PATTERN_LEGACY = Pattern.compile("(\\d{4})(\\d{2})(\\d{2}T\\d{2}:\\d{2}:\\d{2})");

	@Override
	public Object deserialize(Element content) throws XMLRPCException {
		return deserialize(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public Object deserialize(String dateStr) throws XMLRPCException {
		try {
			String value = formatStringIfNeeded(dateStr);
			return javax.xml.bind.DatatypeConverter.parseDateTime(value).getTime();
		} catch (Exception ex) {
			throw new XMLRPCException("Unable to parse given date.", ex);
		}
	}

	private static String formatStringIfNeeded(String dateStr){
		Matcher matcherWithoutColon = PATTERN_WITHOUT_COLON.matcher(dateStr);
		if ( matcherWithoutColon.matches() ){
			return matcherWithoutColon.group(1) + ":" + matcherWithoutColon.group(2);
		}

		Matcher matcherLegacy = PATTERN_LEGACY.matcher(dateStr);
		if ( matcherLegacy.matches() ){
			return matcherLegacy.group(1) + "-" + matcherLegacy.group(2) + "-" + matcherLegacy.group(3);
		}

		return dateStr;
	}

	@Override
	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DATETIME,
				DATE_FORMATER.format(object));
	}

}
