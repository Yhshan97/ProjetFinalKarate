package com.example.luxed.karatefrontend;

import android.util.Log;

import com.example.luxed.karatefrontend.Entities.Account;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class StompConnection {
    public enum FightType {
        Red, White, Tie
    }

    public interface StompMessageEvent {
        void gotMessage(StompMessage message);
    }

    private StompClient client;

    public StompConnection(String url) {
        client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url);

        /*Disposable dis = client.lifecycle().subscribe(lifecycle -> {
            switch (lifecycle.getType()) {
                case OPENED:
                    Log.d("STOMP", "Stomp connection opened");
                    break;

                case ERROR:
                    Log.e("STOMP", "Error", lifecycle.getException());
                    break;

                case CLOSED:
                    Log.d("STOMP", "Stomp connection closed");
                    break;
            }
        });*/

        client.connect();
    }

    public void subTopic(String destination, Consumer<StompMessage> event) {
        Log.i("STOMP", "Subscribing to " + destination);
        Disposable dis = client.topic(destination).subscribe(event);
    }

    public void subReponsePublique(Consumer<StompMessage> event) {
        subTopic("/sujet/reponsepublique", event);
    }

    public void subReponsePrive(Consumer<StompMessage> event) {
        subTopic("/sujet/reponseprive", event);
    }

    public void subChangePlace(Consumer<StompMessage> event) {
        subTopic("/sujet/lstLieux", event);
    }

    public void subMAJCompte(Consumer<StompMessage> event) {
        subTopic("/sujet/MAJCompte", event);
    }

    public void sendMessage(String destination, String message) {
        Log.i("STOMP", "Sending message to " + destination);
        client.send(destination, message).subscribe();
    }

    public void sendMessagePrivate(Account acc) {
        if (acc != null)
            sendMessage("/app/privatemsg", "{ \"de\": \"" + acc.getEmail() + "\", \"session\": \"" + acc.getSessionId() + "\", \"creationTemps\": 0, \"contenu\": \"\" }");
    }

    public void sendMessagePublic(Account acc) {
        if (acc != null)
            sendMessage("/app/publicmsg", "{ \"de\": \"" + acc.getEmail() + "\", \"session\": \"" + acc.getSessionId() + "\", \"creationTemps\": 0, \"contenu\": \"\" }");
    }

    public void sendChangePlace(Account acc, String position, boolean arbitre) {
        if (acc != null) {
            sendMessage(
                    "/app/lieux",
                    "{ \"courriel\": \"" + acc.getEmail() + "\", " +
                            "\"session\": \"" + acc.getSessionId() + "\", " +
                            "\"position\": \"" + position + "\", " +
                            "\"arbitre\": " + arbitre + " }");
        }
    }

    public void sendFight(FightType type) {
        switch (type) {
            case Red:
                break;
            case White:
                break;
            case Tie:
                break;
        }
    }

    public void sendArbiterRed() {

    }

    public void sendArbiterRedWithFault() {

    }

    public void sendPassExam() {

    }

    public void sendFailExam() {

    }

    public void sendChangeRole() {

    }
}
