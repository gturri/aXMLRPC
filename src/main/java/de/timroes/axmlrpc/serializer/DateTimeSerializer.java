package de.timroes.axmlrpc.serializer;

import java.text.SimpleDateFormat;

import org.w3c.dom.Element;

import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLUtil;
import de.timroes.axmlrpc.xmlcreator.XmlElement;
import fr.turri.jiso8601.Iso8601Deserializer;

/**
 *
 * @author timroes
 */
public class DateTimeSerializer implements Serializer {

	public static final String DEFAULT_DATETIME_FORMAT = "yyyyMMdd'T'HHmmss";
	private final SimpleDateFormat dateFormatter;

	private final boolean accepts_null_input;

	public DateTimeSerializer(boolean accepts_null_input) {
		this.accepts_null_input = accepts_null_input;
		this.dateFormatter = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
	}

	public DateTimeSerializer(boolean accepts_null_input, String datetimeFormat) {
		this.accepts_null_input = accepts_null_input;
		this.dateFormatter = new SimpleDateFormat(datetimeFormat);
	}


	@Override
	public Object deserialize(Element content) throws XMLRPCException {
		return deserialize(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public Object deserialize(String dateStr) throws XMLRPCException {
		if (accepts_null_input && (dateStr==null || dateStr.trim().length()==0)) {
			return null;
		}

		try {
			return Iso8601Deserializer.toDate(dateStr);
		} catch (Exception ex) {
			throw new XMLRPCException("Unable to parse given date.", ex);
		}
	}

	@Override
	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DATETIME,
				dateFormatter.format(object));
	}

}
