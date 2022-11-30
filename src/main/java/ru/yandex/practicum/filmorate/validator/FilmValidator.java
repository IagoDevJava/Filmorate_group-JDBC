package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;

@Slf4j
public class FilmValidator {

    public static void isValidFilm(Film film) {
        if(film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            log.info("Попытка добавить фильм без названия");
            throw new InvalidNameException("Отсутствует название фильма");
        }

        if(film.getDescription().length() > 200) {
            log.info("Попытка добавить фильм с описанием более 200 символов");
            throw new InvalidDescriptionException("Описание фильма не может быть длиннее 200 символов");
        }

        if(film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
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
    }

}
