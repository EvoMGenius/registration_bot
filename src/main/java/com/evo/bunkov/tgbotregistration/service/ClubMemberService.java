package com.evo.bunkov.tgbotregistration.service;

import com.evo.bunkov.tgbotregistration.model.ClubMember;
import com.evo.bunkov.tgbotregistration.model.Person;

import java.util.UUID;

public interface ClubMemberService {

    boolean isPersonInfoFill(long chatId);

    ClubMember fillPersonInfoByChatId(long chatId, Person person);

    ClubMember fillPersonInfoById(UUID id, Person person);

    ClubMember fillInfo(ClubMember clubMember);

    ClubMember createNewIfNotExistOrFindExist(long chatId);

    ClubMember findByPersonInfo(Person person);

    void deleteById(UUID id);
}
