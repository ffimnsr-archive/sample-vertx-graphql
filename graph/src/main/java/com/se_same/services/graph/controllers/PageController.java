package com.se_same.services.graph.controllers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;

import static com.se_same.services.graph.helpers.ResponseHelper.respondWithText;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class PageController extends BaseController {
  protected static Logger LOGGER = LoggerFactory.getLogger(PageController.class);

  public PageController(Vertx vertx) {
    super(vertx);
  }

  public static PageController create(final Vertx aVertx) {
    return new PageController(aVertx);
  }

  @Override
  public void handle(final RoutingContext aContext) {
    respondWithText(aContext, OK.code(), "Hello, World!");
  }
}
