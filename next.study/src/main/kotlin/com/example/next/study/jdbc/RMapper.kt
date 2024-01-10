package com.example.next.study.jdbc

import java.sql.ResultSet

interface RMapper<T> {

    fun map(resultSet: ResultSet, index: Int): T
}
