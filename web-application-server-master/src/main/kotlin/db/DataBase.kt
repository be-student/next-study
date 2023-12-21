package db

import com.google.common.collect.Maps
import model.User


object DataBase {
    private val users: MutableMap<String, User> = Maps.newHashMap()
    fun addUser(user: User) {
        users[user.userId] = user
    }

    fun findUserById(userId: String): User? {
        return users[userId]
    }

    fun findAll(): Collection<User> {
        return users.values
    }
}
