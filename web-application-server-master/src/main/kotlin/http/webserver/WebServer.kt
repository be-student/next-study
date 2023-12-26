package http.webserver

import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.net.Socket

private val log = LoggerFactory.getLogger("WebServer")
private const val DEFAULT_PORT = 8080

fun main(args: Array<String>) {
    var port = 0
    port = if (args.isEmpty()) {
        DEFAULT_PORT
    } else {
        args[0].toInt()
    }
    ServerSocket(port).use { listenSocket ->
        log.info("Web Application Server started {} port.", port)

        // 클라이언트가 연결될때까지 대기한다.
        var connection: Socket?
        while (listenSocket.accept().also { connection = it } != null) {
            connection?.let { RequestHandler(it) }?.start()
        }
    }
}
