package com.example.rest.client;

import com.example.rest.model.BeerDTO;
import com.example.rest.model.BeerStyle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClient client;

    @Test
    void findAll() {
        client.findAll(null, null, 1, 25, true);
    }

    @Test
    void searchByName() {
        client.findAll("Ale", null, 1, 25, false);
    }

    @Test
    void searchByStyle() {
        client.findAll(null, BeerStyle.SAISON, 1, 25, false);
    }

    @Test
    void getById() {
        BeerDTO beerDTO = client.findAll(null, null, 1, 25, true).getContent().get(0);

        BeerDTO byId = client.findById(beerDTO.getId());

        assertThat(beerDTO.getBeerName()).isEqualTo(byId.getBeerName());
    }

    @Test
    void addBeer() {
        BeerDTO beerDTO = client.addBeer(BeerDTO.builder()
                .beerName("Sp test")
                .beerStyle(BeerStyle.WHEAT)
                .upc("lololo")
                .price(BigDecimal.TEN)
                .quantityOnHand(999)
                .build());

        assertThat(beerDTO).isNotNull();
    }

    @Test
    void updateBeer() {
        BeerDTO beerDTO = client.findAll(null, null, 1, 25, true).getContent().get(0);

        beerDTO.setBeerName("Alola 2");
        beerDTO.setUpc("oh shiet!");

        BeerDTO updateBeer = client.updateBeer(beerDTO.getId(), beerDTO);

        assertThat(updateBeer.getBeerName()).isEqualTo("Alola 2");
    }

    @Test
    void deleteBeer() {
        BeerDTO beerDTO = client.findAll(null, null, 1, 25, true).getContent().get(0);

        client.deleteBeer(beerDTO.getId());

        assertThrows(HttpClientErrorException.class, () -> {
            client.findById(beerDTO.getId());
        });
    }
}