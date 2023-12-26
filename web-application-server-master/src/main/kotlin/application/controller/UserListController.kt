package application.controller

import application.db.UserDataBase
import application.model.User
import http.code.StatusCode
import http.controller.Controller
import http.cookie.Cookie
import http.method.HttpMethod
import http.request.Request
import http.response.Response
import java.util.function.Consumer

class UserListController(private val database: UserDataBase) : Controller {
    override fun supports(request: Request): Boolean {
        return request.path == "/user/list" && request.method == HttpMethod.GET
    }

    override fun handle(request: Request): Response {
        if (request.headers["Cookie"] == null || !request.headers["Cookie"]!!.contains("logined=true")) {
            return loginFail()
        }
        val users = UserDataBase.findAll()
        val sb = StringBuilder()
        users.forEach(Consumer { user: User ->
            sb.append(user.toString()).append("\n")
        })
        val responseBody = sb.toString()
        return Response.ok(responseBody)
    }

    private fun loginFail() = Response(
        StatusCode.REDIRECT,
        ByteArray(0),
        mapOf(
            "Location" to "/user/login_failed.html"
        ),
        listOf(
            Cookie("logined", "false")
        )
    )
}
