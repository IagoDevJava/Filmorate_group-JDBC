package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    /**
     * добавление фильма в список
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST/films - добавление нового фильма");
        return filmService.create(film);
    }

    /**
     * обновление фильма в списке
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT/films - обновление фильма с id {}", film.getId());
        return filmService.update(film);
    }

    /**
     * получение всех фильмов
     */
    @GetMapping
    public List<Film> findAll() {
        log.info("Получен запрос GET/films - получение списка фильмов");
        return filmService.findAll();
    }

    /**
     * получение фильма по id
     */
    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable(value = "id", required = false) Long id) {
        log.info("Получен запрос GET/films/{id} - получение фильма по id");
        return filmService.findFilmById(id);
    }

    /**
     * Очистить список фильмов
     */
    @DeleteMapping
    public void clearFilms() {
        log.debug("Очищаем список фильмов.");
        filmService.clearFilms();
    }

    /**
     * Удаление фильма по id
     */
    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable String id) {
        log.debug("Удаляем фильм по id {}.", id);
        filmService.deleteFilmById(id);
    }

    /**
     * Пользователь ставит фильму лайк
     */
    @PutMapping("/{id}/like/{userId}")
    public String addLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос PUT /films/{id}/like/{userId} — поставить лайк фильму");
        return filmService.addLike(id, userId);
    }

    /**
     * пользователь удаляет лайк.
     */
    @DeleteMapping("/{id}/like/{userId}")
    public String deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        log.info("Получен запрос DELETE /films/{id}/like/{userId} — удалить лайк");
        return filmService.deleteLike(id, userId);
    }

    /**
     * возвращает список фильмов по количеству лайков.
     */
    @GetMapping("/popular")
    public List<Film> findPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
            log.info("Получен запрос GET /films/popular?count={count} — список фильмов по количеству лайков");
            return filmService.findPopularFilms(count);
    }

    // GET /films/popular?count={limit}&genreId={genreId}&year={year} - получение списка популярных фильмов по жанру или году
    @GetMapping("/popular")
    public List<Film> findPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count,
                                       @RequestParam(value = "genreId", required = false) Long genreId,
                                       @RequestParam(value = "year", required = false) Integer year) {
        if (genreId == null) {
            return filmService.findPopularFilms(count, year);
        } else if (year == null) {
            return filmService.findPopularFilms(count, genreId);
        } else {
            return filmService.findPopularFilms(count, genreId, year);
        }
    }

}
