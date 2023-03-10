package com.marizoo.user.controller;

import com.marizoo.user.api.animalstore_api.*;
import com.marizoo.user.dto.animal_dto.AnimalDetailDto;
import com.marizoo.user.dto.animal_dto.AnimalDto;
import com.marizoo.user.dto.animalstore_dto.AnimalStoreDto;
import com.marizoo.user.dto.animalstore_dto.AnimalStoreWholeDto;
import com.marizoo.user.dto.animalstore_dto.StoreSubDto;
import com.marizoo.user.dto.broadcast_dto.AnimalBroadcastStatusDto;
import com.marizoo.user.dto.broadcast_dto.BroadcastStatusDto;
import com.marizoo.user.dto.broadcast_dto.BroadcastsDto;
import com.marizoo.user.dto.feed_dto.FeedDto;
import com.marizoo.user.dto.play_dto.StorePlayDto;
import com.marizoo.user.dto.species_dto.SpeciesWholeDto;
import com.marizoo.user.entity.*;
import com.marizoo.user.exception.PlayReservationCloasedException;
import com.marizoo.user.service.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user/")
public class StoreController {
    private final AnimalStoreService animalStoreService;
    private final AnimalService animalService;
    private final BroadcastService broadcastService;
    private final FeedService feedService;
    private final SpeciesService speciesService;
    private final ReservationService reservationService;

    @ApiOperation(value = "동물 가게 목록 가져오기")
    @GetMapping("/stores")
    public ResponseEntity<AnimalStoreListResponse> storeList(){
        List<AnimalStore> AnimalStoreList = animalStoreService.findAnimalStores();
        List<AnimalStoreDto> AnimalStoreDtoList = AnimalStoreList.stream()
                .map(as -> new AnimalStoreDto(
                        as.getId(),
                        as.getStoreName(),
                        as.getTel(),
                        as.getAddress(),
                        as.getProfileImg(),
                        as.getLat(),
                        as.getLng())).collect(Collectors.toList());

        return new ResponseEntity<>( new AnimalStoreListResponse(AnimalStoreDtoList), HttpStatus.OK);
    }

    @ApiOperation(value = "[storename, classification]으로 검색한 동물 가게 목록 가져오기")
    @GetMapping("/stores/search")
    public ResponseEntity<AnimalStoreListResponse> store_search(
            @RequestParam(name = "storename", required = false, defaultValue = "") @ApiParam(name = "가게명 검색어", required = false)String storename,
            @RequestParam(name = "classification", required = false, defaultValue = "") @ApiParam(name = "종 검색어", required = false)String species){

        List<AnimalStore> storeList = new ArrayList<>();
        List<AnimalStoreDto> AnimalStoreDtoList;

        if(storename.length() == 0 && species.length() == 0){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }else{
            // 상호명 검색
            if(storename.length() != 0 && species.length() == 0){
                storeList = animalStoreService.findAnimalStoresbyNameSearch(storename);
            }
            // 종 검색
            if(storename.length() == 0 && species.length() != 0){
                storeList = animalStoreService.findAnimalStoresbySpeciesSearch(species);
            }

            // 검색 결과 API 객체로 변환.
            AnimalStoreDtoList = storeList.stream()
                    .map(as -> new AnimalStoreDto(
                            as.getId(),
                            as.getStoreName(),
                            as.getTel(),
                            as.getAddress(),
                            as.getProfileImg(),
                            as.getLat(),
                            as.getLng())).collect(Collectors.toList());

            return new ResponseEntity<>(new AnimalStoreListResponse(AnimalStoreDtoList), HttpStatus.OK);
        }
    }

    // 팔로우
    @ApiOperation(value = "store_id 동물 가게를 follow")
    @PostMapping("/stores/{store_id}")
    public ResponseEntity<String> follow(@PathVariable(name = "store_id") @ApiParam(name = "동물가게 id", required = true, example = "1") Long store_id,
                                         @RequestBody FollowRequest followRequest) {
        animalStoreService.followingStore(store_id, followRequest.getUid());
        return new ResponseEntity<>("성공", HttpStatus.OK);
    }


    // 해당 가게 전체 정보 제공
    @ApiOperation(value = "store_id 동물 가게 정보 가져오기")
    @GetMapping("/stores/{store_id}")
    public ResponseEntity<AnimalStoreWholeDto> reserveToStore(
            @PathVariable(name = "store_id") @ApiParam(name = "동물가게 id", required = true, example = "1") Long store_id,
            @RequestParam(required = false, defaultValue = "0") Long userId){
        log.info("가게 조회 메서드");
        return new ResponseEntity<>(animalStoreService.findAnimalStore(store_id, userId), HttpStatus.OK);
    }

    // 해당 가게 있는 동물 목록 조회.
    @ApiOperation(value = "store_id가 보유한 동물 목록 가져오기")
    @GetMapping("/stores/{store_id}/animals")
    public ResponseEntity<AnimalListResponse> getOwnedAnimal(@PathVariable(name = "store_id") @ApiParam(name = "동물가게 id", required = true, example = "1") Long store_id){
        AnimalListResponse OwnedAnimalList = new AnimalListResponse(animalService.findOwnedAnimal(store_id));
        return new ResponseEntity<>(OwnedAnimalList, HttpStatus.OK);
    }

    // 스트리밍 중인 영상 목록 조회
    @ApiOperation(value = "store_id가 방송 중인 영상 목록 가져오기")
    @GetMapping("/stores/{store_id}/broadcasts")
    public ResponseEntity<BroadcastListResponse> getOnairBroadcast(@PathVariable(name = "store_id") @ApiParam(name = "동물가게 id", required = true, example = "1") Long store_id){
        List<Broadcast> onairs = broadcastService.getOnAirs();
        List<BroadcastsDto> result = new ArrayList<>();
        for (Broadcast onair : onairs) {
            if(onair.getAnimalStore().getId() == store_id){
                List<String> classificationImgs = new ArrayList<>();
                for (BroadcastAnimal broadcastAnimal : onair.getBroadcastAnimalList()) {
                    classificationImgs.add(broadcastAnimal.getAnimal().getSpecies().getClassificationImg());
                }
                result.add(new BroadcastsDto(onair.getId(), onair.getSessionId(), onair.getTitle(), onair.getThumbnail(), classificationImgs));
            }
        }
        return new ResponseEntity<>(new BroadcastListResponse(result), HttpStatus.OK);
    }


    // 가게 체험 프로그램 목록 제공
    @ApiOperation(value = "store_id가 진행하는 가게 체험 프로그램 목록 가져오기")
    @GetMapping("/stores/{store_id}/plays")
    public ResponseEntity<StorePlayListResponse> getPlayInProgress(@PathVariable(name = "store_id") @ApiParam(name = "동물가게 id", required = true, example = "1") Long store_id){
        List<Play> playlist = animalStoreService.findPlayByStore(store_id);
        List<StorePlayDto> playDtoList = playlist.stream()
                .map(m -> new StorePlayDto(
                        m.getId(),
                        m.getPlayDateTime(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getImg())).collect(Collectors.toList());

        return new ResponseEntity<>(new StorePlayListResponse(playDtoList), HttpStatus.OK);
    }

    // 체험 프로그램에 대한 정보 제공.
    @ApiOperation(value = "store_id 가게가 제공하는 play_id 체험 프로그램에 대한 정보 가져오기")
    @GetMapping("/stores/{store_id}/plays/{play_id}")
    public ResponseEntity<?> getPlayInfo(@PathVariable(name = "store_id") @ApiParam(name = "동물가게 id", required = true, example = "1") Long storeId,
                                                                @PathVariable(name = "play_id") @ApiParam(name = "체험 프로그램 id", required = true, example = "1") Long playId){

        PlayAndStoreInfoResponse playInfo = animalStoreService.findPlayInfo(storeId, playId);

        if(playInfo.getStoreInfo() == null){
            return new ResponseEntity<>("예약 불가", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(playInfo, HttpStatus.OK);
    }


    // 체험 프로그램 예약
    @PostMapping("/stores/books")
    public ResponseEntity postReservation(@RequestBody ReservationRequest reservationRequest){
        Long usersPlayId = reservationService.reserve(reservationRequest.getUid(), reservationRequest.getPlayId(), reservationRequest.getTotalVisitor());
        if(usersPlayId != null){
            return new ResponseEntity<>(usersPlayId, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("예약 실패",HttpStatus.BAD_REQUEST);
        }
    }

    // 동물 상세 정보 제공.
    @ApiOperation(value = "animal_id 동물 상세 정보 가져오기")
    @GetMapping("/stores/{animal_id}/animal_detail")
    public ResponseEntity<AnimalDetailResponse> getAnimalInfo(@PathVariable(name = "animal_id") @ApiParam(name = "동물 id", required = true, example = "1") Long animalId){
        AnimalDetailDto aDDto = animalService.getAnimalDetail(animalId);
        AnimalBroadcastStatusDto animalBroadcastStatusDto = animalService.getBroadcastStatus(animalId);
        List<FeedDto> feeds = feedService.findFeedListforAnimal(animalId);

        if(aDDto == null || animalBroadcastStatusDto == null || feeds == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("!!!!" + aDDto.getAnimal().toString());
        log.info("!!!!" + aDDto.getAnimalStore().toString());
        log.info("!!!!" + aDDto.getSpecies().toString());
        log.info("!!!!" + animalBroadcastStatusDto.isOnAir());
        log.info("!!!!" + feeds.toString());

        Animal animal = aDDto.getAnimal();
        AnimalStore animalStore = aDDto.getAnimalStore();;
        Species species = aDDto.getSpecies();
        AnimalDetailResponse animalDetailResponse = new AnimalDetailResponse(
                new AnimalDto(animal.getName(), animal.getGender(), animal.getFeature(), animal.getLength(), animal.getWeight(), animal.getAge(), animal.getImg()),
                new StoreSubDto(animalStore.getId(), animalStore.getStoreName(), animalStore.getProfileImg()),
                new SpeciesWholeDto(species.getId(), species.getHabitat(), species.getClassification(), species.getLifeSpan(), species.getInfo(), species.getClassificationImg()),
                animalBroadcastStatusDto,
                feeds
        );

        return new ResponseEntity(animalDetailResponse, HttpStatus.OK);
    }
    
    // 동물 종 정보 제공
    @ApiOperation(value = "species_id에 해당하는 종 정보 가져오기")
    @GetMapping("/stores/{species_id}/species_detail")
    public ResponseEntity<SpeciesDetailResponse> getSpeciesInfo(@PathVariable(name = "species_id")  @ApiParam(name = "종 id", required = true, example = "1")Long speciesId){
        SpeciesDetailResponse speciesDetailResponse = new SpeciesDetailResponse(
                speciesService.findSpeciesInfo(speciesId),
                feedService.findFeedListforSpecies(speciesId)
        );
        return new ResponseEntity<>(speciesDetailResponse, HttpStatus.OK);
    }

    // 예약 정보 제공
    @ApiOperation(value = "book_id에 해당하는 예약 내역 가져오기")
    @GetMapping("/stores/books/{book_id}")
    public ResponseEntity<ReservationResponse> showReservationInfo(@PathVariable(name = "book_id") @ApiParam(name = "예약 id", required = true, example = "1") Long bookId){
        return new ResponseEntity<>(reservationService.getReservatonResult(bookId), HttpStatus.OK);
    }


}













