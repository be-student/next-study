package http.webserver

import config.ControllerFactory
import http.controller.Controller
import http.request.Request
import http.request.RequestParser
import http.response.Response
import http.util.IOUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.net.Socket
import java.nio.file.Files

class RequestHandler(
    private val connection: Socket,
    private val controllers: List<Controller> = ControllerFactory.getAllControllers()
) : Thread() {
    override fun run() {
        try {
            connection.getInputStream().use { `in` ->
                connection.getOutputStream().use { out ->
                    val request = processRequest(`in`)
                    val response = if (request == null) Response.badRequest() else process(request)
                    handleResponse(out, response)
                }
            }
        } catch (e: IOException) {
            log.error(e.message)
        }
    }

    private fun handleResponse(out: OutputStream?, response: Response) {
        val dos = DataOutputStream(out)
        val title = "HTTP/1.1 ${response.statusCode.httpCode} OK \r\n"
        val headers = response.headers.map { (key, value) -> "$key: $value\r\n" }
        val body = response.body
        val contentLength = "Content-Length: ${body.size}\r\n"
        val cookies = response.cookies.map { cookie -> "Set-Cookie: ${cookie.name}=${cookie.value}\r\n" }
        val cookie = cookies.joinToString("")
        val header = title + headers.joinToString("") + contentLength + cookie + "\r\n"
        dos.writeBytes(header)
        dos.write(body)
        dos.flush()
    }

    private fun process(request: Request): Response {
        val file = File("./webapp${request.path}")
        if (file.exists() && file.isFile()) {
            return handleFile(file, request)
        }
        return controllers.find { it.supports(request) }
            ?.handle(request)
            ?: Response.badRequest()
    }

    private fun handleFile(file: File, request: Request): Response {
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
        log.info("fileName : {}", file.name)
        return Response.ok(responseBody)
    }

    private fun processRequest(`in`: InputStream): Request? {
        val br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
        val requestLines = MutableList(0) { "" }
        while (true) {
            val line = br.readLine()
            if (line == null || line == "") break
            requestLines.add(line)
        }
        if (requestLines.isEmpty()) {
            return null
        }
        val header = RequestParser.parseHeaders(requestLines.drop(1))
        val body = IOUtils.readData(br, header["Content-Length"]?.toInt() ?: 0)
        return RequestParser.parseRequest(requestLines, body)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RequestHandler::class.java)
    }
}
