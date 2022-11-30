package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;

public interface FeedStorage {

    /**
     * создание компонента ленты
     */
    void createFeedEntity(Long userId, Long entityId, String eventType, String operation);
}
