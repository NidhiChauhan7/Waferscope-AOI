import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Handles HTTP communication between the Java application and the Python/Flask ML backend.
 *
 * ARCHITECTURAL ROLE:
 * Java (OOP Application Client) -> HTTP GET/POST -> Flask API -> PyTorch CNN Inference
 */
public class FlaskClient {

    private static final String API_URL = "http://127.0.0.1:5000/";

    public void checkBackendStatus() {
        System.out.println("\n----- FLASK BACKEND CONNECTIVITY TEST -----");
        System.out.println("Connecting to API endpoint: " + API_URL);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("Status Code : " + response.statusCode());
            System.out.println("Response    : " + response.body());
        } catch (Exception ex) {
            System.out.println("Notice: Flask backend is currently offline (" + ex.getMessage() + ").");
            System.out.println("Architecture Note: Java connects to Flask via standard java.net.http.HttpClient.");
        }
    }
}

