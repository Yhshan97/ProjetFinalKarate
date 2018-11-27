package com.example.luxed.karatefrontend;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class HttpConnection {
    public interface RequestFailure {
        void onFailure(Call call, IOException e);
    }
    public interface RequestResponse {
        void onResponse(Call call, Response response) throws IOException;
    }

    private OkHttpClient httpConnection;

    public HttpConnection() {
        this.httpConnection = new OkHttpClient();
    }

    public void executeRequest(String url, final RequestFailure requestFailure, final RequestResponse requestResponse) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        httpConnection.newCall(request).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
                requestFailure.onFailure(call, e);
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) throws IOException {
                requestResponse.onResponse(call, response);
            }
        });
    }
}
