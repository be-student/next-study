package http.method

enum class HttpMethod {
    GET, POST, PUT, DELETE;

    companion object {
        fun of(method: String): HttpMethod {
            return valueOf(method.uppercase())
        }
    }
}
