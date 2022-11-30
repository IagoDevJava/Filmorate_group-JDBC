package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Slf4j
public class ReviewValidator {

    public static void isValidReview(Review review) {
        if(review.getContent() == null || review.getContent().isEmpty() || review.getContent().isBlank()) {
            log.info("Попытка добавить пустой отзыв");
            throw new AlreadyExistException("Отсутствует текст отзыва");
        }

        if(review.getUserId() == null) {
            log.info("Попытка добавить отзыв без id пользователя");
            throw new AlreadyExistException("У отзыва отсутствует id пользователя");
        }

        if(review.getFilmId() == null) {
            log.info("Попытка добавить отзыв без id фильма");
            throw new AlreadyExistException("У отзыва отсутствует id фильма");
        }

        if(review.getIsPositive() == null) {
            log.info("Попытка добавить отзыв без IsPositive");
            throw new AlreadyExistException("У отзыва отсутствует IsPositive");
        }
    }

}
