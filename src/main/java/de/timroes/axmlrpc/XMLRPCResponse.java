package de.timroes.axmlrpc;

import okhttp3.Response;

public class XMLRPCResponse {

  private Response response;

  private Object body;

  public XMLRPCResponse(Response response, Object body) {
    this.response = response;
    this.body = body;
  }

  public Response getResponse() {
    return response;
  }

  public Object getBody() {
    return body;
  }

  public boolean isSuccessful() {
    return response != null && response.isSuccessful() && body != null;
  }
}
