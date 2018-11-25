package com.example.luxed.karatefrontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.luxed.karatefrontend.Entities.Account;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView tvErrorMessage;
    private Spinner spinFighters;
    private Button btnConnection;

    private ArrayList<Account> accounts = new ArrayList<>();
    private Account current;

    private String connectionId;

    private OkHttpClient client;

    private void updateSpinFighters() {
        List<String> accEmails = new ArrayList<>();
        for (Account account : accounts)
            accEmails.add(account.getEmail());

        ArrayAdapter<String> adapterFighters = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, accEmails);

        spinFighters.setAdapter(adapterFighters);
    }

    private Account getCurrent() {
        return accounts.get(spinFighters.getSelectedItemPosition());
    }

    private String getCurrentEmail() {
        return getCurrent().getEmail();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        spinFighters = findViewById(R.id.spinFighters);
        btnConnection = findViewById(R.id.btnConnection);

        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/lstComptes")
                .build();

        Log.i("MainActivity", request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                tvErrorMessage.setText("La liste des comptes n'a pas pu être retournée");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                tvErrorMessage.setText(R.string.ok);
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray arrComptes = jsonObject.getJSONArray("comptes");

                    for (int i = 0; i < arrComptes.length(); i++) {
                        JSONObject obj = arrComptes.getJSONObject(i);
                        Account acc = new Account(obj.getString("courriel"), obj.getString("avatar"));
                        accounts.add(acc);
                    }

                    MainActivity.this.runOnUiThread(() -> updateSpinFighters());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btnConnection.setOnClickListener(v -> {
            if (btnConnection.getText() == getResources().getString(R.string.connection_open)) {
                btnConnection.setText(R.string.connection_close);
                spinFighters.setEnabled(false);

                Request request1 = new Request.Builder()
                        .url("http://10.0.2.2:8080/login/" + getCurrentEmail())
                        .build();

                client.newCall(request1).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Le compte n'a pas pu être connecté"));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();

                        MainActivity.this.runOnUiThread(() -> {
                            //tvErrorMessage.setText(res);
                            current = getCurrent();
                            current.setSessionId(res);
                        });
                    }
                });

            } else {
                btnConnection.setText(R.string.connection_open);
                spinFighters.setEnabled(true);

                Request request1 = new Request.Builder()
                        .url("http://10.0.2.2:8080/logout/" + current.getEmail())
                        .build();

                client.newCall(request1).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Le compte n'a pas pu être déconnecté"));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();

                        MainActivity.this.runOnUiThread(() -> {
                            tvErrorMessage.setText(res);
                            current = null;
                        });
                    }
                });
            }
        });
    }
}
