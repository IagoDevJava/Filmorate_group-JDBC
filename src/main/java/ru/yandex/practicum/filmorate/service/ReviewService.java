package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.IdValidator;
import ru.yandex.practicum.filmorate.validator.ReviewValidator;

import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private ReviewStorage reviewStorage;
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, FilmStorage filmStorage, UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    // добавить отзыв
    public Review create(Review review) {
        ReviewValidator.isValidReview(review);
        filmStorage.findFilmById(review.getFilmId());
        userStorage.findUserById(review.getUserId());

        if(reviewStorage.findAllForCheck().contains(review)) {
            log.info("Попытка добавить уже существующий отзыв");
            throw new FilmAlreadyExistException("Отзыв уже существует");
        }

        return reviewStorage.create(review);
    }

    // обновить отзыв
    public Review update(Review review) {
        ReviewValidator.isValidReview(review);
        IdValidator.isValidId(review.getReviewId());
        filmStorage.findFilmById(review.getFilmId());
        userStorage.findUserById(review.getUserId());

        return reviewStorage.update(review);
    }

    // удалить отзыв
    public String delete(Long id) {
        IdValidator.isValidId(id);
        reviewStorage.findReviewById(id);
        reviewStorage.delete(id);
        return String.format("Отзыв с id %d удален", id);
    }

    // получить отзывы
    public List<Review> findAll(Long filmId, Integer count) {
        if (filmId != null && filmId > 0) {
            filmStorage.findFilmById(filmId);
            return reviewStorage.findAllByFilmId(filmId, count);
        } else {
            return reviewStorage.findAll(count);
        }

    }

    // получить отзыв по id
    public Review findReviewById(Long id) {
        IdValidator.isValidId(id);
        return reviewStorage.findReviewById(id);
    }

    // поставить лайк отзыву
    public String addLike(Long id, Long userId, int useful) {
        IdValidator.isValidId(id, userId);
        reviewStorage.findReviewById(id);
        userStorage.findUserById(userId);

        if(reviewStorage.getUsefulFromUser(id, userId) == null) {
            reviewStorage.addLike(id, userId, useful);
            log.info("Отзыву с id {} поставлен лайк/дизлайк пользователем {}", id, userId);
            return String.format("Отзыву с id %d поставлен лайк/дизлайк пользователем с id %d", id, userId);
        }

        if(reviewStorage.getUsefulFromUser(id, userId).equals(useful)) {
            log.info("Лайк/дизлайк уже существует");
            throw new AlreadyExistException("Лайк/дизлайк уже существует");
        }

        reviewStorage.deleteLike(id, userId);
        log.info("Удален лайк/дизлайк озыву с id {} от пользователя {}", id, userId);

        reviewStorage.addLike(id, userId, useful);
        log.info("Отзыву с id {} поставлен лайк/дизлайк пользователем {}", id, userId);
        return String.format("Отзыву с id %d поставлен лайк/дизлайк пользователем с id %d", id, userId);

    }

    // удалить лайк/дизлайк отзыву
    public String deleteLike(Long id, Long userId, int useful) {
        IdValidator.isValidId(id, userId);
        reviewStorage.findReviewById(id);
        userStorage.findUserById(userId);

        if(reviewStorage.getUsefulFromUser(id, userId) == null) {
            log.info("Лайк/дизлайк не существует");
            throw new NotFoundException("Лайк/дизлайк не существует");
        }

        if(reviewStorage.getUsefulFromUser(id, userId).equals(1) && useful == -1) {
            log.info("У отзыва с id {} нет дизлайка от пользователя {}", id, userId);
            return String.format("У отзыва с id %d нет дизлайка от пользователя с id %d", id, userId);
        }

        reviewStorage.deleteLike(id, userId);
        log.info("Удален лайк/дизлайк отзыву с id {} от пользователя {}", id, userId);
        return String.format("Удален лайк/дизлайк отзыву с id %d от пользователя с id %d", id, userId);
    }

}
