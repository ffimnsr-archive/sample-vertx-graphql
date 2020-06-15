package org.vastorigins.sesame.graph.controllers;

import io.vertx.core.Handler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base controller
 */
public abstract class BaseController implements Handler<RoutingContext> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
  protected Vertx vertx;

  /**
   * Constructor
   * @param vertx The vertx instance
   */
  public BaseController(final Vertx vertx) {
    this.vertx = vertx;
  }
}
