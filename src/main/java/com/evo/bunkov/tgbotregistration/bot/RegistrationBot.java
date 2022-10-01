package com.evo.bunkov.tgbotregistration.bot;

import com.evo.bunkov.tgbotregistration.model.ClubMember;
import com.evo.bunkov.tgbotregistration.model.Person;
import com.evo.bunkov.tgbotregistration.service.ClubMemberService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class RegistrationBot extends TelegramLongPollingBot {

    private final String botUsername;

    private final String botToken;

    private final ClubMemberService service;

    private Message requestMessage = new Message();

    private final SendMessage response = new SendMessage();

    public RegistrationBot(@Value("${telegram.bot.username}") String botUsername, @Value("${telegram.bot.token}") String botToken, ClubMemberService service) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.service = service;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    public void onUpdateReceived(Update update) {
        requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();
        response.setChatId(chatId.toString());

        if(chatId == 1795221743){
            response.setText("Idi naxuy pidor");
            execute(response);
            return;
        }

        ClubMember member = service.createNewIfNotExistOrFindExist(chatId);

        if (requestMessage.getText().equals("/start")) {
            startMsg(response, member);
        } else {
            regMsg(response, member);
        }

    }

    private void regMsg(SendMessage response, ClubMember member) throws TelegramApiException {
        if (service.isPersonInfoFill(member.getChatId())) {
            response.setText("Дальнейшее общение с ботом вам ни к чему. Данный бот нужен только для регистрации и уведомлении вас о собеседовании.\n" +
                             "Если вы допустили ошибку при написании запрашиваемых данных, напишите @The_Mikhailz");
            execute(response);
            return;
        }

        String text = requestMessage.getText();

        String[] strings = StringUtils.commaDelimitedListToStringArray(text);
        if (strings.length != 3) {
            response.setText("Вы неверно ввели данные. Вероятно где-то пропустили запятую или поставили лишнюю.");
            execute(response);
            return;
        }

        String[] LFMNames = StringUtils.delimitedListToStringArray(strings[0], " ");
        if (LFMNames.length != 3) {
            response.setText("Вы неверно ввели ФИО. Вероятно что-то не дописали.");
            execute(response);
            return;
        }

        String phoneNumber = strings[1].trim();
        if (phoneNumber.length() != 12) {
            response.setText("Вы неверно ввели номер телефона. Пример +79999999999.");
            execute(response);
            return;
        }

        String educationGroup = strings[2].trim();

        String lastName = LFMNames[0].trim();
        String firstName = LFMNames[1].trim();
        String middleName = LFMNames[2].trim();

        ClubMember filledClubMember = service.fillPersonInfoById(member.getId(),
                                                                 Person.builder()
                                                                       .lastName(lastName)
                                                                       .firstName(firstName)
                                                                       .middleName(middleName)
                                                                       .phoneNumber(phoneNumber)
                                                                       .educationGroup(educationGroup)
                                                                       .build());
        if (service.isPersonInfoFill(member.getChatId())) {
            defaultMsg(response, "Вы внесли все необходимые данные");
        }
        for (String personInfo : filledClubMember.getPerson().toStrings()) {
            response.setText(personInfo);
            execute(response);
        }
        defaultMsg(response, "Вышеуказанная информация зафиксированна.");
        defaultMsg(response, String.format("Спасибо за прохождение регистрации! \n" +
                                           "%s, Вы приглашены на групповое собеседование которое пройдет в 15:00 среду 5 октября в 325п.",
                                           firstName));
    }

    private void startMsg(SendMessage response, ClubMember member) throws TelegramApiException {
        if (service.isPersonInfoFill(member.getChatId())) {
            defaultMsg(response, "Вы уже проходили регистрацию.\n" +
                                 "Зарегистрированная информация:");
            for (String personInfo : member.getPerson().toStrings()) {
                response.setText(personInfo);
                execute(response);
            }
        } else {
            defaultMsg(response, "Пройдите регистрацию. Введите ФИО(полностью), номер телефона, группу точно в заданном порядке, отделяя запятыми каждый пункт.\n" +
                                 "Пример:\n" +
                                 "Иванов Иван Иванович, +79999999999, ПО-00\n" +
                                 "Если у вас отсутствует отчество, вместо него напишите \"нет\"  \n" +
                                 "Любые совпадения данных из примера с реальностью случайны.");
        }
    }

    private void defaultMsg(SendMessage response, String message) throws TelegramApiException {
        response.setText(message);
        execute(response);
    }
}