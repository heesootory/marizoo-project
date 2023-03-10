package com.marizoo.user.repository.broadcast_repo;

import com.marizoo.user.dto.broadcast_dto.RelatedBroadcastDto;
import com.marizoo.user.entity.Broadcast;

import java.util.List;

public interface BroadcastRepositoryCustom {
    // onair 방송에 대해 종 검색
    List<Broadcast> searchOnAirsHavingSpecies(String input);

    // 현재 방송과 관련된 방송 목록
    List<RelatedBroadcastDto> searchBroadcastRelated(Long broadcastId, List<String> classifications);
}
