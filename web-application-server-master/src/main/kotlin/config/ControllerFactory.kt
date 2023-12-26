package config

import application.controller.CreateUserController
import application.controller.LoginController
import application.controller.UserListController
import application.db.UserDataBase
import http.controller.Controller

object ControllerFactory {

    fun getAllControllers(): List<Controller> {
        return listOf(
            CreateUserController(userDatabase()),
            LoginController(userDatabase()),
            UserListController(userDatabase())
        )
    }

    private fun userDatabase(): UserDataBase {
        return UserDataBase
    }
}
