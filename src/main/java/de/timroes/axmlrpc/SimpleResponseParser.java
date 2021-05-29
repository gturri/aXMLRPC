package de.timroes.axmlrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;


/**
 * Simple xml parser which implements only the features needed for xmlrpc xml
 * (ie: no DTD, tags without attributes, ...).
 * This parser is made to parse the xmlrpc responses, not to validate them (though
 * it can catch some obvious mistakes)
 */
public class SimpleResponseParser {
    private boolean debugMode;
    private StringBuilder globalBufferForDebug = new StringBuilder();

    private Reader reader;
    private StringBuilder temporaryBuffer = new StringBuilder();
    private boolean openedAtLeastOneTag = false;
    private int currentNbOfNestedTags = 0;

    private enum State {
        PARSING_HEADER,
        PARSING_OPENING_TAG,
        PARSING_CLOSING_TAG,
        PARSING_TEXT,
        PARSING_COMMENT,
    }

    public SimpleResponseParser(InputStream response, boolean debugMode){
        this.debugMode = debugMode;
        this.reader = new InputStreamReader(response, StandardCharsets.UTF_8); //TODO: charset should probably be injected too
    }

    public Object parse() throws XMLRPCException {
        try {
            return innerParse();
        } catch (Exception ex){
            throw new XMLRPCException("Error getting result from server.", ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex){ /* Nothing: we don't really care if it fails */ }
        }
    }

    private Object innerParse() throws Exception {
        SimpleXMLRPCParser xmlrpcParser = new SimpleXMLRPCParser();
        while (!openedAtLeastOneTag && currentNbOfNestedTags != 0){
            switch (findOutNextState()){
                case PARSING_HEADER:
                    skipToEndOfHeader();
                    break;
                case PARSING_COMMENT:
                    skipToEndOfComment();
                    break;
                case PARSING_OPENING_TAG:
                    openedAtLeastOneTag = true;
                    currentNbOfNestedTags++;
                    String openingTagName = readTag();
                    xmlrpcParser.openTag(openingTagName);
                    break;
                case PARSING_CLOSING_TAG:
                    currentNbOfNestedTags--;
                    String closingTagName = readTag();
                    xmlrpcParser.closedTag(closingTagName);
                    break;
                case PARSING_TEXT:
                    String text = readTextNode();
                    xmlrpcParser.readText(text);
                    break;
                default:
                    throw new IllegalStateException("unexpected state");
            }
        }

        if (debugMode){
            System.out.println(globalBufferForDebug.toString());
        }
        return xmlrpcParser.getParsedData();
    }

    private char readNextChar() throws IOException {
        int next = reader.read();
        if (next == -1){
            throw new IOException("Unexpected end of stream");
        }
        char nextChar = (char) next;
        if (debugMode){
            globalBufferForDebug.append(nextChar);
        }
        return nextChar;
    }

    private void skipToEndOfComment() throws IOException {
        boolean readFirstDash = false;
        boolean readSecondDash = false;
        boolean readBracket = false;

        while (!readBracket) {
            char c = readNextChar();
            if (c == '-'){
                if (readFirstDash){
                    readSecondDash = true;
                } else {
                    readFirstDash = true;
                }
            } else if (c == '>' && readFirstDash && readSecondDash) {
                readBracket = true;
            } else {
                readFirstDash = false;
                readSecondDash = false;
                readBracket = false;
            }
        }
    }

    private State findOutNextState() throws IOException {
        char firstChar = readNextChar();
        if ( firstChar == '<'){
            char secondChar = readNextChar();
            if (secondChar == '/'){
                temporaryBuffer.setLength(0);
                return State.PARSING_CLOSING_TAG;
            } else if (secondChar == '!'){
                return State.PARSING_COMMENT;
            } else if (secondChar == '?'){
                return State.PARSING_HEADER;
            } else {
                temporaryBuffer.setLength(0);
                temporaryBuffer.append(secondChar);
                return State.PARSING_OPENING_TAG;
            }
        }
        temporaryBuffer.setLength(0);
        temporaryBuffer.append(firstChar);
        return State.PARSING_TEXT;
    }

    private void skipToEndOfHeader() throws IOException {
        while(readNextChar() != '>');
    }

    private String readTextNode() throws IOException {
        boolean done = false;

        while(!done){
            char c = readNextChar();
            if (c == '<'){
               c = readNextChar();
               if (c == '/'){
                   done = true;
               } else {
                   skipToEndOfComment();
               }
            } else {
                temporaryBuffer.append(c);
            }
        }

        // TODO: unescape chars
        return temporaryBuffer.toString();
    }

    private String readTag() throws IOException {
        boolean done = false;

        while(!done){
            char c = readNextChar();
            if (c == '>'){
                done = true;
            } else if (isWhitespaceish(c)){
                temporaryBuffer.append(c);
            }
        }

        return temporaryBuffer.toString();
    }

    private boolean isWhitespaceish(char c){
        return c == ' ' || c == '\n' || c == '\t';
    }
}
