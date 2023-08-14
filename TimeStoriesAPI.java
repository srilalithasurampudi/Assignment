import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TimeStoriesAPI {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/getTimeStories", new TimeStoriesHandler());
        server.setExecutor(null);
        server.start();
    }

    static class TimeStoriesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sourceURL = "https://time.com";
            List<List<String>> latestStories = getLatestStories(sourceURL, 6);
            String response = buildJSONResponse(latestStories);

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    public static List<List<String>> getLatestStories(String sourceURL, int numStories) {
        List<List<String>> stories = new ArrayList<>();

        try {
            URL url = new URL(sourceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream responseStream = connection.getInputStream();
            Scanner scanner = new Scanner(responseStream);

            int i = 0;
            String link = "";
            while (scanner.hasNextLine() && stories.size() < numStories) {
                String line = scanner.nextLine();
                int startIndex = 0, endIndex = 0;
                if (line.contains("<a href=")){
                    startIndex = line.indexOf("<a href=") + 7;
                    endIndex = line.indexOf(">");
                }

                if (startIndex > 0 && startIndex < endIndex)
                    link = line.substring(startIndex, endIndex);
                

                if (line.contains("class=\"latest-stories__item-headline\"")) {
                    
                    String title = extractTitle(line);
                    ArrayList <String> arr = new ArrayList<>();
                    arr.add(title);
                    arr.add(link);
                    stories.add(arr);
                }
            }

            scanner.close();
            responseStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stories;
    }

    public static String extractTitle(String line) {
        int startIndex = line.indexOf("class=\"latest-stories__item-headline\">") + 38;
        int endIndex = line.indexOf("</h3>");
        return line.substring(startIndex, endIndex);
    }

    public static String buildJSONResponse(List<List<String>> stories) {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < stories.size(); i++) {
            if (i > 0) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("{\"title\": \"").append(stories.get(i).get(0)).append("\"}");
            jsonBuilder.append("{\"link\": \"").append(stories.get(i).get(1)).append("\"}");
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }
}
