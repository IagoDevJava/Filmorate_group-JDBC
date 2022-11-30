package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {
    private final LikeDbStorage likeDbStorage;
    private final FilmDbStorage filmDbStorage;

    /**
     * Матрица лайков, представляет собой таблицу, где по вертикали и горизонтали фильмы,
     * а на пересечениях - суммарное количество лайков фильму X, от тех кто поставил лайк фильму Y
     */
    private final Map<Long, Map<Long, Integer>> likeMatrix = new HashMap<>();

    public RecommendationService(
            LikeDbStorage likeDbStorage,
            FilmDbStorage filmDbStorage
    ) {
        this.likeDbStorage = likeDbStorage;
        this.filmDbStorage = filmDbStorage;
    }

    public List<Film> getRecommendation(Long id) {
        List<Like> likes = likeDbStorage.findAll();
        if (likes.stream().noneMatch(like -> Objects.equals(like.getUserId(), id))) {
            return Collections.emptyList();
        }
        buildLikeMatrix(likes);
        List<Long> usersLikedFilms = likes.stream()
                .filter(like -> Objects.equals(like.getUserId(), id))
                .map(Like::getFilmId)
                .collect(Collectors.toList());
        List<Long> recommendationsIds = recommend(usersLikedFilms);
        if (recommendationsIds.isEmpty()) {
            return Collections.emptyList();
        }
        return filmDbStorage.findFilmsByIdsOrdered(recommendationsIds);
    }

    /**
     * Метод строит матрицу перед каждым запросом, по идее лучше сделать этот метод периодическим,
     * что бы он самостоятельно обновлялся раз в минуту например
     */
    private void buildLikeMatrix(List<Like> likes) {
        // вначале очищаем старую матрицу
        likeMatrix.clear();
        HashMap<Long, List<Long>> likeMap = new HashMap<>();
        // потом строим мапу юзер->список залайканых фильмов
        likes.forEach(like -> {
            Long userId = like.getUserId();
            if (!likeMap.containsKey(userId)) {
                likeMap.put(userId, new ArrayList<>());
            }
            likeMap.get(userId).add(like.getFilmId());
        });
        // дальше для каждого юзера (или списка залайканых фильмов)...
        for (List<Long> user : likeMap.values()) {
            // ...для каждого фильма из списка...
            for (Long film : user) {
                //...проходимся по матрице и создаём ключи первого уровня
                if (!likeMatrix.containsKey(film)) {
                    likeMatrix.put(film, new HashMap<>());
                }
                // а дальше уже внутри каждого ключа ещё раз проходимся по всему списку фильмов
                // и суммируем лайкосы и ставим их на места пересечения фильмов
                for (Long film2 : user) {
                    int likeCount = likeMatrix.get(film).getOrDefault(film2, 0) + 1;
                    likeMatrix.get(film).put(film2, likeCount);
                }
            }
        }
    }

    /**
     * Метод возвращает список ID фильмов в порядке убывания по релевантности
     */
    private List<Long> recommend(List<Long> userLikes) {
        // создаём мапу фильм->очки релевантности,
        // очками релевантности тут является сумма лайков из матрицы лайков
        // только по тем фильмам, которые лайкнул юзер
        Map<Long, Integer> filmRate = new HashMap<>();
        // идём по фильмам, которые лайкнул юзер
        for (Long filmId : userLikes) {
            // потом по строке матрицы с этим фильмом
            for (Map.Entry<Long, Integer> filmToLikes : likeMatrix.get(filmId).entrySet()) {
                // дальше создаём в мапе фильм, если его нет и ставим пока ноль очков релевантности
                if (!filmRate.containsKey(filmToLikes.getKey())) {
                    filmRate.put(filmToLikes.getKey(), 0);
                }
                // и берём предыдущее значение в мапе и накидываем к нему ещё из матрицы
                int likeCount = filmRate.get(filmToLikes.getKey()) + filmToLikes.getValue();
                filmRate.put(filmToLikes.getKey(), likeCount);
            }
        }
        return filmRate.entrySet().stream()
                // дальше сортируем фильмы по релевантности
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                // убираем из списка фильмы уже лайкнутые юзером
                .filter(entry -> !userLikes.contains(entry.getKey()))
                // мапим в лонг
                .map(Map.Entry::getKey)
                // и отдаём
                .collect(Collectors.toList());
    }
}
