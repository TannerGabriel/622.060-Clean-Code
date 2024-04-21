package at.aau.core;

import okhttp3.*;
import org.json.JSONObject;

public class Translator {
    private OkHttpClient httpClient = new OkHttpClient();;

    public String translate(String text, String targetLanguage) {
        Request request = createRequest(text, targetLanguage);

        try {
            Response response = httpClient.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            return jsonObject.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
            return text;
        }
    }

    private Request createRequest(String text, String targetLanguage) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"q\": \"" + text + "\",\"source\": \"en\",\"target\": \"" + targetLanguage + "\",\"format\": \"text\"}", mediaType);
        return new Request.Builder()
                .url("https://google-translator9.p.rapidapi.com/v2")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", System.getenv("CLEANCODEAPIKEY"))
                .addHeader("X-RapidAPI-Host", "google-translator9.p.rapidapi.com")
                .build();
    }
}
