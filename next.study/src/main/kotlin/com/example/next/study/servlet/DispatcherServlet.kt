package com.example.next.study.servlet

import com.example.next.study.dao.UserDao
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator

@WebServlet(name = "dispatcher", urlPatterns = ["/"], loadOnStartup = 1)
class DispatcherServlet : HttpServlet(), ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent) {
        val resourceDatabasePopulator = ResourceDatabasePopulator()
        resourceDatabasePopulator.addScript(ClassPathResource("something.sql"))
        DatabasePopulatorUtils.execute(resourceDatabasePopulator, UserDao.dataSource)
    }

    companion object {
        @java.io.Serial
        private const val serialVersionUID: Long = 0
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val findAll = UserDao.findAll()

        resp.characterEncoding = "UTF-8"
        resp.contentType = "text/html;charset=UTF-8"
        val writer = resp.writer
        writer.println(
            """
            <html>
                <head>
                    <title>사용자 목록</title>
                </head>
                <body>
                    <table>
                        <thead>
                            <tr>
                                <th>아이디</th>
                                <th>이름</th>
                                <th>이메일</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${
                findAll.joinToString("") { user ->
                    "<tr><td>${user.userId}</td><td>${user.name}</td><td>${user.email}</td></tr>"
                }
            }
                        </tbody>
                    </table>
                </body>
            </html>
        """.trimIndent()
        )
    }
}
