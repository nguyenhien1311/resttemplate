package com.example.rest.client;

import com.example.rest.model.BeerDTO;
import com.example.rest.model.BeerDTOPageImpl;
import com.example.rest.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerClientImpl implements BeerClient {
    private final RestTemplateBuilder builder;
    public static final String BEER_WITH_ID = "/{id}";

    @Override
    public Page<BeerDTO> findAll() {
        RestTemplate template = builder.build();
        UriComponentsBuilder path = UriComponentsBuilder.fromPath("");
        ResponseEntity<BeerDTOPageImpl> response = template.getForEntity(path.toUriString(), BeerDTOPageImpl.class);
        return response.getBody();
    }

    @Override
    public Page<BeerDTO> findAll(String name, BeerStyle style, Integer pageNumber, Integer pageSize, Boolean showInventory) {
        RestTemplate template = builder.build();
        UriComponentsBuilder path = UriComponentsBuilder.fromPath("");
        if (StringUtils.hasLength(name)) {
            path.queryParam("beerName", name);
        }
        if (style != null) {
            path.queryParam("style", style);
        }
        if (pageNumber != null) {
            path.queryParam("pageNumber", pageNumber);
        }
        if (pageSize != null) {
            path.queryParam("pageSize", pageSize);
        }
        if (showInventory) {
            path.queryParam("showInventory", true);
        }
        ResponseEntity<BeerDTOPageImpl> response = template.getForEntity(path.toUriString(), BeerDTOPageImpl.class);
        return response.getBody();
    }

    @Override
    public BeerDTO findById(UUID id) {
        RestTemplate template = builder.build();


        return template.getForObject(BEER_WITH_ID, BeerDTO.class, id);
    }

    @Override
    public BeerDTO addBeer(BeerDTO dto) {
        RestTemplate template = builder.build();
        URI uri = template.postForLocation("", dto);
        return template.getForObject(uri.getPath(), BeerDTO.class);
    }

    @Override
    public BeerDTO updateBeer(UUID id, BeerDTO dto) {
        RestTemplate template = builder.build();
        template.put(BEER_WITH_ID, dto, id);
        return findById(id);
    }

    @Override
    public void deleteBeer(UUID id) {
        RestTemplate template = builder.build();
        template.delete(BEER_WITH_ID, id);
    }
}
