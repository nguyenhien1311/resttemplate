package com.example.rest.client;

import com.example.rest.configuration.OAuthClientInterceptor;
import com.example.rest.configuration.RestTemplateBuilderConfig;
import com.example.rest.model.BeerDTO;
import com.example.rest.model.BeerDTOPageImpl;
import com.example.rest.model.BeerStyle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
class BeerMockTest {
    static final String HOST = "http://localhost:8080/api/v1/beers";

    @Mock
    RestTemplateBuilder mockTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());
    @Autowired
    ObjectMapper mapper;
    @Autowired
    RestTemplateBuilder builder;
    BeerClient client;
    MockRestServiceServer server;

    BeerDTO dummy;
    String payload;

    @MockBean
    OAuth2AuthorizedClientManager manager;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        ClientRegistrationRepository repository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration
                    .withRegistrationId("springauth")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("test")
                    .tokenUri("test")
                    .build());
        }

        @Bean
        OAuth2AuthorizedClientService service(ClientRegistrationRepository repository) {
            return new InMemoryOAuth2AuthorizedClientService(repository);
        }

        @Bean
        OAuthClientInterceptor OAuthClientInterceptor(OAuth2AuthorizedClientManager manager, ClientRegistrationRepository repository) {
            return new OAuthClientInterceptor(manager, repository);
        }
    }

    @Autowired
    ClientRegistrationRepository repository;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ClientRegistration clientRegistration = repository.findByRegistrationId("springauth");
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "test", Instant.MIN, Instant.MAX);
        when(manager.authorize(any())).thenReturn(new OAuth2AuthorizedClient(clientRegistration, "test", token));
        RestTemplate template = builder.build();
        server = MockRestServiceServer.bindTo(template).build();
        when(mockTemplateBuilder.build()).thenReturn(template);
        client = new BeerClientImpl(mockTemplateBuilder);
        dummy = getBeerDTO();
        payload = mapper.writeValueAsString(dummy);
    }

    @Test
    void findAllWithQuery() throws JsonProcessingException {
        String payload = mapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(HOST)
                .queryParam("beerName", "Ale")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(header("Authorization", "Bearer test"))
                .andExpect(queryParam("beerName", "Ale"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> list = client.findAll("Ale", null, null, null, false);
        assertThat(list.getContent().size()).isGreaterThan(0);

    }

    @Test
    void findAll() throws JsonProcessingException {
        String payload = mapper.writeValueAsString(getPage());
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(HOST))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> list = client.findAll();
        assertThat(list.getContent().size()).isGreaterThan(0);

    }

    @Test
    void findById() {
        mockGetOperation();

        BeerDTO dto = client.findById(dummy.getId());

        assertThat(dto.getId()).isEqualTo(dummy.getId());
    }

    @Test
    void addBeer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.BEER_WITH_ID).build(dummy.getId());
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(HOST))
                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO beerDTO = client.addBeer(dummy);
        assertThat(beerDTO.getId()).isEqualTo(dummy.getId());
    }


    @Test
    void updateBeer() {
        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(HOST + BeerClientImpl.BEER_WITH_ID, dummy.getId()))
                .andRespond(withNoContent());
        mockGetOperation();

        dummy.setBeerName("Update mock cac thu");

        BeerDTO beerDTO = client.updateBeer(dummy.getId(), dummy);

        assertThat(beerDTO.getId()).isEqualTo(dummy.getId());
    }

    @Test
    void deleteBeer() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(HOST + BeerClientImpl.BEER_WITH_ID,
                        dummy.getId()))
                .andRespond(withNoContent());
        client.deleteBeer(dummy.getId());

        server.verify();
    }

    @Test
    void deleteBeerNotFound() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(HOST + BeerClientImpl.BEER_WITH_ID,
                        dummy.getId()))
                .andRespond(withResourceNotFound());
        assertThrows(HttpClientErrorException.class, () -> {
            client.deleteBeer(dummy.getId());
        });
        server.verify();
    }


    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(HOST + "/" + dummy.getId()))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
    }

    BeerDTO getBeerDTO() {
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .beerName("ALoe")
                .price(BigDecimal.TEN)
                .upc("ooooo")
                .quantityOnHand(100)
                .beerStyle(BeerStyle.STOUT)
                .build();
    }

    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Arrays.asList(getBeerDTO()), 1, 25, 1);
    }
}