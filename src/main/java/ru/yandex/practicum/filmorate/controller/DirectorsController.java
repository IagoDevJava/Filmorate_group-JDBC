package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorsController {

    private final DirectorService directorService;

    @GetMapping
    public List<Director> findAll(){
        log.info("Получен запрос GET/directors - список всех режиссеров");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable Long id){
        log.info("Получен запрос GET/directors/{id} - получение режиссера по id");
        return directorService.findById(id);
    }

    @PostMapping
    public Director create(@RequestBody Director director){
        log.info("Получен запрос POST/directors - создание режиссера");
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director){
        log.info("Получен запрос PUT/directors - изминение режиссера");
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        log.info("Получен запрос DELETE/directors/{id} - удаление режиссера");
        directorService.delete(id);
    }
}
