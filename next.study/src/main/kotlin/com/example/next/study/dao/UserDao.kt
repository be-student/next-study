package com.example.next.study.dao

import com.example.next.study.domain.User
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.datasource.DataSourceUtils
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

object UserDao {
    val dataSource: DataSource = DataSourceBuilder.create()
        .apply {
            url("jdbc:h2:mem:testdb")
            username("sa")
            password("")
            driverClassName("org.h2.Driver")
        }.build()

    fun insert(user: User) {
        val con: Connection?
        val pstmt: PreparedStatement?
        try {
            con = DataSourceUtils.getConnection(dataSource)
            val sql = "INSERT INTO USERS VALUES(?,?,?,?)"
            pstmt = con.prepareStatement(sql)
            pstmt.setString(1, user.userId)
            pstmt.setString(2, user.name)
            pstmt.setString(3, user.email)
            pstmt.setString(4, user.password)
            pstmt.executeUpdate()
        } catch (e: Exception) {
            throw e
        }
    }

    fun findAll(): List<User> {
        val con: Connection?
        val pstmt: PreparedStatement?
        try {
            con = DataSourceUtils.getConnection(dataSource)
            val sql = "SELECT * FROM USERS"
            pstmt = con.prepareStatement(sql)
            val resultSet = pstmt.executeQuery()
            val users = mutableListOf<User>()
            while (resultSet.next()) {
                val userId = resultSet.getString("userId")
                val name = resultSet.getString("name")
                val email = resultSet.getString("email")
                val password = resultSet.getString("password")
                users.add(User(userId, name, email, password))
            }
            return users
        } catch (e: Exception) {
            throw e
        }
    }
}
