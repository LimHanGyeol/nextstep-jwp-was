package webserver.http.request;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static webserver.http.HttpHeader.ACCEPT;
import static webserver.http.HttpHeader.CONNECTION;

public class HttpRequestTest {
    private static final String TEST_RESOURCE_PATH = "./src/test/resources/%s";

    @Test
    @Disabled
    void request_resttemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("서버의 ThreadPool 보다 많은 수로 요청을 보내본다.")
    void request_concurrent_test() {
        RestTemplate restTemplate = new RestTemplate();
        int requestCount = 400;

        boolean result = IntStream.range(0, requestCount)
                .parallel()
                .mapToObj(index -> restTemplate.getForEntity("http://localhost:8080/index.html", String.class))
                .allMatch(response -> response.getStatusCode().is2xxSuccessful());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Get 요청을 파싱한다.")
    void request_GET() throws IOException {
        File file = new File(String.format(TEST_RESOURCE_PATH, "Http_GET.txt"));
        FileInputStream inputStream = new FileInputStream(file);

        HttpRequest actual = HttpRequestFactory.parse(inputStream);

        assertThat(actual.getRequestLine().getMethod().name()).isEqualTo("GET");
        assertThat(actual.getRequestLine().getPathValue()).isEqualTo("/user/create");
        assertThat(actual.getHttpHeaders().getHeader(CONNECTION)).isEqualTo("keep-alive");
        assertThat(actual.getHttpHeaders().getHeader(ACCEPT)).isEqualTo("*/*");
        assertThat(actual.getRequestLine().getParameter("userId")).isEqualTo("javajigi");
    }

    @Test
    @DisplayName("Post 요청을 파싱한다.")
    void request_POST() throws IOException {
        File file = new File(String.format(TEST_RESOURCE_PATH, "Http_POST.txt"));
        FileInputStream inputStream = new FileInputStream(file);

        HttpRequest actual = HttpRequestFactory.parse(inputStream);

        assertThat(actual.getRequestLine().getMethod().name()).isEqualTo("POST");
        assertThat(actual.getRequestLine().getPathValue()).isEqualTo("/user/create");
        assertThat(actual.getHttpHeaders().getHeader(CONNECTION)).isEqualTo("keep-alive");
        assertThat(actual.getHttpHeaders().getHeader(ACCEPT)).isEqualTo("*/*");
        assertThat(actual.getBody().getContent("userId")).isEqualTo("javajigi");
    }

    @Test
    @DisplayName("Post 요청의 QueryString과 RequesetBody 를 파싱한다.")
    void request_POST2() throws IOException {
        File file = new File(String.format(TEST_RESOURCE_PATH, "Http_POST2.txt"));
        FileInputStream inputStream = new FileInputStream(file);

        HttpRequest actual = HttpRequestFactory.parse(inputStream);

        assertThat(actual.getRequestLine().getMethod().name()).isEqualTo("POST");
        assertThat(actual.getRequestLine().getPathValue()).isEqualTo("/user/create");
        assertThat(actual.getHttpHeaders().getHeader(CONNECTION)).isEqualTo("keep-alive");
        assertThat(actual.getHttpHeaders().getHeader(ACCEPT)).isEqualTo("*/*");
        assertThat(actual.getBody().getContent("userId")).isEqualTo("javajigi");
        assertThat(actual.getRequestLine().getParameter("id")).isEqualTo("1");
    }
}
