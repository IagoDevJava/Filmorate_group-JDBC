package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {
    private long eventId;
    private long userId;
    private long entityId;
    private String eventType;
    private String operation;
    private long timestamp;
}
