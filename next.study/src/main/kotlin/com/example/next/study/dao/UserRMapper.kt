package com.example.next.study.dao

import com.example.next.study.domain.User
import com.example.next.study.jdbc.RMapper
import java.sql.ResultSet

class UserRMapper : RMapper<User> {
    override fun map(resultSet: ResultSet, index: Int): User {
        val userId = resultSet.getString("userId")
        val name = resultSet.getString("name")
        val email = resultSet.getString("email")
        val password = resultSet.getString("password")
        return User(userId, name, email, password)
    }
}
