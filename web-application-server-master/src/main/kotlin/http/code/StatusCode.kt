package http.code

enum class StatusCode(val httpCode: Int) {
    OK(200),
    REDIRECT(301),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);
}
