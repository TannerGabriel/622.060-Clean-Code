package at.aau.core;

import at.aau.utils.Logger;

import at.aau.wrapper.DocumentWrapper;
import at.aau.wrapper.WebCrawler;
import at.aau.wrapper.WebCrawlerImpl;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class Translator {
    private static final Logger logger = Logger.getInstance();
    private final WebCrawler webCrawler = new WebCrawlerImpl();
    private String targetLanguage;
    protected OkHttpClient httpClient = new OkHttpClient();

    public Translator(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String translate(String text) {
        if (!validateApiKey()) return text;

        try {
            JSONObject jsonPayload = new JSONObject()
                    .put("q", text)
                    .put("source", "en")
                    .put("target", targetLanguage)
                    .put("format", "text");

            Response response = httpClient.newCall(buildRequest("https://google-translator9.p.rapidapi.com/v2", jsonPayload, "POST")).execute();
            if (!validateResponse(response)) {
                return text;
            }
            return parseTranslation(response);
        } catch (IOException e) {
            logger.logError("Translation failed: " + e.getMessage());
            return text;
        }
    }

    public String getSourceLanguage(String url) {
        if (!validateApiKey()) return "";

        try {
            JSONObject jsonPayload = new JSONObject().put("q", getHeading(url));

            Response response = httpClient.newCall(buildRequest("https://google-translator9.p.rapidapi.com/v2/detect", jsonPayload, "POST")).execute();
            return parseLanguageDetection(response);
        } catch (IOException e) {
            logger.logError("Language detection failed: " + e.getMessage());
            return "";
        }
    }

    public boolean isValidTargetLanguage() {
        if (!validateApiKey()) return false;

        try {
            Response response = httpClient.newCall(buildRequest("https://google-translator9.p.rapidapi.com/v2/languages", null, "GET")).execute();
            return checkLanguageAvailability(response, targetLanguage);
        } catch (IOException e) {
            logger.logError("Failed to fetch available languages: " + e.getMessage());
            return false;
        }
    }

    protected Request buildRequest(String url, JSONObject jsonPayload, String method) {
        MediaType mediaType = MediaType.parse("application/json");

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("X-RapidAPI-Key", getTranslateApiKey())
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com");

        if (method.equals("POST")) {
            builder.addHeader("content-type", "application/json");
            builder.post(RequestBody.create(jsonPayload.toString(), mediaType));
        } else if (method.equals("GET")) {
            builder.get();
        }

        return builder.build();
    }

    protected String parseTranslation(Response response) throws IOException {
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
    }

    protected String parseLanguageDetection(Response response) throws IOException {
        if (!validateResponse(response)) {
            return "";
        }

        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getJSONObject("data").getJSONArray("detections").getJSONArray(0).getJSONObject(0).getString("language");
    }

    protected boolean checkLanguageAvailability(Response response, String targetLanguage) throws IOException {
        if (!validateResponse(response)) {
            return false;
        }

        JSONObject jsonObject = new JSONObject(response.body().string());
        JSONArray languages = jsonObject.getJSONObject("data").getJSONArray("languages");
        for (int i = 0; i < languages.length(); i++) {
            if (languages.getJSONObject(i).getString("language").equals(targetLanguage)) {
                return true;
            }
        }
        return false;
    }

    protected boolean validateResponse(Response response) {
        if (!response.isSuccessful()) {
            logger.logError("Request failed with status code: " + response.code());
            return false;
        }
        return true;
    }

    protected String getTranslateApiKey() {
        return System.getenv("TRANSLATION_API_KEY");
    }

    protected boolean validateApiKey() {
        String apiKey = getTranslateApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            logger.logError("API key is not valid or not set.");
            return false;
        }
        return true;
    }

    protected String getHeading(String url) throws IOException {
        DocumentWrapper doc = webCrawler.get(url);

        return doc.extractHeadings()[0].text();
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }
}
