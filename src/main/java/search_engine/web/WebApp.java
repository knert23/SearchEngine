package search_engine.web;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import java.io.File;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.MultipartConfigElement;

public class WebApp {
    static void main() {
        try {
            Tomcat tomcat = new Tomcat();

            // Создание HTTP-коннектора
            Connector connector = new Connector(Http11NioProtocol.class.getName());
            connector.setPort(8080);
            tomcat.getService().addConnector(connector);
            tomcat.setConnector(connector);

            // Создание рабочей директории для Tomcat
            String tempDir = System.getProperty("java.io.tmpdir");
            File docBase = new File(tempDir, "search-app");
            if (!docBase.exists()) {
                docBase.mkdirs();
            }

            // Контекст приложения - точка входа для сервлетов
            Context context = tomcat.addContext("", docBase.getAbsolutePath());
            context.setReloadable(false);

            // Регистрация сервлета поиска
            Wrapper searchWrapper = context.createWrapper();
            searchWrapper.setName("search");
            searchWrapper.setServlet(new SearchServlet());
            context.addChild(searchWrapper);
            context.addServletMappingDecoded("/search", "search");

            // Регистрация сервлета скачивания
            Wrapper downloadWrapper = context.createWrapper();
            downloadWrapper.setName("download");
            downloadWrapper.setServlet(new DownloadServlet());
            context.addChild(downloadWrapper);
            context.addServletMappingDecoded("/download", "download");

            // Регистрация сервлета загрузки с multipart конфигурацией
            Wrapper uploadWrapper = context.createWrapper();
            uploadWrapper.setName("upload");
            uploadWrapper.setServlet(new UploadServlet());

            // Настройка multipart
            MultipartConfigElement multipartConfig = new MultipartConfigElement(
                    docBase.getAbsolutePath(),
                    1024 * 1024 * 10,    // 10 MB max file size
                    1024 * 1024 * 50,    // 50 MB max request size
                    1024 * 1024          // 1 MB file size threshold
            );
            uploadWrapper.setMultipartConfigElement(multipartConfig);

            context.addChild(uploadWrapper);
            context.addServletMappingDecoded("/upload", "upload");

            // Корневой сервлет
            Wrapper rootWrapper = context.createWrapper();
            rootWrapper.setName("root");
            rootWrapper.setServlet(new HttpServlet() {
                @Override
                protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                        throws IOException {
                    resp.setContentType("text/html;charset=UTF-8");
                    resp.getWriter().println(
                            "<html><body><h2>Search Engine is running</h2>" +
                                    "<a href='/search'>Go to search</a></body></html>");
                }
            });
            context.addChild(rootWrapper);
            context.addServletMappingDecoded("/", "root");

            tomcat.start();
            System.out.println("Сервер работает: http://localhost:8080/search");
            tomcat.getServer().await();

        } catch (Exception e) {
            System.err.println("Не удалось запустить сервер");
            System.err.println(e.getMessage());
        }
    }
}