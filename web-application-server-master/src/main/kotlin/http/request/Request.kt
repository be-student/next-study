package http.request

import http.method.HttpMethod

data class Request(
    val method: HttpMethod,
    val path: String,
    val headers: Map<String, String>,
    val query: Map<String, String>,
    val body: Map<String, String>
) {
}
