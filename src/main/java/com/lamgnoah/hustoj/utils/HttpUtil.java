package com.lamgnoah.hustoj.utils;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpUtil {
  private final OkHttpClient client;

  @Value("${judger.token}")
  private String token;

  public String post(String url, String json) throws IOException {
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
    Request request = new Request
        .Builder()
        .header("Content-Type", "application/json")
        .header("X-Judge-Server-Token", token)
        .url(url)
        .post(body)
        .build();
    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }
}
