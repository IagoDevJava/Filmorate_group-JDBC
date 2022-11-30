package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectCountException;
import ru.yandex.practicum.filmorate.exception.InvalidIdException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;
import ru.yandex.practicum.filmorate.validator.IdValidator;

import java.util.List;


@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    /**
     * валидация фильмов и создание фильма
     */
    public Film create(Film film) {
        FilmValidator.isValidFilm(film);

        if (filmStorage.findAll().contains(film)) {
            log.info("Попытка добавить уже существующий фильм");
            throw new FilmAlreadyExistException("Фильм уже существует");
        }

        return filmStorage.create(film);
    }

    /**
     * валидация фильмов и обновление фильма
     */
    public Film update(Film film) {
        FilmValidator.isValidFilm(film);
        IdValidator.isValidId(film.getId());
        findFilmById(film.getId());

        return filmStorage.update(film);
    }

    /**
     * получение всех фильмов
     */
    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    /**
     * получение фильма по id
     */
    public Film findFilmById(Long id) {
        return filmStorage.findFilmById(id);
    }

    /**
     * Очистить список фильмов
     */
    public void clearFilms() {
        filmStorage.clearFilms();
    }

    /**
     * Удаление фильма по id
     */
    public void deleteFilmById(String idStr) {
        filmStorage.deleteFilmById(idStr);
    }

    /**
     * Пользователь ставит фильму лайк
     */
    public String addLike(Long id, Long userId) {
        IdValidator.isValidId(id, userId);
        findFilmById(id);
        userStorage.findUserById(userId);

        filmStorage.addLike(id, userId);
        log.info("Фильму с id {} поставлен лайк пользователем {}", id, userId);
        return String.format("Фильму с id %d поставлен лайк пользователем с id %d", id, userId);
    }

    /**
     * пользователь удаляет лайк.
     */
    public String deleteLike(Long id, Long userId) {
        IdValidator.isValidId(id, userId);
        findFilmById(id);
        userStorage.findUserById(userId);

        if (filmStorage.deleteLike(id, userId)) {
            log.info("У фильма с id {} удален лайк пользователем {}", id, userId);
            return String.format("У фильма с id %d удален лайк пользователем с id %d", id, userId);
        } else {
            log.info("У фильма с id {} нет лайка от пользователя с id {}", id, userId);
            return String.format("У фильма с id %d нет лайка от пользователя с id %d", id, userId);
        }
    }

    // получение списка общих фильмов
    public List<Film> commonFilmsList(Long userId, Long friendId) {
        if (userStorage.findUserById(userId) == null || userStorage.findUserById(friendId) == null) {
            log.info("Попытка получения списка общих фильмов пользователями с несуществующими id");
            throw new UserNotFoundException("Один из пользователей не зарегестрирован");
        } else {
            log.info("Получен список общих фильмов пользователей {} и {}", userId, friendId);
            return filmStorage.commonFilmsList(userId, friendId);
        }
    }

    /**
     * возвращает список первых фильмов по количеству лайков.
     * Если значение параметра count не задано, верните первые 10.
     */
    public List<Film> findPopularFilms(Integer count, Long genreId, Integer year) {
        if (count <= 0) {
            throw new IncorrectCountException("count");
        }
        if (genreId == 0 && year == 0) {
            log.info("Список популярных фильмов сформирован");
            return filmStorage.findPopularFilms(count);
        } else if (genreId == 0) {
            log.info("Список популярных фильмов сформирован");
            return filmStorage.findPopularFilms(count, year);
        } else if (year == 0) {
            log.info("Список популярных фильмов сформирован");
            return filmStorage.findPopularFilms(count, genreId);
        } else {
            log.info("Список популярных фильмов сформирован");
            return filmStorage.findPopularFilms(count, genreId, year);
        }
    }

    public List<Film> findDirectorFilms(Long directorId, String sort) {
        if (filmStorage.findDirectorFilms(directorId, sort) != null) {
            log.info("Список фильмов режиссера сформирован");
            return filmStorage.findDirectorFilms(directorId, sort);
        } else {
            log.info("У режиссера нет фильмов :(");
            return null;
        }
    }

    // поиск фильма по режиссеру или названию
    public List<Film> searchFilm(String query, List<String> by) {
        if (by.contains("director") && !by.contains("title")) {
            return searchFilmByDirector(query,by);
        }
        if (!by.contains("director") && by.contains("title")) {
            return searchFilmByTitle(query, by);
        }
        return searchFilmByTitleAndDirector(query,by);
    }

    private List<Film> searchFilmByDirector(String query, List<String> values) {
        log.info("Поиск фильмов по режиссеру");
        return filmStorage.searchFilmByDirector(query, values);
    }

    private List<Film> searchFilmByTitle(String query, List<String> values) {
        log.info("Поиск фильмов по названию");
        return filmStorage.searchFilmByTitle(query, values);
    }

    private List<Film> searchFilmByTitleAndDirector(String query, List<String> values) {
        log.info("Поиск фильмов по названию и режиссеру");
        return filmStorage.searchFilmByTitleAndDirector(query,values);
    }

    private void checkId(Long id, Long userId) {
        if (id == null || id < 1) {
            log.info("Фильм с пустым или отрицательным id {}", id);
            throw new InvalidIdException("Фильм с пустым или отрицательным id");
        }
        if (userId == null || userId < 1) {
            log.info("Пользователь с пустым или отрицательным id {}", userId);
            throw new InvalidIdException("Пользователь с пустым или отрицательным id");
        }
    }
}
