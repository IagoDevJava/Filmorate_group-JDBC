package ru.yandex.practicum.filmorate.storage.feed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.time.LocalDateTime;

@Component
@Primary
@Slf4j
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * создание компонента ленты
     */
    @Override
    public void createFeedEntity(Long userId, Long entityId, String eventType, String operation) {
        long idFeed = 1L;
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM FEED ORDER BY EVENT_ID DESC LIMIT 1");
        if (userRows.next()) {
            idFeed = userRows.getInt("EVENT_ID");
            log.info("Последний установленный id: {}", idFeed);
            idFeed++;
        }
        log.info("Установлен id компонента ленты: {}", idFeed);

        Feed feed = Feed.builder()
                .eventId(idFeed)
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(operation)
                .timestamp(System.currentTimeMillis())
                .build();

        String sql = "INSERT INTO FEED(EVENT_ID, CREATE_TIME, USER_ID, OPERATION, EVENT_TYPE, ENTITY_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, idFeed, feed.getTimestamp(), userId, operation, eventType, entityId);
        log.info("Добавлен новый компонент ленты: {}", feed);
    }
}
