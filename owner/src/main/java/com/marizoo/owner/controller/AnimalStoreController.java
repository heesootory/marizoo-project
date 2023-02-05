package com.marizoo.owner.controller;

import com.marizoo.owner.api.response.AnimalListResponse;
import com.marizoo.owner.service.AnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnimalStoreController {
    private final AnimalService animalService;
    // 해당 가게 있는 동물 목록 조회.
    @GetMapping("/stores/{store_id}/animals")
    public ResponseEntity<?> getOwnedAnimal(@PathVariable(name = "store_id") Long store_id){
        AnimalListResponse OwnedAnimalList = new AnimalListResponse(animalService.findOwnedAnimal(store_id));
        return new ResponseEntity<>(OwnedAnimalList, HttpStatus.OK);
    }

}
