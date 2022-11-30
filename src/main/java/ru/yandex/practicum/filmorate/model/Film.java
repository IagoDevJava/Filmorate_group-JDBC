package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
@Builder
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    long duration;
    Long rate;
    Mpa mpa;
    List<Genre> genres;
    List<Director> directors;

    public Film(Long id, String name, String description, LocalDate releaseDate, long duration, Long rate,
                Mpa mpa, List<Genre> genres, List<Director> directors) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.rate = rate;
        this.mpa = mpa;
        this.genres = genres;
        this.directors = directors;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return duration == film.duration && name.equals(film.name) && description.equals(film.description)
                && releaseDate.equals(film.releaseDate) && mpa.equals(mpa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, releaseDate, duration);
    }

}
