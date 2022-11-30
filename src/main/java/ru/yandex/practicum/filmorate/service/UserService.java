package ru.yandex.practicum.filmorate.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validator.IdValidator;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @SneakyThrows
    public User create(User user) {
        UserValidator.isValidUser(user);

        if(userStorage.findAll().contains(user)) {
            log.info("Попытка добавить уже существующего пользователя");
            throw new UserAlreadyExistException("Пользователь уже существует");
        }

        if(user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("В качестве name установлен login пользователя: {}", user.getLogin());
        }

        return userStorage.create(user);
    }

    @SneakyThrows
    public User update(User user) {
        UserValidator.isValidUser(user);
        IdValidator.isValidId(user.getId());
        findUserById(user.getId());

        if(user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("В качестве name установлен login пользователя: {}", user.getLogin());
        }
        return userStorage.update(user);
    }

    /**
     * получение списка пользователей
     */
    public List<User> findAll() {
        return userStorage.findAll();
    }

    /**
     * найти пользователя по id
     */
    public User findUserById(Long id) {
        return userStorage.findUserById(id);
    }

    /**
     * Удаление пользователей из списка
     */
    public void clearUsers() {
        userStorage.clearUsers();
    }

    /**
     * Удаление пользователя по id
     */
    public void deleteUserById(String idStr) {
        int id = Integer.parseInt(idStr);
        userStorage.deleteUserById(id);
    }

    /**
     * добавление в друзья
     */
    public String addAsFriend(Long id, Long friendId) {
        IdValidator.isValidId(id, friendId);
        findUserById(id);
        findUserById(friendId);

        if (!id.equals(friendId)) {
            userStorage.addAsFriend(id, friendId);
            log.info("Пользователь с id {} добавлен в друзья к пользователю {} ", friendId, id);
            return String.format("Пользователь с id %d  добавлен в друзья к пользователю %d", friendId, id);
        } else {
            log.info("Попытка добавить пользователя с id {} в друзья к пользователю с id  {}", id, friendId);
            throw new InvalidIdException ("Вы не можете добавить себя к себе в друзья");
        }
    }

    /**
     * вывод списка друзей
     */
    public List<User> getFriends(Long id) {
        IdValidator.isValidId(id);
        findUserById(id);

        return userStorage.getFriends(id);
    }

    /**
     * удаление из друзей
     */
    public String deleteFromFriend(Long id, Long friendId) {
        IdValidator.isValidId(id, friendId);
        findUserById(id);
        findUserById(friendId);

        if (userStorage.deleteFromFriend(id, friendId)) {
            log.info("У пользователя с id {} удален из друзей пользователь с id {}", id, friendId);
            return String.format("У пользователя с id %d удален из друзей пользователь с id %d", id, friendId);
        } else {
            log.info("У пользователя с id {} нет друга с id {}", id, friendId);
            return String.format("У пользователя с id %d нет друга с id %d", id, friendId);
        }
    }

    /**
     * вывод списка общих друзей
     */
    public List<User> mutualFriendsList(Long id, Long otherId) {
        IdValidator.isValidId(id, otherId);
        findUserById(id);
        findUserById(otherId);
        log.info("Найдены общие друзья пользователей с id {} и id {}", id, otherId);
        return userStorage.mutualFriendsList(id, otherId);
    }

    /**
     * Возвращает ленту событий пользователя.
     * */
    public List<Feed> findFeedByIdUser(String id) {
        IdValidator.isValidId(Long.valueOf(id));
        findUserById(Long.valueOf(id));
        return userStorage.findFeedByIdUser(id);
    }
}
