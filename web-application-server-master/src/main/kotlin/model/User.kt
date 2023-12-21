package model


class User(val userId: String, val password: String, private val name: String, private val email: String) {

    override fun toString(): String {
        return "User [userId=$userId, password=$password, name=$name, email=$email]"
    }
}
