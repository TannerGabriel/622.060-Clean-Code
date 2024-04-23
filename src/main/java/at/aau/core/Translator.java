package at.aau.core;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Translator {
    private OkHttpClient httpClient = new OkHttpClient();

    public String translate(String text, String targetLanguage) {
        if (!validateApiKey()) return text;

        Request request = createTranslateRequest(text, targetLanguage);

        try {
            Response response = httpClient.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            return jsonObject.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
            return text;
        }
    }

    public String getSourceLanguage(String text) {
        if (!validateApiKey()) return "";

        Request request = createLanguageDetectionRequest(text);

        try {
            Response response = httpClient.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            return jsonObject.getJSONObject("data").getJSONArray("detections").getJSONArray(0).getJSONObject(0).getString("language");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean isValidTargetLanguage(String targetLanguage) {
        if (!validateApiKey()) return false;

        Request request = getAvailableLanguagesRequest();

        try {
            Response response = httpClient.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            JSONArray availableLangauges = jsonObject.getJSONObject("data").getJSONArray("languages");

            for(int i = 0; i < availableLangauges.length(); i++){
                if(availableLangauges.getJSONObject(i).getString("language").equals(targetLanguage)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private Request createTranslateRequest(String text, String targetLanguage) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"q\": \"" + text + "\",\"source\": \"en\",\"target\": \"" + targetLanguage + "\",\"format\": \"text\"}", mediaType);
        return new Request.Builder()
                .url("https://google-translator9.p.rapidapi.com/v2")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", getTranslateApiKey())
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
                .build();
    }

    private Request createLanguageDetectionRequest(String text) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\r\"q\": \"" + text + "?\"\r}", mediaType);
        return new Request.Builder()
                .url("https://google-translator9.p.rapidapi.com/v2/detect")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", getTranslateApiKey())
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
                .build();
    }

    private Request getAvailableLanguagesRequest() {
        return new Request.Builder()
                .url("https://google-translator9.p.rapidapi.com/v2/languages")
                .get()
                .addHeader("X-RapidAPI-Key", getTranslateApiKey())
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
                .build();
    }

    private String getTranslateApiKey() {
        String key = System.getenv("CLEANCODEAPIKEY");
        if (key == null) {
            System.out.println("No translation API key found in the system. Continuing without translations!");
            return "";
        }
        return key;
    }

    private boolean validateApiKey() {
        return !getTranslateApiKey().isEmpty();
    }
}
