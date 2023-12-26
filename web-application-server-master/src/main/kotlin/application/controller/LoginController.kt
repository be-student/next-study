package application.controller

import application.db.UserDataBase
import http.code.StatusCode
import http.controller.Controller
import http.cookie.Cookie
import http.method.HttpMethod
import http.request.Request
import http.response.Response


class LoginController(private val database: UserDataBase) : Controller {
    override fun supports(request: Request): Boolean {
        return request.path == "/user/login" && request.method == HttpMethod.POST
    }

    override fun handle(request: Request): Response {
        val user = UserDataBase.findUserById(request.body["userId"]!!)
            ?: return loginFail()
        if (user.password == request.body["password"]) {
            return Response.redirect("/index.html")
        }
        return loginFail()
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
