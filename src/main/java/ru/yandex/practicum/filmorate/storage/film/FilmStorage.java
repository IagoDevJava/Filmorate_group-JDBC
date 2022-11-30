package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> findAll();

    Film findFilmById(Long id);

    /**
     * Очистить список фильмов
     */
    void clearFilms();

    /**
     * Удаление фильма по id
     */
    void deleteFilmById(String id);

    String addLike(Long id, Long userId);

    boolean deleteLike(Long id, Long userId);

    List<Film> commonFilmsList(Long userId, Long friendId);


    List<Film> findPopularFilms(Integer count);

    //поиск фильмов режиссера
    List<Film> findDirectorFilms(Long directorId, String sort);

    // поиск популярных фильмов по году
    List<Film> findPopularFilms(Integer count, Integer year);

    // поиск популярных фильмов по жанру
    List<Film> findPopularFilms(Integer count, Long genreId);

    // поиск популярных фильмов по году и жанру
    List<Film> findPopularFilms(Integer count, Long genreId, Integer year);


    List<Film> searchFilmByDirector(String query, List<String> values);

    List<Film> searchFilmByTitle(String query, List<String> values);

    List<Film> searchFilmByTitleAndDirector(String query, List<String> values);

    List<Film> findFilmsByIdsOrdered(List<Long> ids);

}
