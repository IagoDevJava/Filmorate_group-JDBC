package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
public class Review {
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Long useful;
    private LocalDateTime creationDate;

    public Review(Long reviewId, String content, Boolean isPositive, Long userId, Long filmId, Long useful, LocalDateTime creationDate) {
        this.reviewId = reviewId;
        this.content = content;
        this.isPositive = isPositive;
        this.userId = userId;
        this.filmId = filmId;
        this.useful = useful;
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return content.equals(review.content) && isPositive.equals(review.isPositive) && userId.equals(review.userId) && filmId.equals(review.filmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, isPositive, userId, filmId);
    }
}
