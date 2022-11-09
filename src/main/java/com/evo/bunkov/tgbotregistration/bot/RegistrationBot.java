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

import java.util.List;
import java.util.Optional;

@Component
public class RegistrationBot extends TelegramLongPollingBot {

    private final String botUsername;

    private final String botToken;

    private final ClubMemberService service;

    private Message requestMessage = new Message();

    private final Long THE_MIKHAILZ_CHATID = 1434658083L;

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

        ClubMember member = service.createNewIfNotExistOrFindExist(chatId);
        if (Optional.ofNullable(member.getInfo()).isEmpty()) {
            defaultMsg(response, "Бот для вас недоступен. Вы не успели на поток регистрации. Следующий поток ожидается весной");
        }
        if (chatId.equals(THE_MIKHAILZ_CHATID) && requestMessage.getText().startsWith("/")) {
            adminCommandProvider(requestMessage, response);
        }

        if (requestMessage.getText().equals("/start")) {
            startMsg(requestMessage, response, member);
        } else if (requestMessage.getText().startsWith("/reg")) {
            regMsg(requestMessage, response, member);
//            defaultMsg(response, "Регистрация до весны не доступна ");
        } else if (requestMessage.getText().startsWith("/select ")) {
            String permissions = member.getInfo().getPermissions();
            if (permissions.equals("REGISTERED")) {
                courseSelectionMsg(requestMessage, response, member);
            } else {
                defaultMsg(response, "Смена направления не доступна. У вас уже выбрано направление: " + permissions);
            }
        } else {
            defaultMsg(response, "Команда не распознана. Список доступных команд:\n" +
                                 "/reg - регистрация(до весны не доступна)\n" +
                                 "/select <направления> - указание направления\n" +
                                 "Пока все ._.");
        }

    }

    private void courseSelectionMsg(Message requestMessage, SendMessage response, ClubMember member) throws TelegramApiException {
        String messageText = requestMessage.getText().substring("/select ".length());
        if (messageText.toUpperCase().contains("BACKEND C#")
            || messageText.toUpperCase().contains("FRONTEND")
            || messageText.toUpperCase().contains("BACKEND JAVA")
            || messageText.toUpperCase().contains("MOBILE")) {

            ClubMember clubMember = service.selectPermissions(member.getChatId(), messageText);
            System.out.println(clubMember);
            defaultMsg(response, "Информация сохранена. Направления:" + clubMember.getInfo().getPermissions());
        } else {
            defaultMsg(response, "Не удалось распознать направление, которые вы указываете. Возможно вы допустили ошибку. " +
                                 "Убедитесь что вы после написания команды '/select' через пробел корректно написали " +
                                 "выбранное(ые) вами направление(я). Пример команды. '/select backend java, mobile'");
        }
    }

    private void adminCommandProvider(Message requestMessage, SendMessage response) throws TelegramApiException {
        if (requestMessage.getText().equals("/sendNotificationToUndefinedUsers")) {
            sendMailsToUndefinedUsers(response);
        }
    }

    private void sendMailsToUndefinedUsers(SendMessage response) throws TelegramApiException {
        List<ClubMember> list = service.findUndefinedPersons();
        for (ClubMember clubMember : list) {
            System.out.println(clubMember.toString());
        }
        for (ClubMember member : list) {
            response.setText("Укажите ваше направление обучения в клубе. " +
                             "Напишите боту сообщение начав с /select  название направления/направлений. Можно несколько, через запятую пожалуйста перечислите. " +
                             "Их всего 4. frontend, backend c#, backend java, mobile. " +
                             "Пример: /select frontend, backend c#");
            response.setChatId(member.getChatId());
            execute(response);
        }
    }

    private void regMsg(Message requestMessage, SendMessage response, ClubMember member) throws TelegramApiException {
        if (service.isPersonInfoFill(member.getChatId())) {
//            response.setText("Дальнейшее общение с ботом вам ни к чему. Данный бот нужен только для регистрации и уведомлении вас о собеседовании.\n" +
//                             "Если вы допустили ошибку при написании запрашиваемых данных, напишите @The_Mikhailz");
            response.setText("Вы уже внесли информацию для регистрации.");
            execute(response);
            return;
        }

        String text = requestMessage.getText().substring("/reg ".length());

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
            service.setPermissionsOnRegistered(member.getChatId());
            defaultMsg(response, "Вы внесли все необходимые данные");
        }
        for (String personInfo : filledClubMember.getPerson().toStrings()) {
            response.setText(personInfo);
            execute(response);
        }
        defaultMsg(response, "Вышеуказанная информация зафиксированна.");
        defaultMsg(response, String.format("Спасибо за прохождение регистрации! \n" +
                                           "%s, Вы приглашены на групповое собеседование которое пройдет в 15:00 среду 12 октября. " +
                                           "Собираемся около 325П",
                                           firstName));
    }

    private void startMsg(Message requestMessage, SendMessage response, ClubMember member) throws TelegramApiException {
        if (service.isPersonInfoFill(member.getChatId())) {
            defaultMsg(response, "Вы уже проходили регистрацию.\n" +
                                 "Зарегистрированная информация:");
            for (String personInfo : member.getPerson().toStrings()) {
                response.setText(personInfo);
                execute(response);
            }
        } else {
            defaultMsg(response, "Регистрация не доступна.");
//            defaultMsg(response, "Пройдите регистрацию. Введите команду /reg и после укажите ФИО(полностью), номер телефона, группу точно в заданном порядке, отделяя запятыми каждый пункт.\n" +
//                                 "Пример:\n" +
//                                 "/reg Иванов Иван Иванович, +79999999999, ПО-00\n" +
//                                 "Если у вас отсутствует отчество, вместо него напишите \"нет\"  \n" +
//                                 "Любые совпадения данных из примера с реальностью случайны.");
        }
    }

    private void defaultMsg(SendMessage response, String message) throws TelegramApiException {
        response.setText(message);
        execute(response);
    }
}