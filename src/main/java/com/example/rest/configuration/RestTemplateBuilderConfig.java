package com.example.rest.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateBuilderConfig {
    @Value("${rest.root.url}")
    private String ROOT_URL;

    @Bean
    OAuth2AuthorizedClientManager auth2AuthorizedClientManager(ClientRegistrationRepository repository,
                                                               OAuth2AuthorizedClientService service) {
        var providerBuilder = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repository, service);
        clientManager.setAuthorizedClientProvider(providerBuilder);
        return clientManager;
    }

    @Bean
    RestTemplateBuilder builder(RestTemplateBuilderConfigurer config,OAuthClientInterceptor interceptor) {
        return config.configure(new RestTemplateBuilder())
                .additionalInterceptors(interceptor)
                .uriTemplateHandler(new DefaultUriBuilderFactory(ROOT_URL));
    }
}
