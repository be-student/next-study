package com.example.next.study.servlet

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*

//@WebServlet(name = "LoginServlet", urlPatterns = ["/login"])
class LoginServlet : HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val session = req.session
        val User = session.getAttribute("user")
        if (User == null) {
            session.setAttribute("user", UUID.randomUUID().toString())
        } else {
            println("User = $User")
        }
    }

    companion object {
        @java.io.Serial
        private const val serialVersionUID: Long = 2958870642550870448L
    }
}
