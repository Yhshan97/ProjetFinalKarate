package com.example.luxed.karatefrontend;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.luxed.karatefrontend.Adapters.AccountAdapter;
import com.example.luxed.karatefrontend.Entities.Account;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvErrorMessage;
    private Spinner spinFighters;
    private Button btnConnection;
    private TextView tvInfosFighter;
    private Button btnMessagePrivate;
    private TextView tvMessagePrivate;
    private Button btnMessagePublic;
    private TextView tvMessagePublic;
    private RadioButton rbElsewhere;
    private CheckBox cbArbiter;
    private Button btnFightRed;
    private Button btnFightWhite;
    private Button btnFightTie;
    private Button btnArbiterRed;
    private Button btnArbiterRedFault;
    private Button btnPassExam;
    private Button btnFailExam;
    private Button btnChangeRole;

    private LinkedHashMap<String, Account> accounts = new LinkedHashMap<>();
    private HashMap<String, String> emailLieu = new HashMap<>();
    private Account current;
    private ArrayList<Account> lstArbiters = new ArrayList<>();
    private RecyclerView rvArbitrators;
    private AccountAdapter adapterArbitrators;
    private ArrayList<Account> lstElsewhere = new ArrayList<>();
    private RecyclerView rvElsewhere;
    private AccountAdapter adapterElsewhere;
    private ArrayList<Account> lstSpectating = new ArrayList<>();
    private RecyclerView rvSpectating;
    private AccountAdapter adapterSpectating;
    private ArrayList<Account> lstWaiting = new ArrayList<>();
    private RecyclerView rvWaiting;
    private AccountAdapter adapterWaiting;

    private HttpConnection httpConnection;
    private StompConnectionJava stompConnection;

    private void updateSpinFighters() {
        List<String> accEmails = new ArrayList<>();
        accounts.forEach((email, acc) -> accEmails.add(email));

        ArrayAdapter<String> adapterFighters = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, accEmails);

        spinFighters.setAdapter(adapterFighters);
    }

    private Account getCurrent() {
        return accounts.get(spinFighters.getSelectedItem().toString());
    }

    private String getCurrentEmail() {
        return getCurrent().getEmail();
    }

    private void updateLstAccounts(boolean current) {
        httpConnection.executeRequest(
                "http://10.0.2.2:8080/lstComptes",
                (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("La liste des comptes n'a pas pu être retournée")),
                (call, response) -> {
                    tvErrorMessage.setText(R.string.ok);
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        updateAccountList(jsonObject, current);

                        MainActivity.this.runOnUiThread(() -> {
                            updateSpinFighters();
                            updateRecyclerViews();
                            if (current) {
                                updateInfoFighter();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private void updateAccountList(JSONObject jsonObject, boolean updateCurrent) throws JSONException {
        JSONArray arrComptes = jsonObject.getJSONArray("comptes");

        for (int i = 0; i < arrComptes.length(); i++) {
            JSONObject obj = arrComptes.getJSONObject(i);
            Account acc = new Account(
                    obj.getString("courriel"),
                    obj.getString("fullName"),
                    obj.getString("avatar"),
                    obj.getString("role"),
                    obj.getString("groupe"),
                    obj.getInt("points"),
                    obj.getInt("credits"));

            if (updateCurrent && acc.getEmail().equals(current.getEmail())) {
                Log.i("UpdateCurrent", obj.toString());
                String sessionId = current.getSessionId();
                Log.i("UpdateCurrent", "Current: " + current.getPoints() + ", " + current.getCredits());
                current = acc;
                current.setSessionId(sessionId);
                Log.i("UpdateCurrent", "Current: " + current.getPoints() + ", " + current.getCredits());
            }

            accounts.put(acc.getEmail(), acc);
        }
    }

    private void updateInfoFighter() {
        Log.i("UpdateInfo", "Info update");
        Log.i("UpdateInfo", "New info: " + current.getPoints() + ", " + current.getCredits());
        tvInfosFighter.setText(current.getFullName() + ", " + current.getGroupe() + ", " + current.getRole() +
                ", points: " + current.getPoints() + ", credits: " + current.getCredits());
    }

    private void updateRecyclerViews() {
        lstArbiters.clear();
        lstElsewhere.clear();
        lstSpectating.clear();
        lstWaiting.clear();

        emailLieu.forEach((email, lieu) -> {
            switch (lieu) {
                case "arbitre":
                    lstArbiters.add(accounts.get(email));
                    break;
                case "ailleurs":
                    lstElsewhere.add(accounts.get(email));
                    break;
                case "spectateur":
                    lstSpectating.add(accounts.get(email));
                    break;
                case "attente":
                    lstWaiting.add(accounts.get(email));
                    break;
            }
        });

        //Arrays.sort(lstArbiters);

        adapterArbitrators.notifyDataSetChanged();
        adapterElsewhere.notifyDataSetChanged();
        adapterSpectating.notifyDataSetChanged();
        adapterWaiting.notifyDataSetChanged();
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
        rbElsewhere = findViewById(R.id.rbElsewhere);

        rvArbitrators = findViewById(R.id.rvArbitrators);
        adapterArbitrators = new AccountAdapter(lstArbiters);
        RecyclerView.LayoutManager lmArbitrators = new LinearLayoutManager(getApplicationContext());
        rvArbitrators.setLayoutManager(lmArbitrators);
        rvArbitrators.setItemAnimator(new DefaultItemAnimator());
        rvArbitrators.setAdapter(adapterArbitrators);

        rvElsewhere = findViewById(R.id.rvElsewhere);
        adapterElsewhere = new AccountAdapter(lstElsewhere);
        RecyclerView.LayoutManager lmElsewhere = new LinearLayoutManager(getApplicationContext());
        rvElsewhere.setLayoutManager(lmElsewhere);
        rvElsewhere.setItemAnimator(new DefaultItemAnimator());
        rvElsewhere.setAdapter(adapterElsewhere);

        rvSpectating = findViewById(R.id.rvSpectators);
        adapterSpectating = new AccountAdapter(lstSpectating);
        RecyclerView.LayoutManager lmSpectating = new LinearLayoutManager(getApplicationContext());
        rvSpectating.setLayoutManager(lmSpectating);
        rvSpectating.setItemAnimator(new DefaultItemAnimator());
        rvSpectating.setAdapter(adapterSpectating);

        rvWaiting = findViewById(R.id.rvWaiting);
        adapterWaiting = new AccountAdapter(lstWaiting);
        RecyclerView.LayoutManager lmWaiting = new LinearLayoutManager(getApplicationContext());
        rvWaiting.setLayoutManager(lmWaiting);
        rvWaiting.setItemAnimator(new DefaultItemAnimator());
        rvWaiting.setAdapter(adapterWaiting);

        RadioGroup rgPlace = findViewById(R.id.rgPlace);

        httpConnection = new HttpConnection();

        stompConnection = new StompConnectionJava("ws://10.0.2.2:8080/webSocket/websocket");
        // Place
        stompConnection.subChangePlace(stompMessage -> {
            //Log.i("ChangePlace", stompMessage.getPayload());
            JSONObject obj = new JSONObject(stompMessage.getPayload());
            emailLieu.clear();
            obj.keys().forEachRemaining(key -> {
                try {
                    String place = obj.getString(key);
                    emailLieu.put(key, place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            if (accounts.size() > 0) {
                MainActivity.this.runOnUiThread(() -> updateRecyclerViews());
            }
        });

        updateLstAccounts(false);

        // Connexion
        btnConnection.setOnClickListener(v -> {
            if (btnConnection.getText() == getResources().getString(R.string.connection_open)) {
                btnConnection.setText(R.string.connection_close);
                spinFighters.setEnabled(false);

                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/login/" + getCurrentEmail()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Le compte n'a pas pu être connecté")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> {
                                tvErrorMessage.setText("Le compte est connecté");
                                current = getCurrent();
                                current.setSessionId(res);

                                updateInfoFighter();
                            });
                        }
                );
            } else {
                btnConnection.setText(R.string.connection_open);
                spinFighters.setEnabled(true);
                rbElsewhere.setChecked(true);
                cbArbiter.setChecked(false);

                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/logout/%s", current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Le compte n'a pas pu être déconnecté")),
                        (call, response) -> {
                            String res = response.body().string();

                            MainActivity.this.runOnUiThread(() -> {
                                tvErrorMessage.setText(res);
                                current = null;

                                tvInfosFighter.setText(R.string.fighter_info);
                            });
                        }
                );
            }
        });

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
        stompConnection.subMAJCompte(stompMessage -> {
            Log.i("MAJCompte", "MAJ des comptes");
            try {
                JSONObject jsonObject = new JSONObject(stompMessage.getPayload());
                updateAccountList(jsonObject, true);

                MainActivity.this.runOnUiThread(() -> {
                    updateSpinFighters();
                    updateRecyclerViews();
                    updateInfoFighter();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        stompConnection.subLstComptes(stompMessage -> {
            Log.i("LstComptes", "MAJ des comptes");
            try {
                JSONObject jsonObject = new JSONObject(stompMessage.getPayload());
                updateAccountList(jsonObject, true);

                MainActivity.this.runOnUiThread(() -> {
                    updateSpinFighters();
                    updateRecyclerViews();
                    updateInfoFighter();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Message privé
        btnMessagePrivate.setOnClickListener(v -> stompConnection.sendMessagePrivate(current));

        // Message publique
        btnMessagePublic.setOnClickListener(v -> stompConnection.sendMessagePublic(current));

        rgPlace.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbElsewhere:
                    Log.i("MainActivity", "Elsewhere");
                    stompConnection.sendChangePlace(current, "ailleurs", false);
                    break;
                case R.id.rbSpectator:
                    Log.i("MainActivity", "Spectator");
                    stompConnection.sendChangePlace(current, "spectateur", false);
                    break;
                case R.id.rbWaiting:
                    Log.i("MainActivity", "Waiting");
                    stompConnection.sendChangePlace(current, "attente", false);
                    break;
            }
        });

        cbArbiter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.i("MainActivity", "Arbitre");
                stompConnection.sendChangePlace(current, "attente", true);
            } else {
                Log.i("MainActivity", "Plus Arbitre");
                stompConnection.sendChangePlace(current, "attente", false);
            }
        });

        // Combat rouge
        btnFightRed.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/combat1/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Combat blanc impossible")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText(res));
                        });
        });
        // Combat blanc
        btnFightWhite.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/combat2/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Combat blanc impossible")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText(res));
                        });
        });
        // Combat nul
        btnFightTie.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/combat3/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Combat blanc impossible")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText(res));
                        });
        });
        // Arbitre rouge
        btnArbiterRed.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/arbitrer1/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Combat blanc impossible")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText(res));
                        });
        });
        // Arbitre rouge avec faute
        btnArbiterRedFault.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/arbitrer2/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Combat blanc impossible")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText(res));
                        });
        });
        // Passer examen
        btnPassExam.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/examen1/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Impossible de faire l'examen")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> {
                                tvErrorMessage.setText(res);

                                stompConnection.sendGetLstComptes();
                            });
                        });
        });
        // Fail exam
        btnFailExam.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/examen2/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Impossible de faire l'examen")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> {
                                tvErrorMessage.setText(res);

                                //stompConnection.sendGetLstComptes();
                            });
                        });
        });
        // Changer role
        btnChangeRole.setOnClickListener(v -> {
            if (current != null)
                httpConnection.executeRequest(
                        String.format("http://10.0.2.2:8080/passage/%s/%s", current.getEmail(), current.getSessionId()),
                        (call, e) -> MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText("Impossible de changer de role")),
                        (call, response) -> {
                            String res = response.body().string();
                            MainActivity.this.runOnUiThread(() -> tvErrorMessage.setText(res));
                        }
                );
        });
    }
}
