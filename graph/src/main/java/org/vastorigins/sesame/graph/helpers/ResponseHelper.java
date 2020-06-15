package org.vastorigins.sesame.graph.helpers;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.http.HttpHeaders;
import io.vertx.reactivex.ext.web.RoutingContext;

public class ResponseHelper {
  public static void respondWithJson(final RoutingContext aContext, final int aStatusCode, final String aContent) {
    aContext.request().response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .setStatusCode(aStatusCode).end(aContent);
  }

  public static void respondWithText(final RoutingContext aContext, final int aStatusCode, final String aContent) {
    aContext.request().response()
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
      .setStatusCode(aStatusCode)
      .end(aContent);
  }
}
