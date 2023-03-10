package com.marizoo.user.dto.broadcast_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class onAirAnimalStoreDto {
    private Long id;
    private String name;
    private String profile;
}
