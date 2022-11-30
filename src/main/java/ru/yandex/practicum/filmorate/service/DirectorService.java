package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage storage;

    public List<Director> findAll(){
        log.info("Попытка получить всех режиссеров");
        return storage.findAll();
    }

    public Director findById(Long id){
        log.info("Попытка найти режиссера с id = {}", id);
        return storage.findDirectorById(id);
    }

    public Director create(Director director){
        log.info("Попытка создать режиссера");
        return storage.create(director);
    }

    public Director update(Director director){
        log.info("Попытка обновить режиссера");
        return storage.update(director);
    }

    public void delete(Long id){
        log.info("Попытка удалить режиссера с id = {}", id);
        storage.deleteDirectorById(id);
    }
}
