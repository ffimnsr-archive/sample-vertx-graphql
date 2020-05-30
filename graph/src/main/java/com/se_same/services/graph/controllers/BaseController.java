package com.se_same.services.graph.controllers;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;

public abstract class BaseController implements Handler<RoutingContext> {
  protected static Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
  protected Vertx vertx;

  public BaseController(final Vertx vertx) {
    this.vertx = vertx;
  }
}
