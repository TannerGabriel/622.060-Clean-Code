package at.aau.core;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

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
            return parseTranslation(response);
        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
            return text;
        }
    }

    public String getSourceLanguage(String text) {
        if (!validateApiKey()) return "";

        try {
            JSONObject jsonPayload = new JSONObject().put("q", text);

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
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", getTranslateApiKey())
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com");

        if (method.equals("POST")) {
            builder.post(RequestBody.create(jsonPayload.toString(), mediaType));
        } else if (method.equals("GET")) {
            builder.get();
        }

        System.out.println(builder.build().toString());

        return builder.build();
    }

    private String parseTranslation(Response response) throws IOException {
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
    }

    private String parseLanguageDetection(Response response) throws IOException {
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getJSONObject("data").getJSONArray("detections").getJSONArray(0).getJSONObject(0).getString("language");
    }

    private boolean checkLanguageAvailability(Response response, String targetLanguage) throws IOException {
        JSONObject jsonObject = new JSONObject(response.body().string());
        JSONArray languages = jsonObject.getJSONObject("data").getJSONArray("languages");
        for (int i = 0; i < languages.length(); i++) {
            if (languages.getJSONObject(i).getString("language").equals(targetLanguage)) {
                return true;
            }
        }
        return false;
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
}
