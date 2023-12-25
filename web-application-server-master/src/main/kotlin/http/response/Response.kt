package http.response

import http.code.StatusCode
import http.cookie.Cookie

data class Response(
    val statusCode: StatusCode,
    val body: ByteArray,
    val headers: Map<String, String>,
    val cookies: List<Cookie>
) {
    companion object {
        private val EMPTY_BODY = ByteArray(0)
        fun ok(body: String): Response {
            return Response(StatusCode.OK, body.toByteArray(), emptyMap(), emptyList())
        }

        fun ok(body: String, headers: Map<String, String>): Response {
            return Response(StatusCode.OK, body.toByteArray(), headers, emptyList())
        }

        fun redirect(url: String): Response {
            return Response(StatusCode.REDIRECT, EMPTY_BODY, mapOf("Location" to url), emptyList())
        }

        fun notFound(): Response {
            return Response(StatusCode.NOT_FOUND, EMPTY_BODY, emptyMap(), emptyList())
        }

        fun notFound(body: String): Response {
            return Response(StatusCode.NOT_FOUND, body.toByteArray(), emptyMap(), emptyList())
        }

        fun badRequest(): Response {
            return Response(StatusCode.BAD_REQUEST, EMPTY_BODY, emptyMap(), emptyList())
        }

        fun unauthorized(): Response {
            return Response(StatusCode.UNAUTHORIZED, EMPTY_BODY, emptyMap(), emptyList())
        }

        fun internalServerError(): Response {
            return Response(StatusCode.INTERNAL_SERVER_ERROR, EMPTY_BODY, emptyMap(), emptyList())
        }

        fun ok(body: ByteArray, headers: Map<String, String>): Response {
            return Response(StatusCode.OK, body, headers, emptyList())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Response

        if (statusCode != other.statusCode) return false
        if (!body.contentEquals(other.body)) return false
        if (headers != other.headers) return false
        if (cookies != other.cookies) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode.hashCode()
        result = 31 * result + body.contentHashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + cookies.hashCode()
        return result
    }
}
