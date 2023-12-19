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
import java.util.Collection;
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
            if (headers.get("Content-Length") != null && headers.get("Content-Type") != null && "application/x-www-form-urlencoded".equals(headers.get("Content-Type"))) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                log.info("body : {}", body);
                String[] tokens = body.split("&");
                requestBody = Arrays.stream(tokens).map(token -> token.split("="))
                        .collect(Collectors.toMap(token -> token[0], token -> token[1]));
                log.info("requestBody : {}", requestBody);

            }

            File file = new File("./webapp" + url);
            DataOutputStream dos = new DataOutputStream(out);
            if (file.exists() && file.isFile()) {
                if (url.endsWith(".css")) {
                    byte[] body = Files.readAllBytes(file.toPath());
                    response200CssHeader(dos, body.length);
                    responseBody(dos, body);
                    end(dos);
                    return;
                }
                byte[] body = Files.readAllBytes(file.toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
                end(dos);
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
                response302Header(dos, 0, "/index.html");
                end(dos);
                return;
            }
            if (requestPath.equals("/user/login") && method.equals("POST")) {
                User user = DataBase.findUserById(requestBody.get("userId"));
                if (user == null) {
                    response302Header(dos, 0, "/user/login_failed.html");
                    responseCookie(dos, "logined=false");
                    end(dos);
                    return;
                }
                if (user.getPassword().equals(requestBody.get("password"))) {
                    response302Header(dos, 0, "/index.html");
                    responseCookie(dos, "logined=true");
                    end(dos);
                    return;
                }
                response302Header(dos, 0, "/user/login_failed.html");
                responseCookie(dos, "logined=false");
                return;
            } else if (requestPath.equals("/user/list")) {
                if (headers.get("Cookie") == null || !headers.get("Cookie").contains("logined=true")) {
                    response302Header(dos, 0, "/user/login.html");
                    end(dos);
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                users.forEach(user -> sb.append(user.toString()).append("\n"));
                byte[] body = sb.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
                end(dos);
            }
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
            end(dos);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseCookie(DataOutputStream dos, String s) {
        try {
            dos.writeBytes("Set-Cookie: " + s + " \r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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

    private void end(DataOutputStream dos) {
        try {
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
