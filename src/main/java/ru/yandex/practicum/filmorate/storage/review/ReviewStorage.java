package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    public Review create(Review review);
    public Review update(Review review);
    public void delete(Long id);
    public Review findReviewById(Long id);
    public List<Review> findAllForCheck();
    public List<Review> findAll(Integer count);
    public List<Review> findAllByFilmId(Long id, Integer count);
    public String addLike(Long id, Long userId, int useful);
    public Long getUsefulFromUser(Long id, Long userId);
    public void deleteLike(Long id, Long userId);
}
