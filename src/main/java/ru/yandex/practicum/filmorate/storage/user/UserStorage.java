package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    public User create(User user);
    public User update(User user);
    public List<User> findAll();
    public User findUserById(Long id);

    void clearUsers();

    void deleteUserById(long id);

    public String addAsFriend(Long id, Long friendId);
    public List<User> getFriends(Long id);
    public boolean deleteFromFriend(Long id, Long friendId);
    public List<User> mutualFriendsList(Long id, Long otherId);

}
