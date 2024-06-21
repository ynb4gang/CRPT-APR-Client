import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int requestLimit;
    private final TimeUnit timeUnit;
    private final BlockingQueue<Instant> requestQueue;
    private final ReentrantLock lock = new ReentrantLock();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestQueue = new LinkedBlockingQueue<>(requestLimit);
    }

    public HttpResponse<String> createDocument(Document document, String signature) throws IOException, InterruptedException {
        Instant now = Instant.now();

        lock.lock();
        try {
            while (requestQueue.size() >= requestLimit) {
                Instant oldestRequest = requestQueue.peek();
                if (oldestRequest != null) {
                    Duration durationSinceOldestRequest = Duration.between(oldestRequest, now);
                    long millisToWait = timeUnit.toMillis(1) - durationSinceOldestRequest.toMillis();
                    if (millisToWait > 0) {
                        lock.unlock();
                        Thread.sleep(millisToWait);
                        lock.lock();
                    } else {
                        requestQueue.poll();
                    }
                }
            }
            requestQueue.add(now);
        } finally {
            lock.unlock();
        }

        String jsonBody = objectMapper.writeValueAsString(document);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);
        Document document = new Document();
        // Fill the Document object with data
        document.doc_id = "12345";
        document.doc_status = "NEW";
        document.doc_type = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.owner_inn = "1234567890";
        document.participant_inn = "0987654321";
        document.producer_inn = "1122334455";
        document.production_date = "2020-01-23";
        document.production_type = "TYPE";
        document.reg_date = "2020-01-23";
        document.reg_number = "REG12345";
        document.description = new Document.Description();
        document.description.participantInn = "1234567890";
        Document.Product product = new Document.Product();
        product.certificate_document = "CERT_DOC";
        product.certificate_document_date = "2020-01-23";
        product.certificate_document_number = "CERT_NUM";
        product.owner_inn = "1234567890";
        product.producer_inn = "1122334455";
        product.production_date = "2020-01-23";
        product.tnved_code = "CODE";
        product.uit_code = "UIT_CODE";
        product.uitu_code = "UITU_CODE";
        document.products = new Document.Product[]{product};

        try {
            HttpResponse<String> response = api.createDocument(document, "signature_string");
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}