package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        connection = connectionSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            // 클라이언트의 request정보
            String url = "";
            String line = null;
            while ((line = br.readLine()) != null && !"".equals(line)) {
                log.info("info log의 line : {}", line);
                String[] tokens = line.split(" ");
                if ("GET".equals(tokens[0])) {
                    url = tokens[1];
                    break;
                }
            }
            File file = new File("./web-application-server-master/webapp" + url);
            if (file.exists() && file.isFile()) {
                byte[] body = Files.readAllBytes(file.toPath());
                System.out.println(body.length);
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            int index = url.indexOf("?");
            String requestPath = url.substring(0, index);
            String params = url.substring(index + 1);
            String[] tokens = params.split("&");
            Map<String, String> query = Arrays.stream(tokens).map(token -> token.split("="))
                    .collect(Collectors.toMap(token -> token[0], token -> token[1]));
            if (requestPath.equals("/user/create")) {
                User user = new User(query.get("userId"), query.get("password"), query.get("name"),
                        query.get("email"));
                log.info("user : {}", user);
            }
            byte[] body = "Hello World".getBytes();
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
