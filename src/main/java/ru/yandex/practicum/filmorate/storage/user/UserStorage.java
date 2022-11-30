package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User create(User user);
    User update(User user);
    List<User> findAll();
    User findUserById(Long id);

    void clearUsers();

    void deleteUserById(long id);

    String addAsFriend(Long id, Long friendId);
    List<User> getFriends(Long id);
    boolean deleteFromFriend(Long id, Long friendId);
    List<User> mutualFriendsList(Long id, Long otherId);

    /**
     * Возвращает ленту событий пользователя.
     * */
    List<Feed> findFeedByIdUser(String id);
}
