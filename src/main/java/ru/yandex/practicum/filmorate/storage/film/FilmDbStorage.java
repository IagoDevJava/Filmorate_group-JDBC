package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Primary
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FeedStorage feedStorage;
    private final DirectorStorage directorStorage;

    public FilmDbStorage(
            JdbcTemplate jdbcTemplate, MpaStorage mpaStorage, GenreStorage genreStorage, FeedStorage feedStorage,
            DirectorStorage directorStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.feedStorage = feedStorage;
        this.directorStorage = directorStorage;
    }

    /**
     * создание фильма
     */
    @Override
    public Film create(Film film) {
        Long idn = 1L;
        SqlRowSet fr = jdbcTemplate.queryForRowSet("SELECT id FROM FILMS ORDER BY id DESC LIMIT 1");
        if (fr.next()) {
            idn = fr.getLong("id");
            log.info("Последний установленный id: {}", idn);
            idn++;
        }
        film.setId(idn);
        log.info("Установлен id фильма: {}", idn);

        String sql = "INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getId(), film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration());
        log.info("Добавлен новый фильм: {}", film);

        if (film.getMpa() != null) {
            mpaStorage.addMpa(film);
        }

        if (film.getGenres() != null) {
            log.info("Список жанров не null {}", film.getGenres());
            genreStorage.addGenre(film);
        }

        if (film.getDirectors() != null) {
            directorStorage.addDirectorsToFilm(film);
        } else {
            directorStorage.clearDirectors(film);
        }

        return film;
    }

    /**
     * обновление фильма
     */
    @Override
    public Film update(Film film) {
        String sql = "UPDATE FILMS SET name = ?, description = ?, releasedate = ?, duration = ? WHERE id = ?";

        jdbcTemplate.update(sql
                , film.getName()
                , film.getDescription()
                , film.getReleaseDate()
                , film.getDuration()
                , film.getId());
        log.info("Фильм обновлен: {}", film);

        if (film.getMpa() != null) {
            log.info("mpa не пустой {}", film.getMpa());
            mpaStorage.updateMpa(film);
        }

        genreStorage.updateGenre(film);

        if (film.getDirectors() != null) {
            directorStorage.addDirectorsToFilm(film);
        } else {
            directorStorage.clearDirectors(film);
        }

        return findFilmById(film.getId());
    }

    /**
     * получение всех фильмов
     */
    @Override
    public List<Film> findAll() {
        log.info("Получение списка фильмов");
        String sql = "select * from films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    /**
     * получение фильма по id
     */
    @Override
    public Film findFilmById(Long id) {
        String sql = "select * from films where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
        } catch (Exception e) {
            throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
        }
    }

    /**
     * Удаление фильмов из списка
     */
    @Override
    public void clearFilms() {
        try {
            String sqlDelLikes = "DELETE FROM LIKES";
            String sqlDelGenres = "DELETE FROM FILM_GENRE";
            String sqlDelMpa = "DELETE FROM FILM_MPA";
            String sql = "DELETE from FILMS";
            jdbcTemplate.update(sqlDelLikes);
            jdbcTemplate.update(sqlDelGenres);
            jdbcTemplate.update(sqlDelMpa);
            jdbcTemplate.update(sql);
            log.info("Удалены все фильмы таблицы FILM");
        } catch (Exception e) {
            throw new FilmNotFoundException("Фильм не найден");
        }
    }

    /**
     * Удаление фильма по id
     */
    @Override
    public void deleteFilmById(String id) {
        if (findFilmById(Long.valueOf(id)) != null) {
            String sqlDelLikes = "DELETE FROM LIKES WHERE FILM_ID=?";
            jdbcTemplate.update(sqlDelLikes, id);
            String sqlDelGenre = "DELETE FROM FILM_GENRE WHERE FILM_ID=?";
            jdbcTemplate.update(sqlDelGenre, id);
            String sqlDelMpa = "DELETE FROM FILM_MPA WHERE FILM_ID=?";
            jdbcTemplate.update(sqlDelMpa, id);
            String sql = "DELETE from FILMS where ID=?";
            jdbcTemplate.update(sql, id);
            log.info("Удален фильм: {}", id);
        } else {
            throw new FilmNotFoundException("Такого фильма нет в базе.");
        }
    }

    /**
     * Пользователь ставит фильму лайк
     */
    public String addLike(Long id, Long userId) {
        String sql = "INSERT INTO LIKES (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, userId);

        feedStorage.createFeedEntity(userId, id, "LIKE", "ADD");

        return String.format("Фильму с id %d  поставлен лайк пользователем %d", id, userId);
    }

    /**
     * пользователь удаляет лайк.
     */
    public boolean deleteLike(Long id, Long userId) {
        log.info("Проверка наличия лайка от пользователя c id {} у фильма с id {}", userId, id);
        if (getLikes(id).contains(userId)) {
            String sql = "delete from LIKES where film_id = ? and user_id = ?";

            feedStorage.createFeedEntity(userId, id, "LIKE", "REMOVE");

            log.info("У фильма с id {} удален лайк пользователя с id {}", id, userId);
            return jdbcTemplate.update(sql, id, userId) > 0;
        } else {
            log.info("У пользователя с id {} нет друга с id {}", id, userId);
            return false;
        }
    }

    //получение списка общих фильмов
    @Override
    public List<Film> commonFilmsList(Long userId, Long friendId) {
        log.info("Получение списка общих фильмов");
        String sql = "SELECT f.* FROM LIKES as l " +
                "INNER JOIN FILMS AS f ON f.ID = l.FILM_ID " +
                "WHERE l.USER_ID = ? " +
                "INTERSECT " +
                "SELECT f.* FROM LIKES as l " +
                "INNER JOIN FILMS AS f ON f.ID = l.FILM_ID " +
                "WHERE l.USER_ID = ? ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), userId, friendId);
    }

    /**
     * возвращает список первых фильмов по количеству лайков.
     * Если значение параметра count не задано, верните первые 10.
     */
    @Override
    public List<Film> findPopularFilms(Integer count) {
        List<Film> list = new ArrayList<>();

        for (Long l : getIdFilms(count)) {
            list.add(findFilmById(l));
        }
        return list;
    }


    @Override
    public List<Film> findDirectorFilms(Long directorId, String sort) {
        if (directorStorage.findDirectorById(directorId) != null) {
            if (sort.equals("year")) {
                String sql = "select f.*\n" +
                        "from FILM_DIRECTOR as fd\n" +
                        "join FILMS F on F.ID = fd.FILM_ID\n" +
                        "where DIRECTOR_ID = ?\n" +
                        "group by f.ID\n" +
                        "order by extract(YEAR from f.RELEASEDATE)";
                return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), directorId);
            }
            if (sort.equals("likes")) {
                String sql = "select f.*\n" +
                        "from LIKES as l\n" +
                        "RIGHT JOIN FILMS F on F.ID = l.FILM_ID\n" +
                        "JOIN FILM_DIRECTOR FD on F.ID = FD.FILM_ID\n" +
                        "where DIRECTOR_ID = ?\n" +
                        "group by f.ID\n" +
                        "order by count(l.USER_ID)";
                return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), directorId);
            }
        } else {
            throw new DirectorNotFoundException("Режиссера с таким id не существует");
        }

        return Collections.emptyList();
    }

    // поиск популярных фильмов по году
    @Override
    public List<Film> findPopularFilms(Integer count, Integer year) {
        String sql = "SELECT f.* " +
                "FROM LIKES AS l " +
                "RIGHT OUTER JOIN FILMS AS f ON l.FILM_ID = f.ID " +
                "WHERE EXTRACT(YEAR FROM f.RELEASEDATE) = ? " +
                "GROUP BY f.ID " +
                "ORDER BY COUNT(l.USER_ID) DESC LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), year, count);
    }

    // поиск популярных фильмов по жанру
    @Override
    public List<Film> findPopularFilms(Integer count, Long genreId) {
        String sql = "SELECT f.* " +
                "FROM LIKES AS l " +
                "RIGHT OUTER JOIN FILMS AS f ON l.FILM_ID = f.ID " +
                "LEFT OUTER JOIN FILM_GENRE fg on f.ID = FG.FILM_ID " +
                "WHERE fg.GENRE_ID = ? " +
                "GROUP BY f.ID " +
                "ORDER BY COUNT(l.USER_ID) DESC LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, count);
    }

    // поиск популярных фильмов по году и жанру
    @Override
    public List<Film> findPopularFilms(Integer count, Long genreId, Integer year) {
        String sql = "SELECT f.* " +
                "FROM LIKES AS l " +
                "RIGHT OUTER JOIN FILMS AS f ON l.FILM_ID = f.ID " +
                "LEFT OUTER JOIN FILM_GENRE fg on f.ID = FG.FILM_ID " +
                "WHERE fg.GENRE_ID = ? AND EXTRACT(YEAR FROM f.RELEASEDATE) = ?" +
                "GROUP BY f.ID " +
                "ORDER BY COUNT(l.USER_ID) DESC LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, year, count);

    }




    @Override
    public List<Film> searchFilmByDirector(String query, List<String> values) {
        String sql = "select F.*\n" +
                "from LIKES\n" +
                "RIGHT JOIN FILMS F on F.ID = LIKES.FILM_ID\n" +
                "LEFT JOIN FILM_DIRECTOR FD on F.ID = FD.FILM_ID\n" +
                "left JOIN DIRECTORS D on D.ID = FD.DIRECTOR_ID\n" +
                "where LOCATE(LOWER(?), LOWER(D.NAME)) > 0\n" +
                "GROUP BY F.ID\n" +
                "ORDER BY COUNT(USER_ID) desc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query);
    }

    @Override
    public List<Film> searchFilmByTitle(String query, List<String> values) {
        String sql = "select F.*\n" +
                "from LIKES\n" +
                "RIGHT JOIN FILMS F on F.ID = LIKES.FILM_ID\n" +
                "where LOCATE(LOWER(?), LOWER(NAME)) > 0\n" +
                "GROUP BY ID\n" +
                "ORDER BY COUNT(USER_ID) desc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query);
    }

    @Override
    public List<Film> searchFilmByTitleAndDirector(String query, List<String> values) {
        String sql = "select F.*\n" +
                "from LIKES\n" +
                "RIGHT JOIN FILMS F on F.ID = LIKES.FILM_ID\n" +
                "left join FILM_DIRECTOR FD on F.ID = FD.FILM_ID\n" +
                "left join DIRECTORS D on D.ID = FD.DIRECTOR_ID\n" +
                "where LOCATE(LOWER(?), LOWER(F.NAME)) > 0 or\n" +
                "      LOCATE(LOWER(?), LOWER(D.NAME)) > 0\n" +
                "GROUP BY F.ID\n" +
                "ORDER BY COUNT(USER_ID) desc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query, query);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releasedate").toLocalDate())
                .duration(rs.getLong("duration"))
                .rate(getCountLikes(rs.getLong("id")))
                .mpa(mpaStorage.getMpa(rs.getLong("id")))
                .genres(genreStorage.getGenre(rs.getLong("id")))
                .directors(directorStorage.getDirectors(rs.getLong("id")))
                .build();

        if (film.getName() == null) {
            film = null;
        }
        return film;
    }

    private List<Long> getIdFilms(Integer count) {
        log.info("Получение списка id пользователей, поставивших лайки");
        String sql = "select f.id, COUNT(l.user_id) " +
                "from likes as l " +
                "RIGHT OUTER JOIN FILMS as f " +
                "ON l.film_id = f.id " +
                "group by f.id " +
                "order by COUNT(l.user_id) DESC LIMIT " + count;

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilmId(rs));
    }

    private Long makeFilmId(ResultSet rs) throws SQLException {
        return rs.getLong("id");
    }

    private Long getCountLikes(Long id) {
        log.info("Получение списка лайков фильма {}", id);
        String sql = "select COUNT(user_id) AS user_id from likes where film_id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUserId(rs), id);
    }

    private Long makeUserId(ResultSet rs) throws SQLException {

        return rs.getLong("user_id");
    }

    private List<Long> getLikes(Long id) {
        log.info("Получение списка лайков фильма {}", id);
        String sql = "select user_id from likes where film_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUserId(rs), id);
    }

    /**
     * Метод возвращает отсортированный список фильмов по ID,
     * то есть в каком порядке были ID, в том же порядке будет и список фильмов
     */
    @Override
    public List<Film> findFilmsByIdsOrdered(List<Long> ids) {
        StringBuilder valuesSb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            valuesSb.append("(").append(ids.get(i)).append(", ").append(i + 1).append("), ");
        }
        String values = valuesSb.substring(0, valuesSb.length() - 2);
        String sql = String.format("SELECT F.* " +
                                   "FROM FILMS F\n" +
                                   "JOIN (VALUES %s) AS V (ID, ORDERING) ON F.ID = V.ID\n" +
                                   "ORDER BY V.ORDERING;", values);
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }
}
