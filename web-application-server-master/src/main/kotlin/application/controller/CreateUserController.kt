package application.controller

import application.db.UserDataBase
import application.model.User
import http.controller.Controller
import http.method.HttpMethod
import http.request.Request
import http.response.Response
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(CreateUserController::class.java)

class CreateUserController(private val database: UserDataBase) : Controller {
    override fun supports(request: Request): Boolean {
        return request.path == "/user/create" && request.method == HttpMethod.POST
    }

    override fun handle(request: Request): Response {
        val user = User(
            request.body["userId"]!!,
            request.body["password"]!!,
            request.body["name"]!!,
            request.body["email"]!!
        )
        log.info("user : {}", user)
        UserDataBase.addUser(user)
        return Response.redirect("/index.html")
    }
}
