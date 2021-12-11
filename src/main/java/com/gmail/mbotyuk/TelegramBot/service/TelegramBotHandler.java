package com.gmail.mbotyuk.TelegramBot.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ExportChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Component
@Data
@Slf4j
public class TelegramBotHandler extends TelegramLongPollingBot {
    private final String INFO_LABEL = "О чем канал?";
    private final String ACCESS_LABEL = "Как получить доступ?";
    private final String SUCCESS_LABEL = "Дайте полный доступ!";

    private enum COMMANDS {
        INFO("/info"),
        START("/start"),
        ACCESS("/access"),
        SUCCESS("/success");

        private String command;

        COMMANDS(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }

    @Value("${telegram.name}")
    private String name;

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chanel-id}")
    private String privateChannelId;

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            try {
                SendMessage message = getCommandResponse(text, update.getMessage().getFrom(), String.valueOf(chat_id));
                message.enableHtml(true);
                message.setParseMode(ParseMode.HTML);
                message.setChatId(String.valueOf(chat_id));
                execute(message);
            } catch (TelegramApiException e) {
                log.error("", e);
                SendMessage message = handleNotFoundCommand();
                message.setChatId(String.valueOf(chat_id));
            }
        } else if (update.hasCallbackQuery()) {
            try {
                SendMessage message = getCommandResponse(update.getCallbackQuery().getData(), update.getCallbackQuery().getFrom(), String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                message.enableHtml(true);
                message.setParseMode(ParseMode.HTML);
                message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                execute(message);
            } catch (TelegramApiException e) {
                log.error("", e);
            }
        }
    }

    private SendMessage getCommandResponse(String text, User user, String chatId) throws TelegramApiException {
        if (text.equals(COMMANDS.INFO.getCommand())) {
            return handleInfoCommand();
        }

        if (text.equals(COMMANDS.ACCESS.getCommand())) {
            return handleAccessCommand();
        }

        if (text.equals(COMMANDS.SUCCESS.getCommand())) {
            return handleSuccessCommand();
        }

        if (text.equals(COMMANDS.START.getCommand())) {
            return handleStartCommand();
        }

        return handleNotFoundCommand();
    }

    private SendMessage handleNotFoundCommand() {
        SendMessage message = new SendMessage();
        message.setText("Вы что-то сделали не так. Выберите команду:");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private String getChatInviteLink() throws TelegramApiException {
        ExportChatInviteLink exportChatInviteLink = new ExportChatInviteLink();
        exportChatInviteLink.setChatId(privateChannelId);
        return execute(exportChatInviteLink);
    }

    private SendMessage handleStartCommand() {
        SendMessage message = new SendMessage();
        message.setText("Доступные команды:");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage handleInfoCommand() {
        SendMessage message = new SendMessage();
        message.setText("Это канал о самых вкусных пирожочках");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage handleAccessCommand() {
        SendMessage message = new SendMessage();
        message.setText("Чтобы получить полный доступ, вам надо сказать волшебное слово");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage handleSuccessCommand() {
        SendMessage message = new SendMessage();
        message.setText("После проверки вам выдадут полный доступ");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private InlineKeyboardMarkup getKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(INFO_LABEL);
        inlineKeyboardButton.setCallbackData(COMMANDS.INFO.getCommand());

        InlineKeyboardButton inlineKeyboardButtonAccess = new InlineKeyboardButton();
        inlineKeyboardButtonAccess.setText(ACCESS_LABEL);
        inlineKeyboardButtonAccess.setCallbackData(COMMANDS.ACCESS.getCommand());

        InlineKeyboardButton inlineKeyboardButtonSuccess = new InlineKeyboardButton();
        inlineKeyboardButtonSuccess.setText(SUCCESS_LABEL);
        inlineKeyboardButtonSuccess.setCallbackData(COMMANDS.SUCCESS.getCommand());

        List<List<InlineKeyboardButton>> keyboardButtons = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton);
        keyboardButtonsRow1.add(inlineKeyboardButtonAccess);

        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(inlineKeyboardButtonSuccess);

        keyboardButtons.add(keyboardButtonsRow1);
        keyboardButtons.add(keyboardButtonsRow2);

        inlineKeyboardMarkup.setKeyboard(keyboardButtons);

        return inlineKeyboardMarkup;
    }
}
