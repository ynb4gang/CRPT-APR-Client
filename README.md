# CRPT API Client

This repository contains the implementation of a client class for working with the CRPT (Chestny ZNAK) API. The class is thread-safe and supports request rate limiting.

## Features

- Supports request rate limiting within a specified time interval.
- Thread-safe.
- Easy to extend with additional functionality.

## Installation

To work with the project, you need to have JDK 17 installed and clone the repository:

git clone https://github.com/ynb4gang/CRPT-APR-Client.git
cd crpt-api-client-java
## Usage

Example of using the CrptApi class to create a document:
```sh
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);
        CrptApi.Document document = new CrptApi.Document();
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
        document.description = new CrptApi.Document.Description();
        document.description.participantInn = "1234567890";
        CrptApi.Document.Product product = new CrptApi.Document.Product();
        product.certificate_document = "CERT_DOC";
        product.certificate_document_date = "2020-01-23";
        product.certificate_document_number = "CERT_NUM";
        product.owner_inn = "1234567890";
        product.producer_inn = "1122334455";
        product.production_date = "2020-01-23";
        product.tnved_code = "CODE";
        product.uit_code = "UIT_CODE";
        product.uitu_code = "UITU_CODE";
        document.products = new CrptApi.Document.Product[]{product};
        
        try {
            HttpResponse<String> response = api.createDocument(document, "signature_string");
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
## Dependencies

To work with the project, you need to add the following dependencies:

- HttpClient for sending HTTP requests.
- Jackson for JSON serialization and deserialization.

You can add them to your pom.xml (for Maven) or build.gradle (for Gradle) file.

### Example pom.xml for Maven:
```sh
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.13.3</version>
    </dependency>
</dependencies>
```
### Example build.gradle for Gradle:
```sh
dependencies {
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.3'
}
```
