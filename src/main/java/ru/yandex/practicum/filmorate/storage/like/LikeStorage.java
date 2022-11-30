package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Like;

import java.util.List;

public interface LikeStorage {
    List<Like> findAll();
}
