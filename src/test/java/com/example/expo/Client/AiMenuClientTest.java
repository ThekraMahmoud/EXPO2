package com.example.expo.Client;

import com.example.expo.Api.ApiException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import java.net.http.*;
import java.util.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiMenuClientTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test void disabledOrMissingKeyDoesNotCallProvider() {
        var http = mock(HttpClient.class);
        assertThrows(ApiException.class, () -> new AiMenuClient(mapper, false, "", "openrouter/free", http).analyzePdf(new byte[]{1}));
        verifyNoInteractions(http);
    }

    @Test void parsesArabicAndNullPrice() {
        var result = client().parseResponse(envelope("STOP", "{\"isMenu\":true,\"currency\":\"SAR\",\"items\":[{\"name\":\"لاتيه\",\"description\":null,\"price\":null}],\"warnings\":[]}"));
        assertTrue(result.isMenu()); assertEquals("لاتيه", result.items().get(0).name()); assertNull(result.items().get(0).price());
    }

    @Test void rejectsTruncatedBlockedMalformedAndMissingFields() {
        for (String response : List.of(envelope("MAX_TOKENS", "{}"), envelope("SAFETY", "{}"), envelope("STOP", "not json"),
                envelope("STOP", "{\"items\":[],\"warnings\":[]}"), "{}")) {
            assertThrows(ApiException.class, () -> client().parseResponse(response));
        }
    }

    @Test void quotaFailureHasNoPaidFallbackOrRetry() throws Exception {
        var http = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(429);
        when(http.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        assertTrue(assertThrows(ApiException.class, () -> new AiMenuClient(mapper, true, "test-key", "openrouter/free", http)
                .analyzePdf(new byte[]{1})).getMessage().contains("quota"));
        verify(http, times(1)).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test void rejectsPaidModelBeforeSendingAnything() {
        var http = mock(HttpClient.class);
        var error = assertThrows(ApiException.class, () -> new AiMenuClient(mapper, true, "test-key", "google/gemini-3.5-flash-lite", http)
                .analyzePdf(new byte[]{1}));
        assertTrue(error.getMessage().contains(":free")); verifyNoInteractions(http);
    }

    @Test void sendsPdfToOpenRouterWithExplicitFreeParserAndZeroTokenPriceCap() throws Exception {
        var http = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(envelope("STOP", "{\"isMenu\":true,\"currency\":\"SAR\",\"items\":[],\"warnings\":[]}"));
        when(http.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        new AiMenuClient(mapper, true, "test-key", "openrouter/free", http).analyzePdf("%PDF-1.7".getBytes());
        var capture = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http).send(capture.capture(), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        var request = capture.getValue();
        assertEquals("https://openrouter.ai/api/v1/chat/completions", request.uri().toString());
        assertEquals("Bearer test-key", request.headers().firstValue("Authorization").orElseThrow());
        var sink = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<ByteBuffer>() {
            public void onSubscribe(Flow.Subscription subscription) { sink.onSubscribe(subscription); }
            public void onNext(ByteBuffer bytes) { sink.onNext(List.of(bytes)); }
            public void onError(Throwable error) { sink.onError(error); }
            public void onComplete() { sink.onComplete(); }
        });
        var body = mapper.readTree(sink.getBody().toCompletableFuture().get(2, TimeUnit.SECONDS));
        assertEquals("openrouter/free", body.path("model").asString());
        assertEquals("cloudflare-ai", body.path("plugins").path(0).path("pdf").path("engine").asString());
        assertEquals(0, body.path("provider").path("max_price").path("prompt").asInt());
        assertEquals(0, body.path("provider").path("max_price").path("completion").asInt());
        assertEquals(0, body.path("provider").path("max_price").path("request").asInt());
        assertEquals(0, body.path("provider").path("max_price").path("image").asInt());
        assertEquals("json_object", body.path("response_format").path("type").asString());
        assertTrue(body.path("messages").path(1).path("content").path(1).path("file").path("file_data").asString()
                .startsWith("data:application/pdf;base64,"));
    }

    @Test void rejectsProviderErrorEvenIfHttpStatusWasSuccessful() {
        var error = assertThrows(ApiException.class, () -> client().parseResponse("{\"error\":{\"message\":\"No free provider available\"}}"));
        assertTrue(error.getMessage().contains("No free provider available"));
    }

    @Test void successfulCatalogRequestPreservesBooksAndLaptopVariants() throws Exception {
        var http = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(envelope("STOP", """
                {"isMenu":true,"currency":"SAR","items":[
                  {"name":"كتاب رحلة - الطبعة الثانية","description":"تأليف أحمد","price":49.95},
                  {"name":"Laptop X 16GB / 512GB","description":"شاشة 14 بوصة","price":3299.00},
                  {"name":"Laptop X 32GB / 1TB","description":null,"price":4299.00}
                ],"warnings":[]}
                """));
        when(http.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        var result = new AiMenuClient(mapper, true, "test-key", "openrouter/free", http).analyzePdf("%PDF-1.7".getBytes());
        assertTrue(result.isMenu()); assertEquals(3, result.items().size());
        assertEquals(new BigDecimal("49.95"), result.items().get(0).price());
        assertEquals("تأليف أحمد", result.items().get(0).description());
        assertEquals("Laptop X 32GB / 1TB", result.items().get(2).name());
        assertEquals(0, new BigDecimal("4299").compareTo(result.items().get(2).price()));
        verify(http, times(1)).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    private AiMenuClient client() { return new AiMenuClient(mapper, false, "", "openrouter/free"); }

    @Test void providerRejectionShowsDiagnosticWithoutLeakingConfiguredKey() throws Exception {
        var http = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn(mapper.writeValueAsString(Map.of("error", Map.of(
                "message", "Invalid response schema for PDF. key=test-key; api_key=other-secret", "status", "INVALID_ARGUMENT"))));
        when(http.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        var error = assertThrows(ApiException.class, () -> new AiMenuClient(mapper, true, "test-key", "openrouter/free", http)
                .analyzePdf(new byte[]{1}));
        assertTrue(error.getMessage().contains("Invalid response schema"));
        assertFalse(error.getMessage().contains("test-key"));
        assertFalse(error.getMessage().contains("other-secret"));
        verify(http, times(1)).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test void malformedProviderErrorDoesNotExposeRawBody() throws Exception {
        var http = mock(HttpClient.class);
        @SuppressWarnings("unchecked") HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(400); when(response.body()).thenReturn("not-json test-key");
        when(http.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        var error = assertThrows(ApiException.class, () -> new AiMenuClient(mapper, true, "test-key", "openrouter/free", http)
                .analyzePdf(new byte[]{1}));
        assertEquals("OpenRouter could not process PDF (HTTP 400)", error.getMessage());
    }
    private String envelope(String finish, String text) {
        return mapper.writeValueAsString(Map.of("choices", List.of(Map.of("finish_reason", "STOP".equals(finish) ? "stop" : finish,
                "message", Map.of("role", "assistant", "content", text)))));
    }
}
