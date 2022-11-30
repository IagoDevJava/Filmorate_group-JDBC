package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.InvalidIdException;

@Slf4j
public class IdValidator {
    public static void isValidId(Long id) {
        if (id == null || id < 1) {
            log.info("Невалидный id");
            throw new InvalidIdException("Невалидный id");
        }
    }

    public static void isValidId(Long id, Long id2) {
        if (id == null || id < 1 || id2 == null || id2 < 1) {
            log.info("Невалидный id");
            throw new InvalidIdException("Невалидный id");
        }
    }
}
