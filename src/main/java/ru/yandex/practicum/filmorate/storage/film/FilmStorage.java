package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    public Film create(Film film);
    public Film update(Film film);
    public List<Film> findAll();
    public Film findFilmById(Long id);
    public String addLike(Long id, Long userId);
    public boolean deleteLike(Long id, Long userId);
    public List<Film> findPopularFilms(Integer count);
    public List<Film> findPopularFilms(Integer count, Integer year);
    public List<Film> findPopularFilms(Integer count, Long genreId);
    public List<Film> findPopularFilms(Integer count, Long genreId, Integer year);
}
