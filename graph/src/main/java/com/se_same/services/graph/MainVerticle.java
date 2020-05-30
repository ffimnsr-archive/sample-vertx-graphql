package com.se_same.services.graph;

import com.se_same.services.graph.controllers.PageController;
import com.se_same.services.graph.models.Organization;
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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.impl.OAuth2AuthProviderImpl;
import io.vertx.ext.auth.oauth2.impl.OAuth2TokenImpl;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.ext.web.handler.AuthHandler;
import io.vertx.reactivex.ext.web.handler.OAuth2AuthHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.reactivex.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import io.vertx.reactivex.ext.web.sstore.SessionStore;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.se_same.services.graph.helpers.ResponseHelper.respondWithJson;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

  public class MainVerticle extends AbstractVerticle {
  private final static Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
  private PgPool pgPool;

  @Override
  public Completable rxStart() {
    LOGGER.trace("Graph service booting up");
    final Router router = Router.router(vertx);
    final SessionStore sessionStore = LocalSessionStore.create(vertx);
    final SessionHandler sessionHandler = SessionHandler.create(sessionStore);
    router.route().handler(sessionHandler);

    // used for backend calls with bearer token
    final WebClient webClient = WebClient.create(vertx);

    final String hostname = System.getProperty("http.host", "localhost");
    final int port = Integer.getInteger("http.port", 8080);
    final String baseUrl = String.format("http://%s:%d", hostname, port);
    final String oAuthCallbackPath = "/callback";

    final OAuth2ClientOptions clientOptions = new OAuth2ClientOptions()
      .setFlow(OAuth2FlowType.AUTH_CODE)
      .setSite(System.getProperty("oauth2.issuer", "http://192.168.99.100:8080/auth/realms/sesame"))
      .setClientID(System.getProperty("oauth2.client_id", "vertx-client"))
      .setClientSecret(System.getProperty("oauth2.client_secret", "b4130a2d-9a68-4d04-8f6c-c16438598cd6"));

    final HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

    KeycloakAuth.discover(vertx, clientOptions, oAuth2AuthAsyncResult -> {
      final OAuth2Auth oAuth2Auth = oAuth2AuthAsyncResult.result();

      if (oAuth2Auth == null) {
        throw new RuntimeException("Could not configure Keycloak integration via OpenID connect discovery endpoint. Is Keycloak running?");
      }

      final AuthHandler oAuth2 = OAuth2AuthHandler.create(oAuth2Auth, baseUrl + oAuthCallbackPath)
        .setupCallback(router.get(oAuthCallbackPath))
        .addAuthority("openid");

      sessionHandler.setAuthProvider(oAuth2Auth);
      router.route("/protected/*").handler(oAuth2);
      router.get("/health").handler(healthCheckHandler);
      configureRoutes(router, webClient, oAuth2Auth);
    });

    final String pgConnectionUri = System.getProperty("pg.uri", "postgresql://postgres:postgres@192.168.99.100:5432/sesame");

    final PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(Integer.getInteger("pg.pool_size", 5));

    pgPool = PgPool.pool(vertx, pgConnectionUri, poolOptions);

    CircuitBreaker.create("sesame-circuit-breaker", vertx,
      new CircuitBreakerOptions()
        .setMaxFailures(5)
        .setTimeout(2000)
        .setFallbackOnFailure(true)
        .setResetTimeout(10000)
    );

    return vertx
      .createHttpServer()
      .requestHandler(router)
      .rxListen(port)
      .doOnSuccess(c -> LOGGER.info("Graph service successfully started on port " + c.actualPort()))
      .ignoreElement();
  }

  @Override
  public Completable rxStop() {
    pgPool.close();
    return super.rxStop();
  }

  private void configureRoutes(final Router aRouter, final WebClient aWebClient, final OAuth2Auth aOAuth2Auth) {
    LOGGER.trace("Started setting up routes");

    aRouter.get("/").handler(PageController.create(vertx));

    final String schema = vertx.fileSystem().readFileBlocking("organizations.graphql").toString();
    final SchemaParser schemaParser = new SchemaParser();
    final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    final RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .type("Query", builder ->
        builder
          .dataFetcher("organizations", this::getOrganizations)
      )
      .build();

    final SchemaGenerator schemaGenerator = new SchemaGenerator();
    final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    final GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

    aRouter.post("/graphql").handler(GraphQLHandler.create(graphQL));

    final GraphiQLHandlerOptions options = new GraphiQLHandlerOptions().setEnabled(true);
    aRouter.route("/graphiql/*").handler(GraphiQLHandler.create(options));

    aRouter.get("/protected/user").handler(this::handleUserPage);
    aRouter.get("/protected/admin").handler(this::handleAdminPage);

    final String userInfoUrl = ((OAuth2AuthProviderImpl) aOAuth2Auth.getDelegate()).getConfig().getUserInfoPath();
    aRouter.get("/protected/userinfo").handler(createUserInfoHandler(aWebClient, userInfoUrl));

    aRouter.get("/logout").handler(this::handleLogout);
  }

  private DataFetcher<CompletionStage<List<Organization>>> getOrganizations(final DataFetchingEnvironment aDataFetchingEnvironment) {
    final Single<List<Organization>> data = pgPool.preparedQuery("SELECT id, name, description FROM organizations")
      .rxExecute()
      .map(Organization::fromRowSet);

    return e -> data.to(SingleInterop.get());
  }

  private void handleUserPage(final RoutingContext aContext) {
    final OAuth2TokenImpl user = (OAuth2TokenImpl) aContext.user().getDelegate();

    final String username = user.idToken().getString("preferred_username");
    final JsonObject base = new JsonObject()
      .put("success", true)
      .put("message", username);
    respondWithJson(aContext, OK.code(), base.encode());
  }

  private void handleAdminPage(final RoutingContext aContext) {
    final OAuth2TokenImpl user = (OAuth2TokenImpl) aContext.user().getDelegate();

    user.isAuthorized("realm:admin", res -> {
      if (res.succeeded() || !res.result()) {
        final JsonObject base = new JsonObject()
          .put("success", false)
          .put("message", "Unable to access the specific location");
        respondWithJson(aContext, FORBIDDEN.code(), base.encode());
        return;
      }

      final String username = user.idToken().getString("preferred_username");
      final JsonObject base = new JsonObject()
        .put("success", true)
        .put("message", username);
      respondWithJson(aContext, OK.code(), base.encode());
    });
  }

  private void handleLogout(final RoutingContext aContext) {
    final OAuth2TokenImpl oAuth2Token = (OAuth2TokenImpl) aContext.user().getDelegate();
    oAuth2Token.logout(res -> {
      if (res.succeeded()) {
        final JsonObject base = new JsonObject()
          .put("success", false)
          .put("message", "An error occurred on the backend");
        respondWithJson(aContext, INTERNAL_SERVER_ERROR.code(), base.encode());
        return;
      }

      aContext.session().destroy();
      aContext.response()
        .putHeader("location", "/?logout=true")
        .setStatusCode(FOUND.code())
        .end();
    });
  }

  private Handler<RoutingContext> createUserInfoHandler(final WebClient aWebClient, final String aUserInfoUrl) {
    return (final RoutingContext context) -> {
      final OAuth2TokenImpl user = (OAuth2TokenImpl) context.user().getDelegate();
      final URI userInfoEndpointUri = URI.create(aUserInfoUrl);

      aWebClient
        .get(userInfoEndpointUri.getPort(), userInfoEndpointUri.getHost(), userInfoEndpointUri.getPath())
        .bearerTokenAuthentication(user.opaqueAccessToken())
        .as(BodyCodec.jsonObject())
        .rxSend()
        .subscribe(resp -> {
          respondWithJson(context, OK.code(), resp.body().encode());
        }, e -> {
          final JsonObject base = new JsonObject()
            .put("success", false)
            .put("message", "An error occurred on the backend: " + e.toString());
          LOGGER.error("Fatal error occurred", e);
          respondWithJson(context, BAD_REQUEST.code(), base.encode());
        });
    };
  }
}
