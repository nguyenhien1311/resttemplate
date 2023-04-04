package com.example.rest.client;

import com.example.rest.model.BeerDTO;
import com.example.rest.model.BeerStyle;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BeerClient {
    Page<BeerDTO> findAll();
    Page<BeerDTO> findAll(String name, BeerStyle style, Integer pageNumber, Integer pageSize, Boolean showInventory);

    BeerDTO findById(UUID beerId);

    BeerDTO addBeer(BeerDTO dto);
    BeerDTO updateBeer(UUID id,BeerDTO dto);

    void deleteBeer(UUID id);
}
