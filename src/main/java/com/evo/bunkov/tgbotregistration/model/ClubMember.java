package com.evo.bunkov.tgbotregistration.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClubMember {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private long chatId;

    @Embedded
    private Person person;

    @Embedded
    private ClubInfo info;

    @Setter(value = AccessLevel.NONE)
    private LocalDateTime createTime;
}
