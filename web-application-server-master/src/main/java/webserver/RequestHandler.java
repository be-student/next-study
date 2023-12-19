package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;

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
import java.util.HashMap;
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
            String method = null;
            Map<String, String> headers = new HashMap<>();
            while ((line = br.readLine()) != null && !"".equals(line)) {
                log.info("info log의 line : {}", line);
                String[] tokens = line.split(" ");
                if ("GET".equals(tokens[0])) {
                    url = tokens[1];
                    method = tokens[0];
                }
                if ("POST".equals(tokens[0])) {
                    url = tokens[1];
                    method = tokens[0];
                }
                if (tokens[0].contains(":")) {
                    String key = tokens[0].split(":")[0];
                    String value = tokens[1].trim();
                    headers.put(key, value);
                }
            }
            log.info("method : {}", method);
            Map<String, String> requestBody = new HashMap<>();
            if ("POST".equals(method)) {
                String contentLength = headers.get("Content-Length");
                String contentType = headers.get("Content-Type");
                if (contentLength != null && contentType != null && "application/x-www-form-urlencoded".equals(contentType)) {
                    String body = IOUtils.readData(br, Integer.parseInt(contentLength));
                    log.info("body : {}", body);
                    String[] tokens = body.split("&");
                    requestBody = Arrays.stream(tokens).map(token -> token.split("="))
                            .collect(Collectors.toMap(token -> token[0], token -> token[1]));
                    log.info("requestBody : {}", requestBody);
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
            String requestPath = url;
            if (index != -1) {
                requestPath = url.substring(0, index);
            }
            Map<String, String> query = new HashMap<>();
            if (index != -1) {
                String params = url.substring(index + 1);
                String[] tokens = params.split("&");
                query = Arrays.stream(tokens).map(token -> token.split("="))
                        .collect(Collectors.toMap(token -> token[0], token -> token[1]));
            }
            if (requestPath.equals("/user/create") && method.equals("POST")) {
                User user = new User(requestBody.get("userId"), requestBody.get("password"), requestBody.get("name"), requestBody.get("email"));
                log.info("user : {}", user);
                DataBase.addUser(user);
                response302Header(new DataOutputStream(out), 0, "/web-application-server-master/index.html");
                return;
            }
            if (requestPath.equals("/user/login") && method.equals("POST")) {
                User user = DataBase.findUserById(requestBody.get("userId"));
                if (user == null) {
                    response302Header(new DataOutputStream(out), 0, "/user/login_failed.html");
                    return;
                }
                if (user.getPassword().equals(requestBody.get("password"))) {
                    response302Header(new DataOutputStream(out), 0, "/index.html");
                    return;
                }
                response302Header(new DataOutputStream(out), 0, "/user/login_failed.html");
                return;
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

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
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
