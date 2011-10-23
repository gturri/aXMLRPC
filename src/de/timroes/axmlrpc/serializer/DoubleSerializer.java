package de.timroes.axmlrpc.serializer;

import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLUtil;
import de.timroes.axmlrpc.xmlcreator.XmlElement;
import java.text.DecimalFormat;
import org.w3c.dom.Element;

/**
 *
 * @author Tim Roes
 */
public class DoubleSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return Double.parseDouble(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DOUBLE,
				new DecimalFormat("#0.0#").format(((Double)object).doubleValue()));
	}

}
