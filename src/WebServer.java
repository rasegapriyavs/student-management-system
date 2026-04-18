import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.GraduateStudent;
import model.Student;
import model.UndergraduateStudent;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class WebServer {
    private final StudentManagementSystem service;
    private final int port;
    private HttpServer server;

    public WebServer(StudentManagementSystem service, int port) {
        this.service = service;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleRequest);
        server.setExecutor(null);
        server.start();
        System.out.println("Web server started at http://localhost:" + port + "/");
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) {
            serveFile("web/index.html", exchange);
            return;
        }

        if (path.equals("/app.js") || path.equals("/styles.css")) {
            serveFile("web" + path, exchange);
            return;
        }

        if (path.startsWith("/api/students")) {
            handleApi(exchange);
            return;
        }

        sendNotFound(exchange, "Resource not found");
    }

    private void handleApi(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath().replaceAll("/+$", "");

        if (path.equals("/api/students")) {
            if (method.equalsIgnoreCase("GET")) {
                listStudents(exchange);
            } else if (method.equalsIgnoreCase("POST")) {
                createStudent(exchange);
            } else {
                sendMethodNotAllowed(exchange);
            }
            return;
        }

        if (path.matches("/api/students/\\d+")) {
            int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
            if (method.equalsIgnoreCase("GET")) {
                getStudent(exchange, id);
            } else if (method.equalsIgnoreCase("PUT")) {
                updateStudent(exchange, id);
            } else if (method.equalsIgnoreCase("DELETE")) {
                deleteStudent(exchange, id);
            } else {
                sendMethodNotAllowed(exchange);
            }
            return;
        }

        sendNotFound(exchange, "API endpoint not found");
    }

    private void listStudents(HttpExchange exchange) throws IOException {
        List<Student> students = service.getStudents();
        String body = students.stream()
                .map(Student::toJson)
                .collect(Collectors.joining(",", "[", "]"));
        sendJson(exchange, 200, body);
    }

    private void getStudent(HttpExchange exchange, int id) throws IOException {
        Student student = service.findStudentById(id);
        if (student == null) {
            sendNotFound(exchange, "Student not found");
            return;
        }
        sendJson(exchange, 200, student.toJson());
    }

    private void createStudent(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Student student = Student.fromJson(body);
            if (!service.addStudent(student)) {
                sendResponse(exchange, 409, "A student with that ID already exists.", "text/plain");
                return;
            }
            sendJson(exchange, 201, student.toJson());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid student data: " + e.getMessage(), "text/plain");
        }
    }

    private void updateStudent(HttpExchange exchange, int id) throws IOException {
        String body = readRequestBody(exchange);
        try {
            Student updated = Student.fromJson(body);
            updated = withId(updated, id);
            if (!service.replaceStudent(updated)) {
                sendNotFound(exchange, "Student not found");
                return;
            }
            sendJson(exchange, 200, updated.toJson());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid student data: " + e.getMessage(), "text/plain");
        }
    }

    private void deleteStudent(HttpExchange exchange, int id) throws IOException {
        if (!service.deleteStudentById(id)) {
            sendNotFound(exchange, "Student not found");
            return;
        }
        sendResponse(exchange, 204, "", "text/plain");
    }

    private Student withId(Student student, int id) {
        if (student instanceof UndergraduateStudent undergraduate) {
            return new UndergraduateStudent(id, undergraduate.getName(), undergraduate.getMarks(),
                    undergraduate.getMajor());
        }
        if (student instanceof GraduateStudent graduate) {
            return new GraduateStudent(id, graduate.getName(), graduate.getMarks(), graduate.getThesisTitle());
        }
        return new Student(id, student.getName(), student.getMarks());
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        byte[] bytes = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int readBytes;
        var is = exchange.getRequestBody();
        while ((readBytes = is.read(bytes)) != -1) {
            sb.append(new String(bytes, 0, readBytes, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private void serveFile(String relativePath, HttpExchange exchange) throws IOException {
        File file = locateFile(relativePath);
        if (!file.exists() || !file.isFile()) {
            sendNotFound(exchange, "File not found");
            return;
        }

        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        sendResponse(exchange, 200, bytes, contentType);
    }

    private File locateFile(String relativePath) {
        File file = new File(System.getProperty("user.dir"), relativePath);
        if (file.exists()) {
            return file;
        }
        return new File(System.getProperty("user.dir") + File.separator + "..", relativePath);
    }

    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        sendResponse(exchange, status, body.getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
    }

    private void sendResponse(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        sendResponse(exchange, status, body.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private void sendResponse(HttpExchange exchange, int status, byte[] body, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, 404, message, "text/plain; charset=utf-8");
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
    }
}
