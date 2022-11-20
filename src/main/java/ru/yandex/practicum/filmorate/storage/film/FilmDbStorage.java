package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaStorage mpaStorage, GenreStorage genreStorage){
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

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

        String sql = "INSERT INTO FILMS (id, name, description, releasedate, duration) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getId(), film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration());
        log.info("Добавлен новый фильм: {}", film);

        if(film.getMpa() != null) {
            mpaStorage.addMpa(film);
        }

        if(film.getGenres() != null) {
            log.info("Список жанров не пустой {}", film.getGenres());
            genreStorage.addGenre(film);
        }

        return findFilmById(film.getId());
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
                .build();

        if (film == null) {
            return null;
        }
        return film;
    }

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

        if(film.getMpa() != null) {
            log.info("mpa не пустой {}", film.getMpa());
            mpaStorage.updateMpa(film);
        }

        genreStorage.updateGenre(film);

        return findFilmById(film.getId());
    }

    @Override
    public List<Film> findAll() {
        log.info("Получение списка фильмов");
        String sql = "select * from films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film findFilmById(Long id) {
        String sql = "select * from films where id = ?";

        try{
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
        }
    }

    public String addLike(Long id, Long userId) {
        String sql = "INSERT INTO LIKES (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, userId);

        return String.format("Фильму с id %d  поставлен лайк пользователем %d", id, userId);
    }

    public boolean deleteLike(Long id, Long userId) {
        log.info("Проверка наличия лайка от пользователя c id {} у фильма с id {}", userId, id);
        if (getLikes(id).contains(userId)) {
            String sql = "delete from LIKES where film_id = ? and user_id = ?";
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

    private List<Long> getLikes(Long id) {
        log.info("Получение списка лайков фильма {}", id);
        String sql = "select user_id from likes where film_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUserId(rs), id);
    }

    private Long getCountLikes(Long id) {
        log.info("Получение списка лайков фильма {}", id);
        String sql = "select COUNT(user_id) AS user_id from likes where film_id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUserId(rs), id);
    }

    private Long makeUserId(ResultSet rs) throws SQLException {
        Long l = rs.getLong("user_id");

        if (l == null) {
            return null;
        }
        return l;
    }

    public List<Film> findPopularFilms(Integer count) {
        List<Film> list = new ArrayList<>();

        for(Long l : getIdFilms(count)) {
            list.add(findFilmById(l));
        }
        return list;
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
        Long l = rs.getLong("id");

        if (l == null) {
            return null;
        }
        return l;
    }

}
