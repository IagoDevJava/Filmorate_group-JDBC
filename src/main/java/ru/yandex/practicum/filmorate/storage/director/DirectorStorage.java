package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {
    List<Director> findAll();

    Director findDirectorById(Long id);

    Director create(Director director);

    Director update(Director director);

    void deleteDirectorById(Long id);

    void clearDirectors(Film film);

    void addDirectorsToFilm(Film film);

    List<Director> getDirectors(Long id);



}
