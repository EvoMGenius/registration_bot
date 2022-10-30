package com.evo.bunkov.tgbotregistration.service;

import com.evo.bunkov.tgbotregistration.model.ClubInfo;
import com.evo.bunkov.tgbotregistration.model.ClubMember;
import com.evo.bunkov.tgbotregistration.model.Person;
import com.evo.bunkov.tgbotregistration.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubMemberServiceImpl implements ClubMemberService {

    private final ClubMemberRepository repository;

    @Override
    @Transactional
    public boolean isPersonInfoFill(long chatId) {
        ClubMember member = repository.findByChatId(chatId);
        Person person = member.getPerson();
        if (person == null) {
            return false;
        }

        if (person.getFirstName().isEmpty() || person.getFirstName().isBlank() || person.getFirstName() == null) {
            return false;
        }

        if (person.getLastName().isEmpty() || person.getLastName().isBlank() || person.getLastName() == null) {
            return false;
        }

        if (person.getMiddleName().isEmpty() || person.getMiddleName().isBlank() || person.getMiddleName() == null) {
            return false;
        }

        member.setInfo(ClubInfo.builder()
                .permissions("REGISTERED")
                .build());
        repository.save(member);
        return true;
    }

    @Override
    @Transactional
    public ClubMember fillPersonInfoByChatId(long chatId, Person person) {
        ClubMember member = repository.findByChatId(chatId);

        member.setPerson(person);

        return repository.save(member);
    }

    @Override
    @Transactional
    public ClubMember fillPersonInfoById(UUID id, Person person) {
        ClubMember member = getExisting(id);
        member.setPerson(person);

        return repository.save(member);
    }

    @Override
    @Transactional
    public ClubMember fillInfo(ClubMember newClubMemberInfo) {
        ClubMember oldClubMemberInfo = getExisting(newClubMemberInfo.getId());
        Person oldPerson = oldClubMemberInfo.getPerson();
        Person newPerson = newClubMemberInfo.getPerson();
        if (oldPerson.getFirstName() == null || oldPerson.getFirstName().isEmpty()) {
            if (newPerson.getFirstName() != null && !newPerson.getFirstName().isEmpty() && !newPerson.getFirstName().isBlank()) {
                oldPerson.setFirstName(newPerson.getFirstName());
            }
        }
        if (oldPerson.getLastName() == null || oldPerson.getLastName().isEmpty() || oldPerson.getLastName().isBlank()) {
            if (newPerson.getLastName() != null && !newPerson.getLastName().isEmpty() && !newPerson.getLastName().isBlank()) {
                oldPerson.setLastName(newPerson.getLastName());
            }
        }
        if (oldPerson.getMiddleName() == null || oldPerson.getMiddleName().isEmpty() || oldPerson.getMiddleName().isBlank()) {
            if (newPerson.getMiddleName() != null && !newPerson.getMiddleName().isEmpty() && !newPerson.getMiddleName().isBlank()) {
                oldPerson.setMiddleName(newPerson.getMiddleName());
            }
        }
        return repository.save(ClubMember.builder()
                .id(oldClubMemberInfo.getId())
                .chatId(oldClubMemberInfo.getChatId())
                .person(oldPerson)
                .createTime(oldClubMemberInfo.getCreateTime())
                .build());
    }

    @Transactional
    public ClubMember getExisting(UUID id) {
        return repository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Override
    @Transactional
    public ClubMember createNewIfNotExistOrFindExist(long chatId) {
        ClubMember member = repository.findByChatId(chatId);
        if (member == null) {
            LocalDateTime now = LocalDateTime.now();
            System.out.printf("Написал новый пользователь в %s, chatId = %d%n", now, chatId);
            member = repository.save(ClubMember.builder()
                    .chatId(chatId)
                    .createTime(now)
                    .build());
        }
        return member;
    }

    @Override
    public ClubMember findByPersonInfo(Person person) {
        return repository.findClubMemberByPerson(person);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<ClubMember> findUndefinedPersons() {
        return repository.findAllByInfoPermissions("REGISTERED");
    }
}
