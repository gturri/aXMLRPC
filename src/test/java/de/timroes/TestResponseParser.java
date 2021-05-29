package de.timroes;

import static org.junit.Assert.*;

import de.timroes.axmlrpc.*;
import de.timroes.axmlrpc.serializer.SerializerHandler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

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
    public void testWithTrailingWhitespaceInTags() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse >" +
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param >" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("toto", actual);
    }

    @Test
    public void testWithTrailingEndlineInTags() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse\n>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param\n>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("toto", actual);
    }

    @Test
    public void testWithTrailingTabInTags() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse\t>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param\t>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("toto", actual);
    }

    @Test
    public void testResponseWithNonAsciiCharacter() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>Aéris</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("Aéris", actual);
    }

    @Test
    public void testUTF16Response() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, bytesToStream((xmlDecl +
                "<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>").getBytes(StandardCharsets.UTF_16)), false);
        assertEquals("toto", actual);
    }

    @Test
    public void testResponseWithComplexValue() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value>" +
                "        <struct>" +
                "          <member><name>intValue</name><value><i4>12</i4></value></member>" +
                "          <member><name>otherIntValue</name><value><int>13</int></value></member>" +
                "          <member><name>boolValue</name><value><boolean>1</boolean></value></member>" +
                "          <member><name>strValue</name><value><string>toto</string></value></member>" +
                "          <member><name>doubleValue</name><value><double>12.4</double></value></member>" +
                "          <member><name>dateValue</name><value><dateTime.iso8601>20200908T0440Z</dateTime.iso8601></value></member>" +
                // Don't test base64 because it seems assertEqual will do a reference equals on arrray of bytes so it's not easily testable here
                // so we test it in a test below
                //"          <member><name>base64Value</name><value><base64>QWVyaXM=</base64></value></member>" +
                "          <member><name>nestedValue</name><value><struct> " +
                "            <member><name>innerStrValue</name><value><string>inner</string></value></member>" +
                "          </struct></value></member>" +
                "        </struct>" +
                "      </value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);

        Map<Object, Object> expected = new TreeMap<>();
        expected.put("intValue", 12);
        expected.put("otherIntValue", 13);
        expected.put("boolValue", true);
        expected.put("strValue", "toto");
        expected.put("doubleValue", 12.4);
        expected.put("dateValue", new Date(1599540000000L));
        Map<Object, Object> innerStruct = new TreeMap<>();
        innerStruct.put("innerStrValue", "inner");
        expected.put("nestedValue", innerStruct);
        assertEquals(expected, actual);
    }

    @Test
    public void testResponseWithBase64Value() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(xmlDecl +
                "<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value>" +
                "        <base64>QWVyaXM=</base64>" +
                "      </value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);

        String actualAsStr = new String((byte[]) actual, StandardCharsets.UTF_8);
        assertEquals("Aeris", actualAsStr);
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

    @Test
    public void testResponseWithXmlComment() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream("<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <!--value><string>toto</string></value-->" +
                "      <value><string>tata</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("tata", actual);
    }

    @Test
    public void testResponseWithInlineXmlComment() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream("<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>ti<!--blah-->ti</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("titi", actual);
    }

    @Test
    public void testResponseWithSpecialChars() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream("<methodResponse>" +
                "  <params>" +
                "    <param>" +
                "      <value><string>to&lt;to</string></value>" +
                "    </param>" +
                "  </params>" +
                "</methodResponse>"), false);
        assertEquals("to<to", actual);
    }

    @Test(expected = XMLRPCException.class)
    public void testErrorMissingMethodResponseTag() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream(
                "  <params>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param>" +
                "  </params>"), false);
    }

    @Test(expected = XMLRPCException.class)
    public void testErrorMissingParamsTag() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream("<methodResponse>" +
                "    <param>" +
                "      <value><string>toto</string></value>" +
                "    </param>" +
                "</methodResponse>"), false);
    }

    @Test(expected = XMLRPCException.class)
    public void testErrorMissingParamTag() throws Exception {
        ResponseParser sut = new ResponseParser();
        Object actual = sut.parse(sh, strToStream("<methodResponse>" +
                "  <params>" +
                "      <value><string>toto</string></value>" +
                "  </params>" +
                "</methodResponse>"), false);
    }

    private static InputStream strToStream(String str){
        return bytesToStream(str.getBytes(StandardCharsets.UTF_8));
    }

    private static InputStream bytesToStream(byte[] bytes){
        return new ByteArrayInputStream(bytes);
    }
}
