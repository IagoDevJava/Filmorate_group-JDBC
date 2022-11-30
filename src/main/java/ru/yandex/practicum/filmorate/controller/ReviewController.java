package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST/reviews — добавить новый отзыв
    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("Получен запрос POST/reviews - добавление нового отзыва");
        return reviewService.create(review);
    }

    // PUT/reviews - обновить отзыв
    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("Получен запрос PUT/reviews - обновление отзыва с id {}", review.getReviewId());
        return reviewService.update(review);
    }

    // DELETE/reviews/{id} — удалить отзыв
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Long id) {
        log.info("Получен запрос DELETE /{id} — удалить отзыв");
        return reviewService.delete(id);
    }

    // GET /reviews?filmId={filmId}&count={count} - получить отзывы
    @GetMapping
    public List<Review> findAll(@RequestParam(value = "filmId", required = false) Long filmId,
                                @RequestParam(value = "count", defaultValue = "10", required = false) Integer count) {
        log.info("Получен запрос GET/reviews - получение списка отзывов");
        return reviewService.findAll(filmId, count);
    }

    // GET/reviews/{id} — получить отзыв по id
    @GetMapping("/{id}")
    public Review findReviewById(@PathVariable(value = "id", required = false) Long id) {
        log.info("Получен запрос GET/reviews/{id} - получение отзыва по id");
        return reviewService.findReviewById(id);
    }

    // PUT /reviews/{id}/like/{userId}  — пользователь ставит лайк отзыву
    @PutMapping("/{id}/like/{userId}")
    public String addLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос PUT /reviews/{id}/like/{userId} — поставить лайк отзыву");
        return reviewService.addLike(id, userId, 1);
    }

    // PUT /reviews/{id}/dislike/{userId}  — пользователь ставит дизлайк отзыву
    @PutMapping("/{id}/dislike/{userId}")
    public String addDislike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос PUT /reviews/{id}/dislike/{userId} — поставить дизлайк отзыву");
        /*return reviewService.addDislike(id, userId);*/
        return reviewService.addLike(id, userId, -1);
    }

    // DELETE /reviews/{id}/like/{userId}  — пользователь удаляет лайк/дизлайк отзыву
    @DeleteMapping("/{id}/like/{userId}")
    public String deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос DELETE /reviews/{id}/like/{userId} — удалить лайк/дизлайк");
        return reviewService.deleteLike(id, userId, 1);
    }

    // DELETE /reviews/{id}/dislike/{userId}  — пользователь удаляет дизлайк отзыву
    @DeleteMapping("/{id}/dislike/{userId}")
    public String deleteDislike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос DELETE /reviews/{id}/dislike/{userId} — удалить дизлайк");
        return reviewService.deleteLike(id, userId, -1);
    }

}
