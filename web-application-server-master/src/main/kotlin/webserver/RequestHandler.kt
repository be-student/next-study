package webserver

import db.DataBase
import http.code.StatusCode
import http.cookie.Cookie
import http.method.HttpMethod
import http.request.RequestParser
import http.response.Response
import model.User
import org.slf4j.LoggerFactory
import util.IOUtils
import java.io.*
import java.net.Socket
import java.nio.file.Files
import java.util.function.Consumer

class RequestHandler(private val connection: Socket) : Thread() {
    override fun run() {
        try {
            connection.getInputStream().use { `in` ->
                connection.getOutputStream().use { out ->
                    val response = 저지른_코드(`in`)

                    val dos = DataOutputStream(out)
                    val title = "HTTP/1.1 ${response.statusCode.httpCode} OK \r\n"
                    val headers = response.headers.map { (key, value) -> "$key: $value\r\n" }
                    val body = response.body
                    val contentLength = "Content-Length: ${body.size}\r\n"
                    val contentType = "Content-Type: text/html;charset=utf-8\r\n"
                    val cookies =
                        response.cookies.map { cookie -> "Set-Cookie: ${cookie.name}=${cookie.value}\r\n" }
                    val cookie = cookies.joinToString("")
                    val header = title + headers.joinToString("") + contentLength + contentType + cookie + "\r\n"
                    dos.writeBytes(header)
                    dos.write(body)
                    dos.flush()
                }
            }
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun 저지른_코드(`in`: InputStream): Response {
        val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
        val requestLines = MutableList(0) { "" }
        while (true) {
            val line = br.readLine()
            if (line == null || line == "") break
            requestLines.add(line)
        }
        if (requestLines.isEmpty()) {
            return Response.badRequest()
        }
        val header = RequestParser.parseHeaders(requestLines.drop(1))
        val body = IOUtils.readData(br, header["Content-Length"]?.toInt() ?: 0)
        val request = RequestParser.parseRequest(requestLines, body)
        // 클라이언트의 request정보
        log.info("method : {}", request.method)
        val file = File("./webapp${request.path}")
        if (file.exists() && file.isFile()) {
            val responseBody = Files.readAllBytes(file.toPath())
            if (request.path.endsWith(".css")) {
                val headers = mapOf(
                    "Content-Type" to "text/css",
                )
                return Response.ok(responseBody, headers)
            }
            if (request.path.endsWith(".html")) {
                val headers = mapOf(
                    "Content-Type" to "text/html;charset=utf-8",
                )
                return Response.ok(responseBody, headers)
            }
            throw IllegalStateException("지원하지 않는 파일 형식입니다.")
        }
        if (request.path == "/user/create" && request.method == HttpMethod.POST) {
            val user = User(
                request.body["userId"]!!,
                request.body["password"]!!,
                request.body["name"]!!,
                request.body["email"]!!
            )
            log.info("user : {}", user)
            DataBase.addUser(user)
            return Response.redirect("/index.html")
        }
        if (request.path == "/user/login" && request.method == HttpMethod.POST) {
            val user = DataBase.findUserById(request.body["userId"]!!)
                ?: return loginFail()
            if (user.password == request.body["password"]) {
                return Response.redirect("/index.html")
            }
            return loginFail()
        } else if (request.path == "/user/list") {
            if (request.headers["Cookie"] == null || !request.headers["Cookie"]!!.contains("logined=true")) {
                return loginFail()
            }
            val users = DataBase.findAll()
            val sb = StringBuilder()
            users.forEach(Consumer { user: User ->
                sb.append(user.toString()).append("\n")
            })
            val responseBody = sb.toString()
            return Response.ok(responseBody)
        }
        return Response.ok("Hello World")
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

    companion object {
        private val log = LoggerFactory.getLogger(RequestHandler::class.java)
    }
}
