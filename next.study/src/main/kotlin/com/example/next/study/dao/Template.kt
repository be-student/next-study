package com.example.next.study.dao

import com.example.next.study.jdbc.RMapper
import org.springframework.jdbc.datasource.DataSourceUtils
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

class Template(
    val dataSource: DataSource
) {
    fun insert(sql: String, vararg objects: Any) {
        try {
            val con = DataSourceUtils.getConnection(dataSource)
            val pstmt = preparedStatement(con, sql, objects)
            pstmt.executeUpdate()
        } catch (e: Exception) {
            throw e
        }
    }

    fun <T> findAll(sql: String, rMapper: RMapper<T>, vararg objects: Any): List<T> {
        try {
            val con = DataSourceUtils.getConnection(dataSource)
            val pstmt = preparedStatement(con, sql, objects)
            val resultSet = pstmt.executeQuery()
            val results = mutableListOf<T>()
            while (resultSet.next()) {
                results.add(rMapper.map(resultSet, resultSet.row))
            }
            return results
        } catch (e: Exception) {
            throw e
        }
    }

    private fun preparedStatement(
        con: Connection,
        sql: String,
        objects: Array<out Any>
    ): PreparedStatement {
        val pstmt = con.prepareStatement(sql)
        for (i in objects.indices) {
            pstmt.setObject(i + 1, objects[i])
        }
        return pstmt
    }
}
