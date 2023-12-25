package webserver

import db.DataBase
import http.code.StatusCode
import http.cookie.Cookie
import http.response.Response
import model.User
import org.slf4j.LoggerFactory
import util.IOUtils
import java.io.*
import java.net.Socket
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

class RequestHandler(private val connection: Socket) : Thread() {
    override fun run() {
        log.debug(
            "New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
            connection.getPort()
        )
        try {
            connection.getInputStream().use { `in` ->
                connection.getOutputStream().use { out ->
                    val response = 저지른_코드(`in`, out)

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
                    log.info("response : {}", response)
                }
            }
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun 저지른_코드(`in`: InputStream, out: OutputStream): Response {
        val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))

        // 클라이언트의 request정보
        var url = ""
        var line: String?
        var method: String? = null
        val headers: MutableMap<String, String?> =
            HashMap()
        while (br.readLine().also { line = it } != null && "" != line) {
            log.info("info log의 line : {}", line)
            val tokens =
                line!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if ("GET" == tokens[0]) {
                url = tokens[1]
                method = tokens[0]
            }
            if ("POST" == tokens[0]) {
                url = tokens[1]
                method = tokens[0]
            }
            if (tokens[0].contains(":")) {
                val key =
                    tokens[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val value = tokens[1].trim { it <= ' ' }
                headers[key] = value
            }
        }
        log.info("method : {}", method)
        var requestBody: Map<String?, String> =
            HashMap()
        if (headers["Content-Length"] != null && headers["Content-Type"] != null && "application/x-www-form-urlencoded" == headers["Content-Type"]
        ) {
            val body = IOUtils.readData(br, headers["Content-Length"]!!.toInt())
            log.info("body : {}", body)
            val tokens =
                body.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            requestBody = Arrays.stream(tokens)
                .map { token: String ->
                    token.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                }
                .collect(
                    Collectors.toMap(
                        { token: Array<String> ->
                            token[0]
                        },
                        { token: Array<String> ->
                            token[1]
                        })
                )
            log.info("requestBody : {}", requestBody)
        }
        val file = File("./webapp$url")
        if (file.exists() && file.isFile()) {
            if (url.endsWith(".css")) {
                val body = Files.readAllBytes(file.toPath())
                val headers = mapOf(
                    "Content-Type" to "text/css",
                )
                return Response.ok(body, headers)
            }
            val body = Files.readAllBytes(file.toPath())
            val headers = mapOf(
                "Content-Type" to "text/html;charset=utf-8",
            )
            return Response.ok(body, headers)
        }
        val index = url.indexOf("?")
        var requestPath = url
        if (index != -1) {
            requestPath = url.substring(0, index)
        }
        var query: Map<String?, String?>? =
            HashMap()
        if (index != -1) {
            val params = url.substring(index + 1)
            val tokens =
                params.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            query = Arrays.stream(tokens)
                .map { token: String ->
                    token.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                }
                .collect(
                    Collectors.toMap(
                        Function { token: Array<String> ->
                            token[0]
                        },
                        Function { token: Array<String> ->
                            token[1]
                        })
                )
        }
        if (requestPath == "/user/create" && method == "POST") {
            val user = User(
                requestBody["userId"]!!,
                requestBody["password"]!!,
                requestBody["name"]!!,
                requestBody["email"]!!
            )
            log.info("user : {}", user)
            DataBase.addUser(user)
            return Response.redirect("/index.html")
        }
        if (requestPath == "/user/login" && method == "POST") {
            val user = DataBase.findUserById(requestBody["userId"]!!)
                ?: return loginFail()
            if (user.password == requestBody["password"]) {
                return Response.redirect("/index.html")
            }
            return loginFail()
        } else if (requestPath == "/user/list") {
            if (headers["Cookie"] == null || !headers["Cookie"]!!.contains("logined=true")) {
                return loginFail()
            }
            val users = DataBase.findAll()
            val sb = StringBuilder()
            users.forEach(Consumer { user: User ->
                sb.append(user.toString()).append("\n")
            })
            val body = sb.toString()
            return Response.ok(body)
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
