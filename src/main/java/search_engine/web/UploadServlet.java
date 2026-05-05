package search_engine.web;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import search_engine.core.WordIndexer;
import search_engine.dao.WordIndexDao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

  private final WordIndexer indexer = new WordIndexer();
  private final WordIndexDao dao = new WordIndexDao();

  // Директория для сохранения загруженных файлов
  private static final String UPLOAD_DIR = "uploaded_files";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html;charset=UTF-8");

    try {
      Part filePart = req.getPart("file");
      String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

      // Проверка расширения
      if (!fileName.toLowerCase().endsWith(".txt")) {
        sendError(resp, "Разрешены только .txt файлы");
        return;
      }

      // Создание директории для загрузки, если ее нет
      String uploadPath = System.getProperty("user.dir") + "/" + UPLOAD_DIR;
      Path uploadDir = Paths.get(uploadPath);
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      // Сохранение файла
      Path filePath = uploadDir.resolve(fileName);
      Files.copy(filePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      // Индексация файла
      Map<String, Integer> wordCounts = indexer.indexFile(filePath);

      // Сохраннение в БД
      Map<Path, Map<String, Integer>> perDocIndex = new HashMap<>();
      perDocIndex.put(filePath, wordCounts);
      dao.saveIndex(perDocIndex);

      // Перенаправление обратно на страницу поиска
      resp.sendRedirect("/search?uploaded=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8));

    } catch (Exception e) {
      sendError(resp,  e.getMessage());
    }
  }

  private void sendError(HttpServletResponse resp, String message) throws IOException {
    resp.setContentType("text/html;charset=UTF-8");
    resp.getWriter().println("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
    resp.getWriter().println("<h2>" + message + "</h2>");
    resp.getWriter().println("<a href='/search'>Вернуться к поиску</a>");
    resp.getWriter().println("</body></html>");
  }
}