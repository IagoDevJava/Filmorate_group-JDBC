package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

public interface FilmStorage {

    public Film create(Film film);
    public Film update(Film film);
    public List<Film> findAll();
    public Film findFilmById(Long id);
    public String addLike(Long id, Long userId);
    public boolean deleteLike(Long id, Long userId);
    List<Film> commonFilmsList(Long userId, Long friendId);
    public List<Film> findPopularFilms(Integer count);
}
