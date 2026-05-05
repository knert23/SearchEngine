# SearchEngine

Локальный Java search engine для индексирования текста из `.txt` файлов, сохранения в SQLite и поиска по словам через веб-интерфейс.

## Обзор

Проект включает:

- параллельный индексатор текста (`ParallelDocumentIndexer`) для `.txt` файлов
- анализатор слов и подсчет частот (`WordIndexer`)
- сохранение индекса в SQLite через Hibernate/JPA (`WordIndexDao`)
- встроенный веб-сервер на Apache Tomcat для поиска, загрузки и скачивания файлов
- минимальный веб-интерфейс на HTML/CSS

## Структура

- `src/main/java/search_engine/core` — индексатор и CLI-стартер
- `src/main/java/search_engine/dao` — DAO, поисковый результат
- `src/main/java/search_engine/entities` — JPA-сущности для документов, слов и вхождений
- `src/main/java/search_engine/web` — веб-сервлеты и встроенный Tomcat
- `src/main/resources/META-INF/persistence.xml` — конфигурация Hibernate + SQLite

## Требования

- Java 25
- Maven

## Сборка

```bash
mvn compile
```

## CLI: индексирование директории

Чтобы проиндексировать все `.txt` файлы в директории и сохранить результат в SQLite, используйте класс `search_engine.core.Runner`.

Пример запуска из IDE:

- `search_engine.core.Runner` с аргументом: путь к директории

Пример в терминале (если настроена среда Java/Maven):

```bash
mvn compile exec:java -Dexec.mainClass=search_engine.core.Runner -Dexec.args="/path/to/documents"
```

> После успешного выполнения создается/обновляется база `search-index.db`.

## Запуск веб-интерфейса

Веб-приложение создается классом `search_engine.web.WebApp` и запускает Tomcat на `http://localhost:8080/search`.

### Основные возможности

- поиск по слову в индексированных документах
- вывод списка найденных файлов с частотой слова
- загрузка `.txt` файлов через форму и автоматическая индексация
- скачивание исходного файла по ссылке

### Как использовать

1. Запустите `search_engine.web.WebApp`.
2. Откройте `http://localhost:8080/search`.
3. Введите слово и нажмите `Enter`.
4. При необходимости загрузите новый `.txt` файл для индексации.

## База данных

Конфигурация в `src/main/resources/META-INF/persistence.xml`:

- SQLite-файл: `./search-index.db`
- Hibernate dialect: `org.hibernate.community.dialect.SQLiteDialect`
- схема обновляется автоматически (`hibernate.hbm2ddl.auto=update`)

## Особенности реализации

- `WordIndexer` разбивает текст на отдельные слова (отсекает пробелы, запятые, табляцию и т.д.) и считает вхождения слов без учета регистра
- `ParallelDocumentIndexer` использует пул потоков для параллельной обработки файлов
- `WordIndexDao` сохраняет документы, слова и связи через сущности `Document`, `Word`, `WordOccurrence`
- `SearchServlet` рендерит страницу поиска и результаты на HTML
- `UploadServlet` сохраняет загруженные файлы в папку `uploaded_files`
- `DownloadServlet` отдает файл как attachment с корректным UTF-8 именем

## Примечания

- Убедитесь, что директория с файлами содержит `.txt` файлы
- При запуске `WebApp` проверьте, что порт `8080` свободен
- Если запуск из консоли вызывает проблемы, используйте IDE для старта основных классов
