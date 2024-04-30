package at.aau.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import okio.Buffer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TranslatorTest {

    private Translator translator;
    private Translator translatorSpy;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setup() {
        translator = new Translator();
        translatorSpy = spy(translator);

        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void teardown() {
        System.setErr(originalOut);
    }

    @Test
    void testBuildRequestPOST() throws IOException {
        when(translatorSpy.getTranslateApiKey()).thenReturn("API_TOKEN");
        JSONObject jsonPayload = new JSONObject().put("q", "en");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody expectedBody = RequestBody.create(jsonPayload.toString(), mediaType);

        Request request = translatorSpy.buildRequest("https://google.com", jsonPayload, "POST");

        assertEquals("https://google.com/",request.url().toString());
        assertEquals("API_TOKEN",request.header("X-RapidAPI-Key"));
        assertEquals("google-translator9.p.rapidapi.com",request.header("X-RapidAPI-Host"));
        assertEquals("application/json",request.header("content-type"));
        assertEquals(requestBodyToString(expectedBody), requestBodyToString(request.body()));
    }

    @Test
    void testBuildRequestGET() {
        when(translatorSpy.getTranslateApiKey()).thenReturn("API_TOKEN");

        Request request = translatorSpy.buildRequest("https://google.com", null, "GET");

        assertEquals("https://google.com/",request.url().toString());
        assertEquals("API_TOKEN",request.header("X-RapidAPI-Key"));
        assertEquals("google-translator9.p.rapidapi.com",request.header("X-RapidAPI-Host"));
    }

    @Test
    void testParseTranslationSuccessful() throws IOException {
        Response response = createValidTranslationRequest();

        assertEquals("Hello World!", translator.parseTranslation(response));
    }

    @Test
    void testParseLanguageDetectionSuccessful() throws IOException {
        Response response = createValidLanguageDetectionResponse();

        assertEquals("en", translator.parseLanguageDetection(response));
    }

    @Test
    void testParseLanguageDetectionInvalidResponse() throws IOException {
        Response invalidResponse = createFailingResponse();

        assertEquals("",translator.parseLanguageDetection(invalidResponse));
    }


    @Test
    void testCheckLanguageAvailableValidLanguage() throws IOException {
        Response response = createLanguageResponse();

        assertTrue(translator.checkLanguageAvailability(response, "de"));
    }

    @Test
    void testCheckLanguageAvailableInvalidLanguage() throws IOException {
        Response response = createLanguageResponse();

        assertFalse(translator.checkLanguageAvailability(response, "non"));
    }

    @Test
    void testCheckLanguageAvailableInvalidResponse() throws IOException {
        Response invalidResponse = createFailingResponse();

        assertFalse(translator.checkLanguageAvailability(invalidResponse, "en"));
    }

    @Test
    void testValidResponseSuccessful() {
        Response successResponse = createSuccessfulResponse();

        assertTrue(translator.validateResponse(successResponse));
    }

    @Test
    void testValidResponseFailed() {
        Response failingResponse = createFailingResponse();

        assertFalse(translator.validateResponse(failingResponse));
    }

    @Test
    void testValidateApiKeySuccessful() {
        when(translatorSpy.getTranslateApiKey()).thenReturn("SAMPLE_KEY");

        assertTrue(translatorSpy.validateApiKey());
    }

    @Test
    void testValidateApiKeyFailed() {
        when(translatorSpy.getTranslateApiKey()).thenReturn("");

        assertFalse(translatorSpy.validateApiKey());
        assertTrue(outContent.toString().contains("API key is not valid or not set."));
    }

    private Response createValidTranslationRequest() {
        Request request = new Request.Builder().url("https://google.com").build();

        JsonObject root = new JsonObject();

        JsonObject data = new JsonObject();
        root.add("data", data);

        JsonArray translations = new JsonArray();
        data.add("translations", translations);

        JsonObject translationDetails = new JsonObject();
        translationDetails.addProperty("translatedText", "Hello World!");
        translations.add(translationDetails);

        ResponseBody responseBody = ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                root.toString()
        );

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();
    }

    private Response createValidLanguageDetectionResponse() {
        Request request = new Request.Builder().url("https://google.com").build();

        JsonObject root = new JsonObject();
        JsonObject data = new JsonObject();
        root.add("data", data);

        JsonArray detections = new JsonArray();
        data.add("detections", detections);

        JsonArray detectionDetails = new JsonArray();
        detections.add(detectionDetails);

        JsonObject detection = new JsonObject();
        detection.addProperty("confidence", 1);
        detection.addProperty("language", "en");
        detection.addProperty("isReliable", false);
        detectionDetails.add(detection);

        ResponseBody responseBody = ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                root.toString()
        );

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();
    }

    private Response createLanguageResponse() {
        Request request = new Request.Builder().url("https://google.com").build();

        JsonObject root = new JsonObject();
        JsonObject data = new JsonObject();
        JsonArray languages = new JsonArray();

        JsonObject lang1 = new JsonObject();
        lang1.addProperty("language", "en");
        languages.add(lang1);

        JsonObject lang2 = new JsonObject();
        lang2.addProperty("language", "de");
        languages.add(lang2);

        data.add("languages", languages);
        root.add("data", data);

        ResponseBody responseBody = ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                root.toString()
        );

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();
    }


    private Response createSuccessfulResponse() {
        Request request = new Request.Builder().url("https://google.com").build();

        ResponseBody responseBody = ResponseBody.create(
                MediaType.get("application/json; charset=utf-8"),
                "{\"key\":\"value\"}"
        );

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();
    }

    private Response createFailingResponse() {
        Request request = new Request.Builder().url("https://google.com").build();

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(400)
                .message("Bad request")
                .body(null)
                .build();
    }

    private String requestBodyToString(RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return buffer.readUtf8();
    }
}
