package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmoRateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private final JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM FILM_GENRE");
        jdbcTemplate.update("DELETE FROM FILM_MPA");
        jdbcTemplate.update("DELETE FROM LIKES");
        jdbcTemplate.update("DELETE FROM FILMS");
        jdbcTemplate.update("DELETE FROM FRIENDS");
        jdbcTemplate.update("DELETE FROM USERS");
    }

    // User
    @Test
    public void FindUserByIdTest() {
        jdbcTemplate.update("INSERT INTO USERS (id, login, name, email, BIRTHDAY) " +
                "VALUES ( 1, 'Кое-кто', 'name', 'koe_kto@mail.ru', '1900-01-01')");

        Optional<User> userOptional = Optional.ofNullable(userStorage.findUserById(1L));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void createUserTest() {
        User user = new User(1L, "login2", "name2", "ya@ya.ru", LocalDate.of(1900,01,01), null);

        Optional<User> userOptional = Optional.ofNullable(userStorage.create(user));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user2 ->
                        assertThat(user)
                );
    }

    @Test
    public void updateUserTest() {
        User user = new User(1L, "login2", "name2", "ya@ya.ru", LocalDate.of(1900,01,01), null);

        Optional<User> userOptional = Optional.ofNullable(userStorage.create(user));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user2 ->
                        assertThat(user)
                );

        User userUp = new User(1L, "login4", "name4", "ya@ya.ru", LocalDate.of(1909,01,01), null);

        Optional<User> userOptional2 = Optional.ofNullable(userStorage.update(userUp));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user2 ->
                        assertThat(userUp)
                );
    }

    @Test
    public void findAllUserTest() {
        List<User> list = userStorage.findAll();
        assertNotNull(list, "Cписок не возвращается");
        assertEquals(0, list.size(), "Неверное количество элементов");

        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);
        User user2 = new User(2L, "login2", "name2", "ya2@ya.ru",
                LocalDate.of(1909,01,01), null);
        userStorage.create(user2);

        List<User> list2 = userStorage.findAll();
        assertNotNull(list2, "Cписок не возвращается");
        assertEquals(2, list2.size(), "Неверное количество элементов");

        Optional<User> userOptional1 = Optional.ofNullable(list2.get(0));
        Optional<User> userOptional2 = Optional.ofNullable(list2.get(1));

        assertThat(userOptional1)
                .isPresent()
                .hasValueSatisfying(userN ->
                        assertThat(user1)
                );

        assertThat(userOptional2)
                .isPresent()
                .hasValueSatisfying(userN ->
                        assertThat(user2)
                );

    }

    @Test
    public void addAsFriendTest() {
        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        User user2 = new User(2L, "login2", "name2", "ya2@ya.ru",
                LocalDate.of(1909,01,01), null);
        userStorage.create(user2);

        userStorage.addAsFriend(1L, 2L);
        List<Long> friends = userStorage.findUserById(1L).getFriends();

        assertNotNull(friends, "Cписок не возвращается");
        assertEquals(1, friends.size(), "Неверное количество элементов");
    }

    @Test
    public void getFriendsTest() {

        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        User user2 = new User(2L, "login2", "name2", "ya2@ya.ru",
                LocalDate.of(1909,01,01), null);
        userStorage.create(user2);

        userStorage.addAsFriend(1L, 2L);
        List<User> friends = userStorage.getFriends(1L);

        assertNotNull(friends, "Cписок не возвращается");
        assertEquals(1, friends.size(), "Неверное количество элементов");

        Optional<User> userOptional1 = Optional.ofNullable(friends.get(0));

        assertThat(userOptional1)
                .isPresent()
                .hasValueSatisfying(userN ->
                        assertThat(user2)
                );

    }

    @Test
    public void deleteFromFriendTest() {
        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        User user2 = new User(2L, "login2", "name2", "ya2@ya.ru",
                LocalDate.of(1909,01,01), null);
        userStorage.create(user2);

        userStorage.addAsFriend(1L, 2L);
        List<User> friends = userStorage.getFriends(1L);

        assertNotNull(friends, "Cписок не возвращается");
        assertEquals(1, friends.size(), "Неверное количество элементов");

        userStorage.deleteFromFriend(1L, 2L);
        List<User> friends2 = userStorage.getFriends(1L);

        assertNotNull(friends2, "Cписок не возвращается");
        assertEquals(0, friends2.size(), "Неверное количество элементов");
    }

    @Test
    public void mutualFriendsListTest() {

        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        User user2 = new User(2L, "login2", "name2", "ya2@ya.ru",
                LocalDate.of(1909,01,01), null);
        userStorage.create(user2);

        User user3 = new User(3L, "login3", "name3", "ya3@ya.ru",
                LocalDate.of(1903,01,01), null);
        userStorage.create(user3);

        userStorage.addAsFriend(1L, 2L);
        userStorage.addAsFriend(3L, 2L);
        List<User> friends = userStorage.mutualFriendsList(1L, 3L);

        assertNotNull(friends, "Cписок не возвращается");
        assertEquals(1, friends.size(), "Неверное количество элементов");

        Optional<User> userOptional1 = Optional.ofNullable(friends.get(0));

        assertThat(userOptional1)
                .isPresent()
                .hasValueSatisfying(userN ->
                        assertThat(user2)
                );
    }

    // Film
    @Test
    public void FindFilmByIdTest() {
        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.findFilmById(1L));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void createFilmTest() {
        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, null);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.create(film));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film)
                );
    }

    @Test
    public void updateFilmTest() {
        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, null);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.create(film));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film)
                );

        Film film2 = new Film(1L, "Какой-то НОВЫЙ фильм", "Какое-то еще описание",
                LocalDate.of(1900,01,01),120, null, null, null);

        Optional<Film> filmOptional2 = Optional.ofNullable(filmStorage.update(film2));

        assertThat(filmOptional2)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film2)
                );
    }

    @Test
    public void findAllFilmTest() {
        List<Film> list = filmStorage.findAll();
        assertNotNull(list, "Cписок не возвращается");
        assertEquals(0, list.size(), "Неверное количество элементов");

        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film);
        Film film2 = new Film(2L, "Какой-то НОВЫЙ фильм", "Какое-то еще описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film2);

        List<Film> list2 = filmStorage.findAll();
        assertNotNull(list2, "Cписок не возвращается");
        assertEquals(2, list2.size(), "Неверное количество элементов");

        Optional<Film> filmOptional1 = Optional.ofNullable(list2.get(0));
        Optional<Film> filmOptional2 = Optional.ofNullable(list2.get(1));

        assertThat(filmOptional1)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film)
                );

        assertThat(filmOptional2)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film2)
                );

    }

    /**
     * тест на удаление фильма по id
     */
    @Test
    public void isDeleteFilmById() {
        Film film = Film.builder()
                .id(1L)
                .name("film1")
                .description("desc1")
                .releaseDate(LocalDate.parse("2010-10-10"))
                .duration(120)
                .rate(0L)
                .mpa(new Mpa(1L, "G"))
                .genres(new ArrayList<>())
                .build();
        filmStorage.create(film);
        filmStorage.deleteFilmById(String.valueOf(1));

        assertThrows(FilmNotFoundException.class, () -> {
            filmStorage.findFilmById(1L);
        });
    }

    /**
     * тест на удаление фильмов
     */
    @Test
    public void isDeleteFilms() {
        Film film = Film.builder()
                .id(1L)
                .name("film1")
                .description("desc1")
                .releaseDate(LocalDate.parse("2010-10-10"))
                .duration(120)
                .rate(0L)
                .mpa(new Mpa(1L, "G"))
                .genres(new ArrayList<>())
                .build();
        filmStorage.create(film);
        filmStorage.clearFilms();

        assertThrows(FilmNotFoundException.class, () -> {
            filmStorage.findFilmById(1L);
        });
    }

    @Test
    public void addLikeTest() {
        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film);

        filmStorage.addLike(1L, 1L);
        Long l = filmStorage.findFilmById(1L).getRate();

        assertEquals(1L, l, "Значение не совпадает");

    }

    @Test
    public void deleteLikeTest() {
        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film);

        filmStorage.addLike(1L, 1L);
        Long l = filmStorage.findFilmById(1L).getRate();

        assertEquals(1L, l, "Значение не совпадает");

        filmStorage.deleteLike(1L, 1L);
        Long l1 = filmStorage.findFilmById(1L).getRate();

        assertEquals(0, l1, "Значение не совпадает");
    }

    @Test
    public void findPopularFilmsTest() {
        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film);
        Film film2 = new Film(2L, "Какой-то НОВЫЙ фильм", "Какое-то еще описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film2);
        Film film3 = new Film(3L, "Какой-то еще один НОВЫЙ фильм", "Какое-то еще одно описание",
                LocalDate.of(1900,01,01),120, null, null, null);
        filmStorage.create(film3);

        User user1 = new User(1L, "login1", "name1", "ya1@ya.ru",
                LocalDate.of(1900,01,01), null);
        userStorage.create(user1);

        User user2 = new User(2L, "login2", "name2", "ya2@ya.ru",
                LocalDate.of(1909,01,01), null);
        userStorage.create(user2);

        filmStorage.addLike(1L, 1L);
        filmStorage.addLike(2L, 1L);
        filmStorage.addLike(2L, 2L);

        List<Film> list2 = filmStorage.findPopularFilms(3);
        assertNotNull(list2, "Cписок не возвращается");
        assertEquals(3, list2.size(), "Неверное количество элементов");

        Optional<Film> filmOptional1 = Optional.ofNullable(list2.get(0));
        Optional<Film> filmOptional2 = Optional.ofNullable(list2.get(1));
        Optional<Film> filmOptional3 = Optional.ofNullable(list2.get(2));

        assertThat(filmOptional1)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film)
                );

        assertThat(filmOptional2)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film2)
                );

        assertThat(filmOptional3)
                .isPresent()
                .hasValueSatisfying(filmN ->
                        assertThat(film3)
                );
    }

    // Mpa
    @Test
    public void findAllMpaTest() {
        List<Mpa> list = mpaStorage.findAll();
        assertNotNull(list, "Cписок не возвращается");
        assertEquals(5, list.size(), "Неверное количество элементов");

    }

    @Test
    public void FindMpaByIdTest() {
        Optional<Mpa> mpaOptional = Optional.ofNullable(mpaStorage.findMpaById(1L));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void getMpaTest() {
        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");
        jdbcTemplate.update("INSERT INTO FILM_MPA (film_id, mpa_id) VALUES ( 1, 1)");

        Optional<Mpa> mpaOptional = Optional.ofNullable(mpaStorage.getMpa(1L));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void addMpaTest() {
        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
              LocalDate.of(1900,01,01),120, null, new Mpa(1L, "G"), null);

        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");
        mpaStorage.addMpa(film);
        Optional<Mpa> mpaOptional = Optional.ofNullable(mpaStorage.getMpa(1L));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void updateMpaTest() {
        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, new Mpa(1L, "G"), null);

        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");
        mpaStorage.addMpa(film);

        Optional<Mpa> mpaOptional = Optional.ofNullable(mpaStorage.getMpa(1L));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L)
                );

        Film film2 = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, new Mpa(2L, "PG"), null);

        mpaStorage.updateMpa(film2);
        Optional<Mpa> mpaOptional2 = Optional.ofNullable(mpaStorage.getMpa(1L));

        assertThat(mpaOptional2)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 2L)
                );
    }

    // Genre
    @Test
    public void findAllGenreTest() {
        List<Genre> list = genreStorage.findAll();
        assertNotNull(list, "Cписок не возвращается");
        assertEquals(6, list.size(), "Неверное количество элементов");

    }

    @Test
    public void FindGenreByIdTest() {
        Optional<Genre> optional = Optional.ofNullable(genreStorage.findGenreById(1L));

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(o ->
                        assertThat(o).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void getGenreTest() {
        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");
        jdbcTemplate.update("INSERT INTO FILM_GENRE (film_id, genre_id) VALUES ( 1, 1), ( 1, 2), ( 1, 3)");

        List<Genre> list = genreStorage.getGenre(1L);

        assertNotNull(list, "Cписок не возвращается");
        assertEquals(3, list.size(), "Неверное количество элементов");

    }

    @Test
    public void addGenreTest() {
        Genre g = new Genre(1L, "Комедия");
        List<Genre> l = new ArrayList<>();
        l.add(g);

        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, l);

        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");
        genreStorage.addGenre(film);

        List<Genre> list = genreStorage.getGenre(1L);
        assertNotNull(list, "Cписок не возвращается");
        assertEquals(1, list.size(), "Неверное количество элементов");

        Optional<Genre> optional = Optional.ofNullable(list.get(0));

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(o ->
                        assertThat(g)
                );
    }

    @Test
    public void updateGenreTest() {
        Genre g = new Genre(1L, "Комедия");
        List<Genre> l = new ArrayList<>();
        l.add(g);

        Film film = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, l);

        jdbcTemplate.update("INSERT INTO FILMS (id, name, description, releasedate, duration) " +
                "VALUES ( 1, 'Какой-то фильм', 'Какое-то описание', '1900-01-01', '120')");
        genreStorage.addGenre(film);

        List<Genre> list = genreStorage.getGenre(1L);
        assertNotNull(list, "Cписок не возвращается");
        assertEquals(1, list.size(), "Неверное количество элементов");

        Optional<Genre> optional = Optional.ofNullable(list.get(0));

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(o ->
                        assertThat(g)
                );

        Genre g2 = new Genre(2L, "Драма");
        List<Genre> l2 = new ArrayList<>();
        l.add(g2);

        Film film2 = new Film(1L, "Какой-то фильм", "Какое-то описание",
                LocalDate.of(1900,01,01),120, null, null, l2);

        genreStorage.addGenre(film2);

        List<Genre> list2 = genreStorage.getGenre(1L);
        assertNotNull(list2, "Cписок не возвращается");
        assertEquals(1, list.size(), "Неверное количество элементов");

        Optional<Genre> optional2 = Optional.ofNullable(list2.get(0));

        assertThat(optional2)
                .isPresent()
                .hasValueSatisfying(o ->
                        assertThat(g2)
                );
    }
}
