package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Primary
@Slf4j
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;


    public ReviewDbStorage(
            JdbcTemplate jdbcTemplate, UserStorage userStorage, FilmStorage filmStorage, FeedStorage feedStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;

    }

    // создать отзыв
    @Override
    public Review create(Review review) {
        Long idReview = 1L;
        SqlRowSet fr = jdbcTemplate.queryForRowSet("SELECT id FROM REVIEWS ORDER BY id DESC LIMIT 1");
        if (fr.next()) {
            idReview = fr.getLong("id");
            log.info("Последний установленный id: {}", idReview);
            idReview++;
        }
        review.setReviewId(idReview);
        log.info("Установлен id отзыва: {}", idReview);

        String sql = "INSERT INTO REVIEWS (id, content, isPositive, user_id, film_id, creationDate) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, review.getReviewId(), review.getContent(), review.getIsPositive(), review.getUserId(),
                review.getFilmId(), Timestamp.valueOf(LocalDateTime.now()));

        feedStorage.createFeedEntity(review.getUserId(), review.getReviewId(), "REVIEW", "ADD");

        log.info("Добавлен новый отзыв: {}", review);

        return findReviewById(review.getReviewId());
    }

    // обновить отзыв
    @Override
    public Review update(Review review) {
        String sql = "UPDATE REVIEWS SET content = ?, isPositive = ?, creationDate = ? WHERE id = ?";

        jdbcTemplate.update(sql
                , review.getContent()
                , review.getIsPositive()
                , Timestamp.valueOf(LocalDateTime.now())
                , review.getReviewId());

        Review reviewUpdate = findReviewById(review.getReviewId());

        feedStorage.createFeedEntity(reviewUpdate.getUserId(), reviewUpdate.getReviewId(),
                "REVIEW", "UPDATE");

        log.info("Отзыв обновлен: {}", review);

        return reviewUpdate;
    }

    // удалить отзыв
    @Override
    public void delete(Long id) {
        Review review = findReviewById(id);
        feedStorage.createFeedEntity(review.getUserId(), review.getReviewId(), "REVIEW", "REMOVE");

        String sqlL = "delete from REVIEW_LIKES where review_id = ?";
        String sqlR = "delete from REVIEWS where id = ?";
        jdbcTemplate.update(sqlL, id);
        jdbcTemplate.update(sqlR, id);
        log.info("Отзыв с id {} удален", id);
    }

    // получить отзыв по id отзыва
    @Override
    public Review findReviewById(Long id) {
        log.info("Получение отзыва с id %d", id);
        String sql = "select t.*, SUM(l.useful) as useful " +
                "from REVIEWS as t " +
                "LEFT OUTER JOIN REVIEW_LIKES as l " +
                "ON t.id = l.review_id " +
                "where t.id = ?" +
                "GROUP BY t.id ";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeReview(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Отзыв с id %d не найден", id));
        }
    }

    // приват - билдер отзыва
    private Review makeReview(ResultSet rs) throws SQLException {
        Review review = Review.builder()
                .reviewId(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("isPositive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getLong("useful"))
                .creationDate(rs.getTimestamp("creationdate").toLocalDateTime())
                .build();

        if (review == null) {
            return null;
        }
        return review;
    }

    // получить отзывы
    @Override
    public List<Review> findAll(Integer count) {
        log.info("Получение списка отзывов в количестве {}", count);
        String sql = "select t.*, " +
                "CASE WHEN l.useful IS NULL THEN 0 " +
                "ELSE SUM(l.useful) END as useful " +
                "from REVIEWS as t " +
                "LEFT OUTER JOIN REVIEW_LIKES as l " +
                "ON t.id = l.review_id " +
                "group by t.film_id, t.id " +
                "order by useful DESC LIMIT " + count;
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs));
    }

    // получить список всех отзывов для проверки на дубли при создании отзыва
    @Override
    public List<Review> findAllForCheck() {
        log.info("Получение списка отзывов");
        String sql = "select t.*, SUM(l.useful) as useful " +
                "from REVIEWS as t " +
                "LEFT OUTER JOIN REVIEW_LIKES as l " +
                "ON t.id = l.review_id " +
                "group by t.film_id, t.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs));
    }

    // получить список отзывов по id фильма
    @Override
    public List<Review> findAllByFilmId(Long filmId, Integer count) {
        log.info("Получение списка отзывов для фильма с id {} в количестве {}", filmId, count);
        String sql = "select t.*, SUM(l.useful) as useful " +
                "from REVIEWS as t " +
                "LEFT OUTER JOIN REVIEW_LIKES as l " +
                "ON t.id = l.review_id " +
                "where t.film_id = ? " +
                "group by t.film_id, t.id " +
                "order by SUM(l.useful) DESC LIMIT " + count;
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), filmId);
    }

    // поставить лайк/дизлайк отзыву
    @Override
    public String addLike(Long id, Long userId, int useful) {
        String sql = "INSERT INTO REVIEW_LIKES (review_id, user_id, useful, creationDate) " +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, userId, useful, Timestamp.valueOf(LocalDateTime.now()));
        return String.format("Фильму с id %d  поставлен лайк пользователем %d", id, userId);
    }

    // удалить лайк/дизлайк
    @Override
    public void deleteLike(Long id, Long userId) {
        String sql = "delete from REVIEW_LIKES where review_id = ? and user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    // получить useful отзыва по id (для проверки наличия и статуса лайка)
    public Long getUsefulFromUser(Long id, Long userId) {
        log.info("Получение лайков отзыва {} от пользователя {}", id, userId);
        String sql = "select useful from REVIEW_LIKES where review_id = ? and user_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUseful(rs), id, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // приват - билдер useful (нужен для getUsefulFromUser)
    private Long makeUseful(ResultSet rs) throws SQLException {
        Long l = rs.getLong("useful");
        return l;
    }
}
