package com.marizoo.owner.entity;

import com.marizoo.owner.entity.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Play extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_id")
    private Long id;

    private LocalDateTime playDateTime;

    @Enumerated(EnumType.STRING)
    private PlayType playType;

    private String title;

    private String description;

    private Integer maxVisitor;
    private Integer runningTime;

    private String notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_store_id")
    private AnimalStore animalStore;

    private String img;
}
