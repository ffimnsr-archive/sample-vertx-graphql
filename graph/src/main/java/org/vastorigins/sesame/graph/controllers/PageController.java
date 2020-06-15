package org.vastorigins.sesame.graph.controllers;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.vastorigins.sesame.graph.helpers.ResponseHelper.respondWithText;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The page controller
 */
public class PageController extends BaseController {
  protected static Logger LOGGER = LoggerFactory.getLogger(PageController.class);

  /**
   * Constructor
   * @param vertx The vertx instance
   */
  public PageController(final Vertx vertx) {
    super(vertx);
  }

  /**
   * A static method to create a PageController instance
   * @param aVertx The vertx instance
   * @return PageController instance
   */
  public static PageController create(final Vertx aVertx) {
    return new PageController(aVertx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(final RoutingContext aContext) {
    respondWithText(aContext, OK.code(), "OSSLOCAL Graph API");
  }
}
