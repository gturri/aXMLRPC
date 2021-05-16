package de.timroes;

import static org.junit.Assert.*;

import de.timroes.axmlrpc.ResponseParser;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCServerException;
import de.timroes.axmlrpc.serializer.SerializerHandler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TestResponseParser {
    public final static String xmlDecl = "<?xml version=\"1.0\"?>";
    public final static SerializerHandler sh = new SerializerHandler(XMLRPCClient.FLAGS_NONE);


    @Test
    public void testSimpleResponse() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("toto", actual);
    }

    @Test
    /**
     * I'm not sure it really makes sense to support this case. But since I'm adding tests on code which existed
     * for years, I guess it's better to avoid breaking retro compatibility.
     */
    public void testAcceptMissingHeader() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream("<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("toto", actual);
    }

    @Test
    public void testXmlrpcError() throws Exception {
        ResponseParser sut = new ResponseParser();
        try {
            sut.parse(sh, strToStream("<methodResponse>" +
                    "  <fault>" +
                    "    <value>" +
                    "      <struct>" +
                    "        <member>" +
                    "          <name>faultCode</name>" +
                    "          <value><int>4</int></value>" +
                    "        </member>" +
                    "        <member>" +
                    "          <name>faultString</name>" +
                    "          <value><string>error X occurred</string></value>" +
                    "        </member>" +
                    "      </struct>" +
                    "    </value>" +
                    "  </fault>" +
                    "</methodResponse>"), false);
            fail("The previous call should have thrown");
        } catch (XMLRPCServerException e){
            assertEquals("error X occurred [4]", e.getMessage());
            assertEquals(4, e.getErrorNr());
        }
}

    private static InputStream strToStream(String str){
        return new ByteArrayInputStream(str.getBytes());
    }
}
