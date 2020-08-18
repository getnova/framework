package net.getnova.backend.api.handler.rest;

import lombok.Getter;
import net.getnova.backend.api.data.ApiEndpointCollectionData;
import net.getnova.backend.api.data.ApiEndpointData;
import net.getnova.backend.api.parser.ApiEndpointCollectionParser;
import net.getnova.backend.codec.http.server.HttpServerService;
import net.getnova.backend.config.ConfigService;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PostInitService;
import net.getnova.backend.service.event.PostInitServiceEvent;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service(id = "rest-api", depends = {ConfigService.class, HttpServerService.class})
@Singleton
public final class RestApiService {

  private final Set<Object> collections;
  private final RestApiConfig config;
  @Getter
  private Set<ApiEndpointCollectionData> collectionsData;

  @Inject
  private HttpServerService httpServerService;

  @Inject
  private InjectionHandler injectionHandler;

  @Inject
  public RestApiService(final ConfigService configService) {
    this.collections = new LinkedHashSet<>();
    this.config = configService.addConfig("rest-api", new RestApiConfig());
  }

  @PostInitService
  private void postInit(@NotNull final PostInitServiceEvent event) {
    this.collectionsData = ApiEndpointCollectionParser.parseCollections(this.collections);
    final Map<String, ApiEndpointData> endpoints = ApiEndpointCollectionParser.getEndpoints(this.collectionsData);
    this.httpServerService.addLocationProvider(this.config.getPath(), new RestApiLocationProvider(endpoints));
  }

  @NotNull
  public <T> T addEndpointCollection(@NotNull final Class<T> collection) {
    final T instance = this.injectionHandler.getInjector().getInstance(collection);
    this.collections.add(instance);
    return instance;
  }
}