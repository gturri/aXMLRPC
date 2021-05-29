package de.timroes.axmlrpc;

public class SimpleXMLRPCParser {
    private State currentState = State.NOT_STARTED;
    private int numberOfOpenValueTag = 0;

    private enum State {
        NOT_STARTED,
        IN_METHODRESPONSE,
        OUT_METHODRESPONSE,
        IN_PARAMS,
        OUT_PARAMS,
        IN_PARAM,
        OUT_PARAM,
        IN_VALUE,
        OUT_VALUE,
        IN_FAULT,
        OUT_FAULT,
        DONE
    }

    public void openTag(String tagName) throws XMLRPCException {
        if (currentState == State.NOT_STARTED){
            if (tagName.equals(XMLRPCClient.METHOD_RESPONSE)) {
                currentState = State.IN_METHODRESPONSE;
            } else {
                throw new XMLRPCException("MethodResponse root tag is missing.");
            }
        } else if (currentState == State.IN_METHODRESPONSE){
            if (tagName.equals(XMLRPCClient.PARAMS)){
                currentState = State.IN_PARAMS;
            } else if (tagName.equals(XMLRPCClient.FAULT)) {
                currentState = State.IN_FAULT;
            } else {
                throw new XMLRPCException("The methodResponse tag must contain a fault or params tag.");
            }
        } else if (currentState == State.IN_PARAMS){
            if (tagName.equals(XMLRPCClient.PARAM)){
                currentState = State.IN_PARAM;
            } else {
                throw new XMLRPCException("The params tag must contain a param tag.");
            }
        } else if (currentState == State.IN_PARAM){
            if (tagName.equals(XMLRPCClient.VALUE)){
                currentState = State.IN_VALUE;
                numberOfOpenValueTag++;
            } else {
                throw new XMLRPCException("Value tag is missing around value.");
            }
        } else if (currentState == State.IN_VALUE){
            //TODO
        } else if (currentState == State.IN_FAULT) {
            //TODO
        } else {
            throw new XMLRPCException("Unexpected open tag " + tagName + " in state " + currentState);
        }
    }

    public void closedTag(String tagName) throws XMLRPCException {
        if (currentState == State.IN_VALUE){
            if (tagName.equals(XMLRPCClient.VALUE)){
                numberOfOpenValueTag--;
                if (numberOfOpenValueTag == 0){
                    currentState = State.OUT_VALUE;
                }
            } else {
                //TODO: pass to underlying deserializer
            }
        //TODO: handle other states
        } else {
            throw new XMLRPCException("Unexpected closing tag " + tagName + " in state " + currentState);
        }

    //TODO
    }

    public void readText(String text){
        //TODO
    }

    public Object getParsedData(){
        //TODO
        return null;
    }
}
