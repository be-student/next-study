package com.example.next.study.connection

import com.example.next.study.test.AbstractSprintBootTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.Connection
import javax.sql.DataSource


class ConnectionTest : AbstractSprintBootTest() {

    @Autowired
    private lateinit var dataSource: DataSource

    @Test
    fun connectionTest() {
        TransactionSynchronizationManager.initSynchronization()

        val conn: Connection = DataSourceUtils.getConnection(dataSource)

        DataSourceUtils.releaseConnection(conn, dataSource)

        val conn2: Connection = DataSourceUtils.getConnection(dataSource)

        assertThat(conn).isEqualTo(conn2)

        TransactionSynchronizationManager.clearSynchronization()
    }

    @Test
    fun `initSynchronization을 2번 하면 예외 발생`() {
        TransactionSynchronizationManager.initSynchronization()

        assertThrows<IllegalStateException> {
            TransactionSynchronizationManager.initSynchronization()
        }
        TransactionSynchronizationManager.clearSynchronization()
    }

    @Test
    fun connectionTest2() {

        val conn: Connection = DataSourceUtils.getConnection(dataSource)

        DataSourceUtils.releaseConnection(conn, dataSource)

        val conn2: Connection = DataSourceUtils.getConnection(dataSource)

        assertThat(conn).isNotEqualTo(conn2)
    }

    @Test
    fun threadLocalTest() {
        val threadLocal = ThreadLocal<String>()
        threadLocal.set("hello")
        assertThat(threadLocal.get()).isEqualTo("hello")
        threadLocal.remove()
        assertThat(threadLocal.get()).isNull()
    }
}
