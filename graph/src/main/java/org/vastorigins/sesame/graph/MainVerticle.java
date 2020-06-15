package org.vastorigins.sesame.graph;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static org.vastorigins.sesame.graph.helpers.ResponseHelper.respondWithJson;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.vastorigins.sesame.graph.controllers.PageController;
import org.vastorigins.sesame.graph.models.Organization;
import org.vastorigins.sesame.graph.models.UserClue;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.impl.OAuth2AuthProviderImpl;
import io.vertx.ext.auth.oauth2.impl.OAuth2TokenImpl;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.AuthProvider;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.authorization.Authorization;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.ext.web.handler.AuthHandler;
import io.vertx.reactivex.ext.web.handler.LoggerHandler;
import io.vertx.reactivex.ext.web.handler.OAuth2AuthHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.reactivex.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import io.vertx.reactivex.ext.web.sstore.SessionStore;
import io.vertx.reactivex.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
  private MySQLPool mySQLPool;

  @Override
  public Completable rxStart() {
    LOGGER.trace("Graph service booting up");

    final Router router = Router.router(vertx);
    final LoggerHandler loggerHandler = LoggerHandler.create();
    final SessionStore sessionStore = LocalSessionStore.create(vertx);
    final SessionHandler sessionHandler = SessionHandler.create(sessionStore);

    router.route().handler(loggerHandler);
    router.route().handler(sessionHandler);

    // used for backend calls with bearer token
    final WebClient webClient = WebClient.create(vertx);

    final String hostname = System.getProperty("http.host", "localhost");
    final int port = Integer.getInteger("http.port", 8080);
    final String baseUrl = String.format("http://%s:%d", hostname, port);
    final String oAuthCallbackPath = "/callback";

    final OAuth2Options clientOptions = new OAuth2Options()
        .setFlow(OAuth2FlowType.AUTH_CODE)
        .setSite(System.getProperty("oauth2.issuer", "http://192.168.99.100:8080/auth/realms/sesame"))
        .setClientID(System.getProperty("oauth2.client_id", "vertx-api-client"))
        .setClientSecret(System.getProperty("oauth2.client_secret", "a81335a1-7c1c-4c2c-a34d-62022437a4f3"));

    final HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    KeycloakAuth.discover(vertx, clientOptions, oAuth2AuthAsyncResult -> {
      final OAuth2Auth oAuth2Auth = oAuth2AuthAsyncResult.result();

      if (oAuth2Auth == null) {
        throw new RuntimeException(
            "Could not configure Keycloak integration via OpenID connect discovery endpoint. Is Keycloak running?");
      }

      final OAuth2AuthHandler oAuth2 = OAuth2AuthHandler.create(vertx, oAuth2Auth, baseUrl + oAuthCallbackPath)
          .setupCallback(router.get(oAuthCallbackPath));

      // sessionHandler.setAuthProvider((AuthProvider) oAuth2Auth);
      router.route("/protected/*").handler(oAuth2);
      router.get("/health").handler(healthCheckHandler);
      configureRoutes(router, webClient, oAuth2Auth);
    });

    final String mySqlConnectionUri = System.getProperty("mariadb.uri",
        "mariadb://root:mariadb@192.168.99.100:3306/osslocal");

    final PoolOptions poolOptions = new PoolOptions().setMaxSize(Integer.getInteger("mariadb.pool_size", 5));

    mySQLPool = MySQLPool.pool(vertx, mySqlConnectionUri, poolOptions);

    CircuitBreaker.create("sesame-circuit-breaker", vertx, new CircuitBreakerOptions().setMaxFailures(5)
        .setTimeout(2000).setFallbackOnFailure(true).setResetTimeout(10000));

    return vertx.createHttpServer().requestHandler(router).rxListen(port)
        .doOnSuccess(c -> LOGGER.info("Graph service successfully started on port " + c.actualPort())).ignoreElement();
  }

  @Override
  public Completable rxStop() {
    mySQLPool.close();
    return super.rxStop();
  }

  private void configureRoutes(final Router aRouter, final WebClient aWebClient, final OAuth2Auth aOAuth2Auth) {
    LOGGER.trace("Started setting up routes");
    aRouter.get("/").handler(PageController.create(vertx));

    final String schema = vertx.fileSystem().readFileBlocking("osslocal.graphql").toString();
    final SchemaParser schemaParser = new SchemaParser();
    final SchemaGenerator schemaGenerator = new SchemaGenerator();
    final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    final RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .type("QueryType", builder -> builder
          .dataFetcher("organizations", this::getOrganizations)
          .dataFetcher("users", this::getUserClues)
        )
        .build();

    final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    final GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

    aRouter.post("/graphql").handler(GraphQLHandler.create(graphQL));

    final GraphiQLHandlerOptions options = new GraphiQLHandlerOptions().setEnabled(true);
    aRouter.route("/graphiql/*").handler(GraphiQLHandler.create(options));

    aRouter.get("/protected/user").handler(this::handleUserPage);
    aRouter.get("/protected/admin").handler(this::handleAdminPage);

    final String userInfoUrl = ((OAuth2AuthProviderImpl) aOAuth2Auth.getDelegate()).getConfig().getUserInfoPath();
    aRouter.get("/protected/userinfo").handler(createUserInfoHandler(aWebClient, aOAuth2Auth, userInfoUrl));

    aRouter.get("/logout").handler(this::handleLogout);
  }

  private DataFetcher<CompletionStage<List<Organization>>> getOrganizations(
      final DataFetchingEnvironment aDataFetchingEnvironment) {

    final Single<List<Organization>> data = mySQLPool.preparedQuery("SELECT id, name, description FROM organizations")
        .rxExecute().map(Organization::fromRowSet);

    return e -> data.to(SingleInterop.get());
  }

  private DataFetcher<CompletionStage<List<UserClue>>> getUserClues(
      final DataFetchingEnvironment aDataFetchingEnvironment) {

    final Single<List<UserClue>> data = mySQLPool.preparedQuery("SELECT id, name, description FROM user_clues")
        .rxExecute().map(UserClue::fromRowSet);

    return e -> data.to(SingleInterop.get());
  }

  private void handleUserPage(final RoutingContext aContext) {
    final User user = aContext.user();

    final String username = user.attributes().getString("preferred_username");
    final JsonObject base = new JsonObject().put("success", true).put("message", username);
    respondWithJson(aContext, OK.code(), base.encode());
  }

  private void handleAdminPage(final RoutingContext aContext) {
    final User user = aContext.user();

    user.isAuthorized("realm:admin", res -> {
      if (res.succeeded() || !res.result()) {
        final JsonObject base = new JsonObject().put("success", false).put("message",
            "Unable to access the specific location");
        respondWithJson(aContext, FORBIDDEN.code(), base.encode());
        return;
      }

      final String username = user.attributes().getString("preferred_username");
      final JsonObject base = new JsonObject().put("success", true).put("message", username);
      respondWithJson(aContext, OK.code(), base.encode());
    });
  }

  private void handleLogout(final RoutingContext aContext) {
    aContext.clearUser();

    // aContext.
    // final User user = (OAuth2Auth) aContext.user().isAuthorized(authority);
    // user.logout(res -> {
    //   if (res.succeeded()) {
    //     final JsonObject base = new JsonObject().put("success", false).put("message",
    //         "An error occurred on the backend");
    //     respondWithJson(aContext, INTERNAL_SERVER_ERROR.code(), base.encode());
    //     return;
    //   }

    //   aContext.session().destroy();
    //   aContext.response().putHeader("location", "/?logout=true").setStatusCode(FOUND.code()).end();
    // });

    aContext.session().destroy();
    aContext.response().putHeader("location", "/?logout=true").setStatusCode(FOUND.code()).end();
  }

  private Handler<RoutingContext> createUserInfoHandler(final WebClient aWebClient, final OAuth2Auth aOAuth2Auth, final String aUserInfoUrl) {
    return (final RoutingContext context) -> {
      final User user = context.user();
      final URI userInfoEndpointUri = URI.create(aUserInfoUrl);

      aWebClient.get(userInfoEndpointUri.getPort(), userInfoEndpointUri.getHost(), userInfoEndpointUri.getPath())
          .bearerTokenAuthentication("user.getOpaqueToken").as(BodyCodec.jsonObject()).rxSend().subscribe(resp -> {
            respondWithJson(context, OK.code(), resp.body().encode());
          }, e -> {
            final JsonObject base = new JsonObject().put("success", false).put("message",
                "An error occurred on the backend: " + e.toString());

            LOGGER.error("Fatal error occurred", e);
            respondWithJson(context, BAD_REQUEST.code(), base.encode());
          });
    };
  }
}
