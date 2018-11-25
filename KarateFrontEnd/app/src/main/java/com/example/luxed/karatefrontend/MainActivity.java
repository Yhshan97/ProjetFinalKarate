package com.example.luxed.karatefrontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView tvErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/lstComptes")
                .build();

        Log.i("MainActivity", request.toString());

        //String clients = "";

        //Response res = client.newCall(request).execute();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                tvErrorMessage.setText("La liste des comptes n'a pas pu être retournée");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                tvErrorMessage.setText(R.string.ok);
                try {
                    Log.i("MainActivity", response.body().string());
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    //Log.i("MainActivity", response.body().string());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
