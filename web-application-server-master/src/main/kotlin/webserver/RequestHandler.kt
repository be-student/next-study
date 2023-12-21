package webserver

import db.DataBase
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
                    // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
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
                    val dos = DataOutputStream(out)
                    if (file.exists() && file.isFile()) {
                        if (url.endsWith(".css")) {
                            val body = Files.readAllBytes(file.toPath())
                            response200CssHeader(dos, body.size)
                            responseBody(dos, body)
                            end(dos)
                            return
                        }
                        val body = Files.readAllBytes(file.toPath())
                        response200Header(dos, body.size)
                        responseBody(dos, body)
                        end(dos)
                        return
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
                        response302Header(dos, 0, "/index.html")
                        end(dos)
                        return
                    }
                    if (requestPath == "/user/login" && method == "POST") {
                        val user = DataBase.findUserById(requestBody["userId"]!!)
                        if (user == null) {
                            response302Header(dos, 0, "/user/login_failed.html")
                            responseCookie(dos, "logined=false")
                            end(dos)
                            return
                        }
                        if (user.password == requestBody["password"]) {
                            response302Header(dos, 0, "/index.html")
                            responseCookie(dos, "logined=true")
                            end(dos)
                            return
                        }
                        response302Header(dos, 0, "/user/login_failed.html")
                        responseCookie(dos, "logined=false")
                        return
                    } else if (requestPath == "/user/list") {
                        if (headers["Cookie"] == null || !headers["Cookie"]!!.contains("logined=true")) {
                            response302Header(dos, 0, "/user/login.html")
                            end(dos)
                            return
                        }
                        val users = DataBase.findAll()
                        val sb = StringBuilder()
                        users.forEach(Consumer { user: User ->
                            sb.append(user.toString()).append("\n")
                        })
                        val body = sb.toString().toByteArray()
                        response200Header(dos, body.size)
                        responseBody(dos, body)
                        end(dos)
                    }
                    val body = "Hello World".toByteArray()
                    response200Header(dos, body.size)
                    responseBody(dos, body)
                    end(dos)
                }
            }
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun responseCookie(dos: DataOutputStream, s: String) {
        try {
            dos.writeBytes("Set-Cookie: $s \r\n")
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun response200Header(dos: DataOutputStream, lengthOfBodyContent: Int) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n")
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n")
            dos.writeBytes("Content-Length: $lengthOfBodyContent\r\n")
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun response200CssHeader(dos: DataOutputStream, lengthOfBodyContent: Int) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n")
            dos.writeBytes("Content-Type: text/css\r\n")
            dos.writeBytes("Content-Length: $lengthOfBodyContent\r\n")
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun response302Header(dos: DataOutputStream, lengthOfBodyContent: Int, redirectUrl: String) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n")
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n")
            dos.writeBytes("Content-Length: $lengthOfBodyContent\r\n")
            dos.writeBytes("Location: $redirectUrl\r\n")
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun responseBody(dos: DataOutputStream, body: ByteArray) {
        try {
            dos.write(body, 0, body.size)
            dos.flush()
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun end(dos: DataOutputStream) {
        try {
            dos.writeBytes("\r\n")
            dos.flush()
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RequestHandler::class.java)
    }
}