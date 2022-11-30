package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.InvalidNameException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Primary
@Slf4j
public class DirectorDbStorage implements DirectorStorage {

    JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> findAll() {
        try {
            String sql = "SELECT * FROM DIRECTORS";
            return jdbcTemplate.query(sql, this::makeDirector);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Director findDirectorById(Long id) {
        String sql = "SELECT * FROM DIRECTORS WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::makeDirector, id);
        } catch (Exception e) {
            throw new DirectorNotFoundException(String.format("Режиссер с id = %d не найден", id));
        }
    }

    @Override
    public Director create(Director director) {
        if (director.getName().isEmpty() || director.getName().startsWith(" ")) {
            throw new InvalidNameException("Имя пустое либо начинается с пробела");
        }
        Long id = 1L;
        SqlRowSet fr = jdbcTemplate.queryForRowSet("SELECT id from DIRECTORS ORDER BY id DESC LIMIT 1");
        if (fr.next()) {
            id = fr.getLong("id");
            log.info("Последний установленный id: {}", id);
            id++;
        }
        director.setId(id);
        log.info("Установлен id режиссеру: {}", id);

        String sql = "INSERT INTO DIRECTORS (id,name) VALUES (?,?)";
        jdbcTemplate.update(sql, director.getId(), director.getName());

        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE DIRECTORS SET name = ?";
        jdbcTemplate.update(sql, director.getName());
        log.info("Директор обновлен");
        return findDirectorById(director.getId());
    }

    @Override
    public void deleteDirectorById(Long id) {
        if (findDirectorById(id) != null) {
            jdbcTemplate.update("DELETE FROM FILM_DIRECTOR WHERE director_id = ?", id);
            String sql = "DELETE FROM DIRECTORS WHERE id = ?";
            jdbcTemplate.update(sql, id);

        } else {
            throw new DirectorNotFoundException(String.format("Режиссера с id %d не существует", id));
        }
    }

    @Override
    public void addDirectorsToFilm(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                String sql = "INSERT INTO FILM_DIRECTOR (film_id, director_id) values (?,?)";
                jdbcTemplate.update(sql, film.getId(), director.getId());
                log.info("Фильму с id = {} добавлен режиссер с id = {}", film.getId(), director.getId());
            }
        }
    }

    @Override
    public List<Director> getDirectors(Long id) {
        log.info("Получение List<Director> фильма с id = {}", id);
        List<Director> list = new ArrayList<>();
        if (getDirectorsId(id) != null) {
            for (Long directorId : getDirectorsId(id)) {
                list.add(findDirectorById(directorId));
            }
        }
        return list;
    }

    @Override
    public void clearDirectors(Film film) {
        String sql = "DELETE FROM FILM_DIRECTOR where film_id = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    private List<Long> getDirectorsId(Long id) {
        String sql = "SELECT director_id from FILM_DIRECTOR where film_id = ?";
        try {
            return jdbcTemplate.query(sql, this::makeDirectorsId, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Long makeDirectorsId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("director_id");
    }

    private Director makeDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}
