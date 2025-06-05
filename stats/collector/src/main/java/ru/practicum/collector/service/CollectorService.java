package ru.practicum.collector.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface CollectorService {
    void sendAction(UserActionAvro actionAvro);
}
