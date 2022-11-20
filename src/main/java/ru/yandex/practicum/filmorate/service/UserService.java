package ru.yandex.practicum.filmorate.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        if (user.getId() != null && findUserById(user.getId()) != null) {
            log.info("Попытка добавить пользователя с уже существующим id");
            throw new UserAlreadyExistException(String.format("id %d уже существует", user.getId()));
        }
        if (userStorage.findAll().contains(user)) {
            log.info("Попытка добавить уже существующего пользователя");
            throw new UserAlreadyExistException("Пользователь уже существует");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank()) {
            log.info("Попытка создать пользователя без указания логина");
            throw new InvalidNameException("Отсутствует логин пользователя");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.info("Попытка создать пользователя с некорректным email");
            throw new InvalidEmailException(String.format("Некорректный email: %s", user.getEmail()));
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Попытка добавить пользователя из будущего");
            throw new InvalidBirthdateException(String.format("Некорректная дата рождения пользователя: %s", user.getBirthday()));
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Попытка добавить пользователя с пустым полем name");
            user.setName(user.getLogin());
            log.info("В значение пустого поля name установлен логин пользователя: {}", user.getLogin());
        }

        return userStorage.create(user);
    }

    @SneakyThrows
    public User update(User user) {
        if (user.getId() == null || findUserById(user.getId()) == null) {
            log.info("Попытка обновить пользователя с пустым или несуществующим id");
            throw new InvalidIdException(String.format("Пользователь с пустым или несуществующим id %d", user.getId()));
        }
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank()) {
            log.info("Попытка обновить пользователя без указания логина");
            throw new InvalidNameException("Отсутствует логин пользователя");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.info("Попытка обновить пользователя с некорректным email");
            throw new InvalidEmailException(String.format("Некорректный email: %s", user.getEmail()));
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Попытка обновить пользователя с датой рождения в будущем");
            throw new InvalidBirthdateException(String.format("Некорректная дата рождения пользователя: %s", user.getBirthday()));
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Попытка обновить пользователя с пустым полем name");
            user.setName(user.getLogin());
            log.info("В значение пустого поля name установлен логин пользователя: {}", user.getLogin());
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
        if (userStorage.findUserById(id) != null) {
            return userStorage.findUserById(id);
        } else {
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        }
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
        checkId(id, friendId);
        if (findUserById(id) != null) {
            if (findUserById(friendId) != null) {
                if (!id.equals(friendId)) {
                    userStorage.addAsFriend(id, friendId);
                    log.info("Пользователь с id {} добавлен в друзья к пользователю {} ", friendId, id);
                    return String.format("Пользователь с id %d  добавлен в друзья к пользователю %d", friendId, id);
                } else {
                    log.info("Попытка добавить пользователя с id {} в друзья к пользователю с id  {}", id, friendId);
                    throw new InvalidIdException("Вы не можете добавить себя к себе в друзья");
                }
            } else {
                log.info("Попытка добавить в друзья пользователя с несуществующим id {}", friendId);
                throw new UserNotFoundException(String.format("Пользователь с id %d не найден", friendId));
            }
        } else {
            log.info("Попытка добавить друга к пользователю с несуществующим id {}", id);
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    /**
     * вывод списка друзей
     */
    public List<User> getFriends(Long id) {
        if (id == null) {
            log.info("Попытка получить список друзей пользователя с пустым id");
            throw new InvalidIdException("Пользователь с пустым id");
        }
        if (findUserById(id) == null) {
            log.info("Попытка получить список друзей пользователя с несуществующим id");
            throw new InvalidIdException(String.format("Пользователь с несуществующим id %d", id));
        }

        return userStorage.getFriends(id);
    }

    /**
     * удаление из друзей
     */
    public String deleteFromFriend(Long id, Long friendId) {
        checkId(id, friendId);
        if (findUserById(id) != null) {
            if (findUserById(friendId) != null) {
                if (userStorage.deleteFromFriend(id, friendId)) {
                    log.info("У пользователя с id {} удален из друзей пользователь с id {}", id, friendId);
                    return String.format("У пользователя с id %d удален из друзей пользователь с id %d", id, friendId);
                } else {
                    log.info("У пользователя с id {} нет друга с id {}", id, friendId);
                    return String.format("У пользователя с id %d нет друга с id %d", id, friendId);
                }

            } else {
                log.info("Попытка удалить из друзей пользователя с несуществующим id {}", friendId);
                throw new UserNotFoundException(String.format("Пользователь с id %d не найден", friendId));
            }

        } else {
            log.info("Попытка удалить друга у пользователя с несуществующим id {}", id);
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    /**
     * вывод списка общих друзей
     */
    public List<User> mutualFriendsList(Long id, Long otherId) {

        checkId(id, otherId);

        if (findUserById(id) != null && findUserById(otherId) != null) {
            log.info("Найдены общие друзья пользователей с id {} и id {}", id, otherId);
            return userStorage.mutualFriendsList(id, otherId);
        } else {
            log.info("Попытка найти друзей пользователя с несуществующим id {}", otherId);
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", otherId));
        }
    }

    /**
     * проверка id
     */
    private void checkId(Long id, Long friendId) {
        if (id == null || id < 1 || friendId == null || friendId < 1) {
            log.info("Пользователь с пустым или отрицательным id {} {}", id, friendId);
            throw new InvalidIdException("Пользователь с пустым или отрицательным id");
        }
    }

}
