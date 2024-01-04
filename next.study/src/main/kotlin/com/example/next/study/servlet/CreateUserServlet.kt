package com.example.next.study.servlet

import com.example.next.study.dao.UserDao
import com.example.next.study.domain.User
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.Serial

//@WebServlet(name = "CreateUserServlet", urlPatterns = ["/users/create"])
class CreateUserServlet : HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val userId = req.getParameter("userId")
        val name = req.getParameter("name")
        val email = req.getParameter("email")
        val password = req.getParameter("password")
        val user = User(userId, name, email, password)
        UserDao.insert(user)
        println("user = $user")
        resp.status = HttpServletResponse.SC_MOVED_TEMPORARILY
        resp.setHeader("Location", "/")
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2958870642550870448L
    }
}
