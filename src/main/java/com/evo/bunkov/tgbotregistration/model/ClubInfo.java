package com.evo.bunkov.tgbotregistration.model;

import lombok.*;

import javax.persistence.Embeddable;

import static lombok.AccessLevel.PRIVATE;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@ToString
public class ClubInfo {

    private String permissions;

}
