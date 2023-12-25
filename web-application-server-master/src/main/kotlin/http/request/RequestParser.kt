package http.request

import http.method.HttpMethod

object RequestParser {

    fun parseRequest(requestLines: MutableList<String>, body: String): Request {
        val requestLine = requestLines[0].split(" ")
        val pathAndQuery = requestLine[1].split("?")
        val query = parseQuery(pathAndQuery.drop(1))
        val method = HttpMethod.valueOf(requestLine[0])
        val headers = parseHeaders(requestLines.drop(1))
        val bodyMap = parseBody(body)
        return Request(method, pathAndQuery[0], headers, query, bodyMap)
    }

    fun parseQuery(query: List<String>): Map<String, String> {
        return query
            .flatMap { it.split("&") }
            .map { it.split("=") }
            .associate { it[0] to it[1] }
    }

    fun parseBody(body: String): Map<String, String> {
        return emptyMap()
    }

    fun parseHeaders(requestLines: List<String>): Map<String, String> {
        return requestLines
            .takeWhile { it != "" }
            .map { it.split(": ") }
            .associate { it[0] to it[1] }
    }
}
