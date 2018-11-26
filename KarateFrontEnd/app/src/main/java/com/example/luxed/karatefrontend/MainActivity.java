package com.example.luxed.karatefrontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
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
    private TextView tvInfosFighter;
    private Button btnMessagePrivate;
    private TextView tvMessagePrivate;
    private Button btnMessagePublic;
    private TextView tvMessagePublic;
    private CheckBox cbArbiter;
    private Button btnFightRed;
    private Button btnFightWhite;
    private Button btnFightTie;
    private Button btnArbiterRed;
    private Button btnArbiterRedFault;
    private Button btnPassExam;
    private Button btnFailExam;
    private Button btnChangeRole;

    private ArrayList<Account> accounts = new ArrayList<>();
    private Account current;
    private ArrayList<Account> lstArbiters = new ArrayList<>();
    private ArrayList<Account> lstElsewhere = new ArrayList<>();
    private ArrayList<Account> lstSpectating = new ArrayList<>();
    private ArrayList<Account> lstWaiting = new ArrayList<>();

    private String connectionId;

    private OkHttpClient client;
    private StompConnection stompConnection;

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
        tvInfosFighter = findViewById(R.id.tvFighterInfo);
        btnMessagePrivate = findViewById(R.id.btnPrivateMessage);
        tvMessagePrivate = findViewById(R.id.tvPrivateMessage);
        btnMessagePublic = findViewById(R.id.btnPublicMessage);
        tvMessagePublic = findViewById(R.id.tvPublicMessage);
        btnFightRed = findViewById(R.id.btnFightRed);
        btnFightWhite = findViewById(R.id.btnFightWhite);
        btnFightTie = findViewById(R.id.btnFightTie);
        btnArbiterRed = findViewById(R.id.btnArbiterRed);
        btnArbiterRedFault = findViewById(R.id.btnArbiterRedFault);
        btnPassExam = findViewById(R.id.btnExamPass);
        btnFailExam = findViewById(R.id.btnExamFail);
        btnChangeRole = findViewById(R.id.btnChangeRole);
        cbArbiter = findViewById(R.id.cbArbiter);

        RadioGroup rgPlace = findViewById(R.id.rgPlace);

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
                        Account acc = new Account(
                                obj.getString("courriel"),
                                obj.getString("fullName"),
                                obj.getString("avatar"),
                                obj.getString("role"),
                                obj.getString("groupe"));
                        accounts.add(acc);
                    }

                    MainActivity.this.runOnUiThread(() -> updateSpinFighters());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Connexion
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
                            tvErrorMessage.setText("Le compte est connecté");
                            current = getCurrent();
                            current.setSessionId(res);

                            tvInfosFighter.setText(current.getFullName() + ", " + current.getGroupe() + ", " + current.getRole());
                        });
                    }
                });
            } else {
                btnConnection.setText(R.string.connection_open);
                spinFighters.setEnabled(true);

                Request request1 = new Request.Builder()
                        .url("http://10.0.2.2:8080/logout/" + current.getSessionId())
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

                            tvInfosFighter.setText(R.string.fighter_info);
                        });
                    }
                });
            }
        });

        stompConnection = new StompConnection("ws://10.0.2.2:8080/webSocket/websocket");

        stompConnection.subReponsePrive(stompMessage -> {
            Log.i("StompPrive", stompMessage.getPayload());
            if (current != null) {
                JSONObject rep = new JSONObject(stompMessage.getPayload());
                Date creation = new Date(rep.getLong("creationTemps"));
                tvMessagePrivate.setText(rep.getString("de") + ", " + creation.toString() + ", " + rep.getString("contenu"));
            }
        });
        stompConnection.subReponsePublique(stompMessage -> {
            Log.i("StompPublic", stompMessage.getPayload());
            JSONObject rep = new JSONObject(stompMessage.getPayload());
            Date creation = new Date(rep.getLong("creationTemps"));
            tvMessagePublic.setText(rep.getString("de") + ", " + creation.toString() + ", " + rep.getString("contenu"));
        });

        // Message privé
        btnMessagePrivate.setOnClickListener(v -> stompConnection.sendMessagePrivate(current));

        // Message publique
        btnMessagePublic.setOnClickListener(v -> stompConnection.sendMessagePublic(current));

        // Place
        rgPlace.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbElsewhere:
                    Log.i("MainActivity", "Elsewhere");
                    break;
                case R.id.rbSpectator:
                    Log.i("MainActivity", "Spectator");
                    break;
                case R.id.rbWaiting:
                    Log.i("MainActivity", "Waiting");
                    break;
            }
        });

        cbArbiter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.i("MainActivity", "Arbitre");
            } else {
                Log.i("MainActivity", "Plus Arbitre");
            }
        });

        // Combat rouge
        btnFightRed.setOnClickListener(v -> stompConnection.sendFight(StompConnection.FightType.Red));
        // Combat blanc
        btnFightWhite.setOnClickListener(v -> stompConnection.sendFight(StompConnection.FightType.White));
        // Combat nul
        btnFightTie.setOnClickListener(v -> stompConnection.sendFight(StompConnection.FightType.Tie));
        // Arbitre Rouge
        btnArbiterRed.setOnClickListener(v -> stompConnection.sendArbiterRed());
        // Arbitre Rouge avec faute
        btnArbiterRedFault.setOnClickListener(v -> stompConnection.sendArbiterRedWithFault());
        // Passer examen
        btnPassExam.setOnClickListener(v -> stompConnection.sendPassExam());
        // Fail exam
        btnFailExam.setOnClickListener(v -> stompConnection.sendFailExam());
        // Changer role
        btnChangeRole.setOnClickListener(v -> stompConnection.sendChangeRole());
    }
}
