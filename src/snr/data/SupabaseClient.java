package snr.data;

import snr.model.HelpdeskException;
import snr.util.Json;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Talks to the Supabase REST API (PostgREST). Each table is at <url>/rest/v1/<table>.
public class SupabaseClient {
    private static final String CONFIG_FILE = "config.properties";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;
    private final String key;

    public SupabaseClient() throws HelpdeskException {
        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            config.load(in);
        } catch (IOException e) {
            throw new HelpdeskException("Missing " + CONFIG_FILE + " in the project folder.\n"
                    + "Add supabase.url and supabase.key (see README).");
        }
        String url = config.getProperty("supabase.url", "").trim();
        key = config.getProperty("supabase.key", "").trim();
        if (url.isEmpty() || key.isEmpty()) throw new HelpdeskException("supabase.url or supabase.key is empty.");
        baseUrl = (url.endsWith("/") ? url.substring(0, url.length() - 1) : url) + "/rest/v1/";
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> select(String table, String query) throws HelpdeskException {
        return (List<Map<String, Object>>) send(request(table + "?" + query).GET());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> insert(String table, Map<String, Object> row) throws HelpdeskException {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) send(request(table)
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(Json.write(row))));
        return rows.get(0);
    }

    public void update(String table, String column, Object value, Map<String, Object> changes) throws HelpdeskException {
        String filter = column + "=eq." + URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
        send(request(table + "?" + filter)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(Json.write(changes))));
    }

    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("apikey", key)
                .header("Content-Type", "application/json");
        // Legacy anon keys are JWTs and also go in Authorization; new publishable keys do not.
        if (key.startsWith("eyJ")) b.header("Authorization", "Bearer " + key);
        return b;
    }

    private Object send(HttpRequest.Builder builder) throws HelpdeskException {
        try {
            HttpResponse<String> res = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 300) {
                throw new HelpdeskException("Database error (" + res.statusCode() + "): " + res.body());
            }
            return res.body().isBlank() ? null : Json.parse(res.body());
        } catch (IOException e) {
            throw new HelpdeskException("Cannot reach Supabase. Check your internet connection.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HelpdeskException("Request was interrupted.");
        }
    }
}
