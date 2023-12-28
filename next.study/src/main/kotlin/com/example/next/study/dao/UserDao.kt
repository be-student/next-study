package com.example.next.study.dao

import com.example.next.study.domain.User

object UserDao {
    private val users = mutableListOf<User>()
    fun insert(user: User) {
        users.add(user)
    }

    fun findAll(): List<User> {
        return users
    }
}
