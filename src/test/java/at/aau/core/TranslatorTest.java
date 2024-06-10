package at.aau.core;

import at.aau.utils.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import okio.Buffer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TranslatorTest {
    private Translator translator;
    private Translator translatorSpy;

    private final PrintStream originalOut = System.out;

    private final Logger logger = Logger.getInstance();

    @Mock
    private OkHttpClient mockHttpClient;
    @Mock
    private Call mockCall;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        translator = new Translator("en");
        translator.httpClient = mockHttpClient;
        translatorSpy = spy(translator);
    }

    @AfterEach
    void teardown() {
        System.setErr(originalOut);
    }

    @Test
    void testTranslateValid() throws IOException {
        Response response = createValidTranslationResponse();

        setupCommonMocks();
        when(mockCall.execute()).thenReturn(response);

        assertEquals("Hello World!", translatorSpy.translate("Hallo Welt!"));
    }

    @Test
    void testTranslateRequestValidInvalidResponse() throws IOException {
        Response response = createValidTranslationResponse();

        setupCommonMocks();
        when(mockCall.execute()).thenReturn(response);
        when(translatorSpy.validateResponse(response)).thenReturn(false);

        assertEquals("Hallo Welt!", translatorSpy.translate("Hallo Welt!"));
    }


    @Test
    void testTranslateInvalidApiKey() {
        when(translatorSpy.validateApiKey()).thenReturn(false);

        assertEquals("Hallo Welt!", translatorSpy.translate("Hallo Welt!"));
    }

    @Test
    void testTranslateException() throws IOException {
        IOException toThrow = new IOException("Failed to connect");

        setupCommonMocks();
        doThrow(toThrow).when(mockCall).execute();

        assertEquals("Hallo Welt!", translatorSpy.translate("Hallo Welt!"));
        assertTrue(logger.getLogsString().contains("Translation failed: Failed to connect"));
    }

    @Test
    void testGetSourceLanguageValid() throws IOException {
        Response response = createValidLanguageDetectionResponse();

        setupCommonMocks();
        doReturn("I am a Heading").when(translatorSpy).getHeading("https://google.com");
        when(mockCall.execute()).thenReturn(response);

        assertEquals("en", translatorSpy.getSourceLanguage("https://google.com"));
    }

    @Test
    void testGetSourceLanguageInvalidAPIKey() {
        when(translatorSpy.validateApiKey()).thenReturn(false);

        assertEquals("", translatorSpy.getSourceLanguage("https://google.com"));
    }

    @Test
    void testGetSourceLanguageInvalidException() throws IOException {
        IOException toThrow = new IOException("Failed to connect");

        setupCommonMocks();
        doReturn("I am a Heading").when(translatorSpy).getHeading("https://google.com");
        doThrow(toThrow).when(mockCall).execute();

        assertEquals("", translatorSpy.getSourceLanguage("https://google.com"));
        assertTrue(logger.getLogsString().contains("Language detection failed: Failed to connect"));
    }


    @Test
    void testIsValidTargetLanguageValidRequest() throws IOException {
        Response response = createLanguageResponse();

        setupCommonMocks();
        when(mockCall.execute()).thenReturn(response);

        assertTrue(translatorSpy.isValidTargetLanguage());
    }

    @Test
    void testIsValidTargetLanguageInvalidAPIKey() {
        when(translatorSpy.validateApiKey()).thenReturn(false);

        assertFalse(translatorSpy.isValidTargetLanguage());
    }

    @Test
    void testIsValidTargetLanguageException() throws IOException {
        IOException toThrow = new IOException("Failed to connect");

        setupCommonMocks();
        doThrow(toThrow).when(mockCall).execute();

        assertFalse(translatorSpy.isValidTargetLanguage());
        assertTrue(logger.getLogsString().contains("Failed to fetch available languages: Failed to connect"));
    }

    @Test
    void testBuildRequestPOST() throws IOException {
        JSONObject jsonPayload = new JSONObject().put("q", "en");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody expectedBody = RequestBody.create(jsonPayload.toString(), mediaType);
        when(translatorSpy.getTranslateApiKey()).thenReturn("API_TOKEN");

        Request request = translatorSpy.buildRequest("https://google.com", jsonPayload, "POST");

        assertEquals("https://google.com/", request.url().toString());
        assertEquals("API_TOKEN", request.header("X-RapidAPI-Key"));
        assertEquals("google-translator9.p.rapidapi.com", request.header("X-RapidAPI-Host"));
        assertEquals("application/json", request.header("content-type"));
        assertEquals(requestBodyToString(expectedBody), requestBodyToString(request.body()));
    }

    @Test
    void testBuildRequestGET() {
        when(translatorSpy.getTranslateApiKey()).thenReturn("API_TOKEN");

        Request request = translatorSpy.buildRequest("https://google.com", null, "GET");

        assertEquals("https://google.com/", request.url().toString());
        assertEquals("API_TOKEN", request.header("X-RapidAPI-Key"));
        assertEquals("google-translator9.p.rapidapi.com", request.header("X-RapidAPI-Host"));
    }

    @Test
    void testParseTranslationSuccessful() throws IOException {
        Response response = createValidTranslationResponse();

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

        assertEquals("", translator.parseLanguageDetection(invalidResponse));
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
        assertTrue(logger.getLogsString().contains("API key is not valid or not set."));
    }

    private Response createValidTranslationResponse() {
        JsonObject root = new JsonObject();
        JsonObject data = new JsonObject();
        JsonArray translations = new JsonArray();
        JsonObject translationDetails = new JsonObject();
        translationDetails.addProperty("translatedText", "Hello World!");
        translations.add(translationDetails);
        data.add("translations", translations);
        root.add("data", data);

        return createMockResponse(200, "OK", root);
    }

    private Response createValidLanguageDetectionResponse() {
        JsonObject root = new JsonObject();
        JsonObject data = new JsonObject();
        JsonArray detections = new JsonArray();
        JsonArray detectionDetails = new JsonArray();
        JsonObject detection = new JsonObject();
        detection.addProperty("confidence", 1);
        detection.addProperty("language", "en");
        detection.addProperty("isReliable", false);
        detectionDetails.add(detection);
        detections.add(detectionDetails);
        data.add("detections", detections);
        root.add("data", data);

        return createMockResponse(200, "OK", root);
    }

    private Response createLanguageResponse() {
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

        return createMockResponse(200, "OK", root);
    }

    private Response createSuccessfulResponse() {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("key", "value");
        return createMockResponse(200, "OK", jsonResponse);
    }

    private Response createFailingResponse() {
        return createMockResponse(400, "Bad request", new JsonObject());
    }

    private Response createMockResponse(int statusCode, String message, JsonObject jsonResponse) {
        ResponseBody responseBody = ResponseBody.create(
                jsonResponse.toString(), MediaType.get("application/json; charset=utf-8")
        );
        return new Response.Builder()
                .request(new Request.Builder().url("https://google.com").build())
                .protocol(Protocol.HTTP_2)
                .code(statusCode)
                .message(message)
                .body(responseBody)
                .build();
    }

    private String requestBodyToString(RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return buffer.readUtf8();
    }

    private void setupCommonMocks() {
        when(translatorSpy.getTranslateApiKey()).thenReturn("API_KEY");
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(translatorSpy.validateApiKey()).thenReturn(true);
    }

}
