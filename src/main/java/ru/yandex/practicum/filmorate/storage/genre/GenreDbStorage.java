package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Primary
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    // получение списка всех жанров
    @Override
    public List<Genre> findAll() {
        log.info("Получение списка жанров");
        String sql = "select * from genre";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    // поиск жанра по id
    @Override
    public Genre findGenreById(Long id) {
        String sql = "select * from GENRE where id = ?";

        try{
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeGenre(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Жанр с id %d не найден", id));
        }
    }

    // получение жанров по id фильма
    @Override
    public List<Genre> getGenre(Long id) {
        log.info("Получение List<Genre> фильма с id {}", id);
        List<Genre> list = new ArrayList<>();

        if (getGenreId(id) != null) {
            for (Long l : getGenreId(id)) {
                list.add(findGenreById(l));
            }
        }
        return list;
    }

    // получение id жанров по id фильма
    private List<Long> getGenreId(Long id) {
        log.info("Получение списка id жанров для фильма с id {}", id);
        String sql = "select genre_id from FILM_GENRE where film_id = ?";
        try{
            return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenreId(rs), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // добавление жанров
    @Override
    public void addGenre(Film film) {
        if (!film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                String sql = "INSERT INTO FILM_GENRE (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(sql, film.getId(), g.getId());
                log.info("Фильму с id {} присвоен жанр {}", film.getId(), g);
            }
        }
    }

    // обновление жанров
    @Override
    public void updateGenre(Film film) {
        String qsql = "delete from FILM_GENRE where film_id = ?";
        log.info("У фильма с id {} удалены существующие жанры", film.getId());
        jdbcTemplate.update(qsql, film.getId());
        log.info("Обновление списка жанров фильма с id {}", film.getId());

        if (film.getGenres() != null) {
            HashMap <Long, Genre> h = new HashMap<>();
            for (Genre g : film.getGenres()) {
                h.put(g.getId(), g);
            }
            for(Genre g : h.values()){
                String sql = "INSERT INTO FILM_GENRE (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(sql, film.getId(), g.getId());
                log.info("Фильму с id {} присвоен жанр {}", film.getId(), g);
            }
        }
    }

    private Long makeGenreId(ResultSet rs) throws SQLException {
        Long l = rs.getLong("genre_id");

        if (l == null) {
            return null;
        }
        return l;
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        Genre genre = Genre.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();

        if (genre == null) {
            return null;
        }
        return genre;
    }
}
