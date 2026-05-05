package search_engine.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import search_engine.dao.SearchResult;
import search_engine.dao.WordIndexDao;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    private final WordIndexDao dao = new WordIndexDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String word = req.getParameter("word");

        try (PrintWriter out = resp.getWriter()) {
            // Страница поиска
            out.println("<!DOCTYPE html>");
            out.println("<html lang='ru'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Поиск по индексированным файлам</title>");
            out.println("<style>");
            out.println("* { margin: 0; padding: 0; box-sizing: border-box; }");
            out.println("body { font-family: Arial, sans-serif; min-height: 100vh; background: #fff; }");
            out.println(".page-wrapper { display: flex; flex-direction: column; align-items: center; padding-top: 25vh; padding-bottom: 40px; }");
            out.println("h1 { text-align: center; margin-bottom: 30px; color: #202124; font-size: 28px; font-weight: 400; }");
            out.println(".search-container { width: 100%; max-width: 584px; margin-bottom: 40px; }");
            out.println(".search-box { display: flex; align-items: center; border: 1px solid #dfe1e5; border-radius: 24px; padding: 10px 20px; background: #fff; transition: box-shadow 0.3s, border-color 0.3s; }");
            out.println(".search-box:hover, .search-box:focus-within { box-shadow: 0 1px 6px rgba(32,33,36,0.28); border-color: rgba(223,225,229,0); }");
            out.println(".search-icon { width: 20px; height: 20px; margin-right: 12px; fill: #9aa0a6; flex-shrink: 0; }");
            out.println(".search-input { flex: 1; border: none; outline: none; font-size: 16px; color: #202124; background: transparent; }");
            out.println(".search-input::placeholder { color: #9aa0a6; }");
            out.println(".results { width: 100%; max-width: 584px; }");
            out.println(".results-table { width: 100%; border-collapse: collapse; }");
            out.println(".results-table th { background: #f8f9fa; padding: 12px 16px; text-align: left; border-bottom: 1px solid #dadce0; font-weight: 600; color: #202124; }");
            out.println(".results-table td { padding: 12px 16px; border-bottom: 1px solid #f1f3f4; color: #202124; }");
            out.println(".results-table tr:hover { background: #f8f9fa; }");
            out.println(".file-link { color: #1a0dab; text-decoration: none; font-size: 16px; }");
            out.println(".file-link:hover { text-decoration: underline; }");
            out.println(".no-results { text-align: center; color: #5f6368; margin-top: 20px; font-size: 16px; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='page-wrapper'>");

            out.println("<h1>Поиск по индексированным файлам</h1>");

            out.println("<form class='search-container' method='get' action='/search' id='searchForm'>");
            out.println("<div class='search-box'>");
            out.println("<svg class='search-icon' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'>");
            out.println("<path d='M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z'/>");
            out.println("</svg>");
            out.printf("<input type='text' class='search-input' name='word' placeholder='Введите слово для поиска' value='%s' autofocus id='searchInput'>",
                    word != null ? escapeHtml(word) : "");
            out.println("</div>");
            out.println("</form>");

            // Кнопка для показа/скрытия формы загрузки
            out.println("<div style='text-align: center; margin: 20px 0;'>");
            out.println("<button onclick='document.getElementById(\"uploadForm\").style.display=\"block\"' ");
            out.println("style='background: #4285f4; color: white; border: none; padding: 10px 20px; ");
            out.println("border-radius: 4px; cursor: pointer; font-size: 14px;'>");
            out.println("Загрузить файл для индексации</button>");
            out.println("</div>");

            // Форма загрузки для пользовательского файла
            out.println("<div id='uploadForm' style='display: none; max-width: 584px; margin: 0 auto 40px; ");
            out.println("padding: 30px; border: 2px dashed #dadce0; border-radius: 8px; text-align: center;'>");
            out.println("<form method='post' action='/upload' enctype='multipart/form-data'>");
            out.println("<p style='color: #5f6368; margin-bottom: 20px;'>");
            out.println("Загрузите текстовый файл (.txt) для индексации и поиска</p>");
            out.println("<input type='file' name='file' accept='.txt' required ");
            out.println("style='margin-bottom: 15px; font-size: 14px;'>");
            out.println("<br>");
            out.println("<button type='submit' ");
            out.println("style='background: #34a853; color: white; border: none; padding: 10px 24px; ");
            out.println("border-radius: 4px; cursor: pointer; font-size: 14px; margin-right: 10px;'>");
            out.println("Загрузить</button>");
            out.println("<button type='button' onclick='document.getElementById(\"uploadForm\").style.display=\"none\"' ");
            out.println("style='background: #f1f3f4; color: #3c4043; border: none; padding: 10px 24px; ");
            out.println("border-radius: 4px; cursor: pointer; font-size: 14px;'>");
            out.println("Отмена</button>");
            out.println("</form>");
            out.println("</div>");

            // Сообщение об успешной загрузке
            String uploadedFile = req.getParameter("uploaded");
            if (uploadedFile != null) {
                out.println("<div style='max-width: 584px; margin: 20px auto; padding: 15px; ");
                out.println("background: #d4edda; border: 1px solid #c3e6cb; border-radius: 4px; ");
                out.println("text-align: center; color: #155724;'>");
                out.println("Файл <strong>" + escapeHtml(uploadedFile) + "</strong> загружен и проиндексирован!");
                out.println("</div>");
            }

            if (word != null && !word.trim().isEmpty()) {
                List<SearchResult> results = dao.search(word.trim());
                if (results.isEmpty()) {
                    out.println("<div class='no-results'>");
                    out.println("<p>Ничего не найдено по запросу: <strong>" + escapeHtml(word) + "</strong></p>");
                    out.println("</div>");
                } else {
                    out.println("<div class='results'>");
                    out.println("<table class='results-table'>");
                    out.println("<thead><tr><th>Файл</th><th>Частота</th></tr></thead>");
                    out.println("<tbody>");
                    for (SearchResult r : results) {
                        String encodedPath = URLEncoder.encode(r.filePath(), StandardCharsets.UTF_8);
                        out.printf("<tr><td><a href='/download?path=%s' class='file-link'>%s</a></td><td>%d</td></tr>%n",
                                encodedPath, escapeHtml(r.fileName()), r.count());
                    }
                    out.println("</tbody></table></div>");
                }
            }

            out.println("</div>");
            out.println("<script>");
            out.println("document.getElementById('searchInput').addEventListener('keypress', function(e) {");
            out.println("  if (e.key === 'Enter') {");
            out.println("    e.preventDefault();");
            out.println("    if (this.value.trim()) {");
            out.println("      document.getElementById('searchForm').submit();");
            out.println("    }");
            out.println("  }");
            out.println("});");
            out.println("</script>");
            out.println("</body></html>");
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}