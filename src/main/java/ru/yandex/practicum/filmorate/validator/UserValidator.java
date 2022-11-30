package ru.yandex.practicum.filmorate.validator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {

    @SneakyThrows
    public static void isValidUser(User user) {
        if(user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank()) {
            log.info("Попытка создать пользователя без указания логина");
            throw new InvalidNameException("Отсутствует логин пользователя");
        }

        if(user.getEmail() == null || !user.getEmail().contains("@")) {
            log.info("Попытка создать пользователя с некорректным email");
            throw new InvalidEmailException(String.format("Некорректный email: %s", user.getEmail()));
        }

        if(user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Попытка добавить пользователя из будущего");
            throw new InvalidBirthdateException(String.format("Некорректная дата рождения пользователя: %s", user.getBirthday()));
        }
    }
}
