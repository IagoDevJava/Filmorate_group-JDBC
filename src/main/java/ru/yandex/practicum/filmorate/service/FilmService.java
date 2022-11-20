package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;


@Service
@Slf4j
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private final LocalDate oldDate = LocalDate.of(1895, 12, 28);

    public Film create(Film film) {

        if(film.getId() != null && findFilmById(film.getId()) != null) {
            log.info("Попытка добавить фильм с уже существующим id");
            throw new FilmAlreadyExistException(String.format("id %d уже существует", film.getId()));
        }

        if(filmStorage.findAll().contains(film)) {
            log.info("Попытка добавить уже существующий фильм");
            throw new FilmAlreadyExistException("Фильм уже существует");
        }

        if(film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            log.info("Попытка добавить фильм без названия");
            throw new InvalidNameException("Отсутствует название фильма");
        }

        if(film.getDescription().length() > 200) {
            log.info("Попытка добавить фильм с описанием более 200 символов");
            throw new InvalidDescriptionException("Описание фильма не может быть длиннее 200 символов");
        }

        if(film.getReleaseDate().isBefore(oldDate)) {
            log.info("Попытка добавить фильм с датой релиза ранее 1895-12-28");
            throw new InvalidReleaseDateException("Дата релиза фильма ранее 1895-12-28");
        }

        if(film.getDuration() <= 0) {
            log.info("Попытка добавить фильм продолжительностью <= 0");
            throw new InvalidDurationException(String.format(
                    "Некорректная продолжительность фильма %d",
                    film.getDuration()));
        }

        if(film.getMpa() == null) {
            log.info("Попытка добавить фильм без mpa");
            throw new InvalidNameException("У фильма отсутствует mpa");
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if(film.getId() == null || findFilmById(film.getId()) == null) {
            log.info("Попытка обновить фильм с несуществующим или пустым id: {}", film.getId());
            throw new InvalidIdException(String.format("Пустой или несуществующий id: %d", film.getId()));
        }
        if(film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            log.info("Попытка обновить фильм без указания названия, id фильма: {}", film.getId());
            throw new InvalidNameException("Отсутствует название фильма");
        }
        if(film.getDescription().length() > 200) {
            log.info("Попытка добавить фильм с описанием более 200 символов");
            throw new InvalidDescriptionException("Описание фильма не может быть длиннее 200 символов");
        }
        if(film.getReleaseDate().isBefore(oldDate)) {
            log.info("Попытка обновить фильм с датой релиза ранее 1895-12-28, id фильма: {}", film.getId());
            throw new InvalidReleaseDateException("Дата релиза фильма ранее 1895-12-28");
        }
        if(film.getDuration() <= 0) {
            log.info("Попытка обновить фильм с продолжительностью <= 0: {}", film.getDuration());
            throw new InvalidDurationException(String.format(
                    "Некорректная продолжительность фильма %d",
                    film.getDuration()));
        }

        return filmStorage.update(film);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    // поиск фильма по id
    public Film findFilmById(Long id) {
        if (filmStorage.findFilmById(id) != null) {
            return filmStorage.findFilmById(id);
        } else {
            throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
        }
    }

    // поставить лайк фильму
    public String addLike(Long id, Long userId) {
        checkId(id, userId);
        if(userStorage.findUserById(userId) != null) {
            if (findFilmById(id) != null) {
                filmStorage.addLike(id, userId);
                log.info("Фильму с id {} поставлен лайк пользователем {}", id, userId);
                return String.format("Фильму с id %d поставлен лайк пользователем с id %d", id, userId);
            } else {
                log.info("Попытка поставить лайк фильму с несуществующим id {}", id);
                throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
            }
        } else {
            log.info("Попытка поставить лайк от пользователя с несуществующим id {}", userId);
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
    }

    // удалить лайк
    public String deleteLike(Long id, Long userId) {
        checkId(id, userId);
        if(userStorage.findUserById(userId) != null) {
            if (findFilmById(id) != null) {
                if (filmStorage.deleteLike(id, userId)) {
                    log.info("У фильма с id {} удален лайк пользователем {}", id, userId);
                    return String.format("У фильма с id %d удален лайк пользователем с id %d", id, userId);
                } else {
                    log.info("У фильма с id {} нет лайка от пользователя с id {}", id, userId);
                    return String.format("У фильма с id %d нет лайка от пользователя с id %d", id, userId);
                }

            } else {
                log.info("Попытка удалить лайк у фильма с несуществующим id {}", id);
                throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
            }
        } else {
            log.info("Попытка удалить лайк пользователя с несуществующим id {}", userId);
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    // получение списка общих фильмов
    public List<Film> commonFilmsList(Long userId, Long friendId) {
        if(userStorage.findUserById(userId) == null || userStorage.findUserById(friendId) == null) {
            log.info("Попытка получения списка общих фильмов пользователями с несуществующими id");
            throw new UserNotFoundException("Один из пользователей не зарегестрирован");
        } else {
            log.info("Получен список общих фильмов пользователей {} и {}", userId, friendId);
            return filmStorage.commonFilmsList(userId, friendId);
        }
    }

    private void checkId(Long id, Long userId) {
        if (id == null || id < 1) {
            log.info("Фильм с пустым или отрицательным id {}");
            throw new InvalidIdException("Фильм с пустым или отрицательным id");
        }

        if (userId == null || userId < 1) {
            log.info("Пользователь с пустым или отрицательным id {}");
            throw new InvalidIdException("Пользователь с пустым или отрицательным id");
        }
    }

    // список из первых {count} фильмов по количеству лайков
    public List<Film> findPopularFilms(Integer count) {
        if (count <= 0) {
            throw new IncorrectCountException("count");
        }

        if (filmStorage.findPopularFilms(count) != null) {
            log.info("Список популярных фильмов сформирован");
            return filmStorage.findPopularFilms(count);
        } else {
            log.info("Популярных фильмов нет :( ");
            return null;
        }
    }
}
