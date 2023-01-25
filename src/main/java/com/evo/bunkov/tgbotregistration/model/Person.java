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
public class Person {

    private String lastName;

    private String firstName;

    private String middleName;

    private String phoneNumber;

    private String educationGroup;

    public String[] toStrings(){
        return new String[]{lastName, firstName, middleName, phoneNumber, educationGroup};
    }
}
