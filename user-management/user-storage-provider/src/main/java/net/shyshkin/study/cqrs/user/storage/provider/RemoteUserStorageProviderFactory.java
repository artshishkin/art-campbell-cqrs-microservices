package net.shyshkin.study.cqrs.user.storage.provider;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class RemoteUserStorageProviderFactory implements UserStorageProviderFactory<RemoteUserStorageProvider> {

    public static final String PROVIDER_NAME = "remote-user-storage-provider";
    public static final String USER_STORAGE_PROVIDER_URI = "http://user-query-api:8080";

    @Override
    public RemoteUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new RemoteUserStorageProvider(session, model, buildHttpClient(USER_STORAGE_PROVIDER_URI));
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    private UsersApiService buildHttpClient(String uri) {
        var client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(uri);

        return target
                .proxyBuilder(UsersApiService.class)
                .classloader(UsersApiService.class.getClassLoader())
                .build();
    }
}
