package com.evo.bunkov.tgbotregistration.repository;

import com.evo.bunkov.tgbotregistration.model.ClubMember;
import com.evo.bunkov.tgbotregistration.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClubMemberRepository extends JpaRepository<ClubMember, UUID> {

    ClubMember findClubMemberByPerson(Person person);

    ClubMember findByChatId(long id);

    List<ClubMember> findAllByInfoPermissions(String info);
}
