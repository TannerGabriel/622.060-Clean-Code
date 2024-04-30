package at.aau.core;

import at.aau.io.LinkExtractor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Translator {
    private OkHttpClient httpClient = new OkHttpClient();

    public String translate(String text, String targetLanguage) {
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
        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
            return text;
        }
    }

    public String getSourceLanguage(String url){
        if (!validateApiKey()) return "";

        try {
            JSONObject jsonPayload = new JSONObject().put("q", getHeading(url));

            Response response = httpClient.newCall(buildRequest("https://google-translator9.p.rapidapi.com/v2/detect", jsonPayload, "POST")).execute();
            return parseLanguageDetection(response);
        } catch (Exception e) {
            System.out.println("Language detection failed: " + e.getMessage());
            return "";
        }
    }

    public boolean isValidTargetLanguage(String targetLanguage) {
        if (!validateApiKey()) return false;

        try {
            Response response = httpClient.newCall(buildRequest("https://google-translator9.p.rapidapi.com/v2/languages", null, "GET")).execute();
            return checkLanguageAvailability(response, targetLanguage);
        } catch (Exception e) {
            System.out.println("Failed to fetch available languages: " + e.getMessage());
            return false;
        }
    }

    private Request buildRequest(String url, JSONObject jsonPayload, String method) {
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

    private String parseTranslation(Response response) throws IOException {
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
    }

    private String parseLanguageDetection(Response response) throws IOException {
        if (!validateResponse(response)) {
            return "";
        }

        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getJSONObject("data").getJSONArray("detections").getJSONArray(0).getJSONObject(0).getString("language");
    }

    private boolean checkLanguageAvailability(Response response, String targetLanguage) throws IOException {
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

    private boolean validateResponse(Response response) {
        if (!response.isSuccessful()) {
            System.out.println("Request failed with status code: " + response.code());
            return false;
        }
        return true;
    }

    private String getTranslateApiKey() {
        return System.getenv("TRANSLATION_API_KEY");
    }

    private boolean validateApiKey() {
        String apiKey = getTranslateApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("API key is not valid or not set.");
            return false;
        }
        return true;
    }

    private String getHeading(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        LinkExtractor extractor = new LinkExtractor(doc);

        return extractor.extractHeadings().getFirst().text();
    }
}
