package application.db

import application.model.User
import com.google.common.collect.Maps


object UserDataBase {
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
