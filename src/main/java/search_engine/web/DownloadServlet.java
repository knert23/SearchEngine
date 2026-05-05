package search_engine.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathParam = req.getParameter("path");
        if (pathParam == null || pathParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Путь к файлу не указан");
            return;
        }

        Path filePath = Path.of(pathParam).normalize();
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Файл не найден");
            return;
        }

        String fileName = filePath.getFileName().toString();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        resp.setContentType("text/plain;charset=UTF-8");
        // RFC 5987 filename* для UTF-8, чтобы кириллица  отображалась корректно
        resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        resp.setContentLengthLong(Files.size(filePath));

        Files.copy(filePath, resp.getOutputStream());
        resp.getOutputStream().flush();
    }
}
