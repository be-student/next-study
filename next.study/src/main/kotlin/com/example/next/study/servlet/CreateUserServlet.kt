package com.example.next.study.servlet

import com.example.next.study.config.UserDataSourceConfig
import com.example.next.study.dao.Template
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.Serial

//@WebServlet(name = "CreateUserServlet", urlPatterns = ["/users/create"])
class CreateUserServlet(
    private val userTemplate: Template = UserDataSourceConfig.userTemplate
) : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val userId = req.getParameter("userId")
        val name = req.getParameter("name")
        val email = req.getParameter("email")
        val password = req.getParameter("password")

        userTemplate.insert("INSERT INTO USERS VALUES(?, ?, ?, ?)", userId, name, email, password)
        resp.status = HttpServletResponse.SC_MOVED_TEMPORARILY
        resp.setHeader("Location", "/")
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2958870642550870448L
    }
}
