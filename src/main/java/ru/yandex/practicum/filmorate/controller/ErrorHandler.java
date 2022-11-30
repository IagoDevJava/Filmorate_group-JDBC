package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;

import java.util.Map;


@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleIncorrectCountException(final IncorrectCountException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleFilmNotFoundException(final FilmNotFoundException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(final UserNotFoundException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleDirectorNotFoundException(final DirectorNotFoundException e){
        return new ResponseEntity<>(Map.of("error:", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleInvalidIdException(final InvalidIdException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleFilmAlreadyExistException(final FilmAlreadyExistException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistException(final UserAlreadyExistException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleAlreadyExistException(final AlreadyExistException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleInvalidNameException(final InvalidNameException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handInvalidDescriptionException(final InvalidDescriptionException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handInvalidReleaseDateException(final InvalidReleaseDateException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handInvalidDurationException(final InvalidDurationException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleInvalidEmailException(final InvalidEmailException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleInvalidBirthdateException(final InvalidBirthdateException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleNotFoundException(final NotFoundException e) {
        return new ResponseEntity<>(Map.of("error:", e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

}
