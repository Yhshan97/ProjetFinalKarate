package ca.corentinbrunel.web.karatefrontendv3

import android.util.Log
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account
import ua.naiksoftware.stomp.client.StompMessage
import io.reactivex.functions.Consumer
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.client.StompClient

class StompConnection(url:String) {
    private val client:StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

    init {
        client.connect()
    }

    /*
     * Subscibe
     */
    private fun subTopic(destination:String, event: Consumer<StompMessage>) {
        Log.i("STOMP", "Subscribing to $destination")
        val dis = client.topic(destination).subscribe(event)
    }

    fun subReponsePublique(event:Consumer<StompMessage>) = subTopic("/sujet/reponsepublique", event)

    fun subReponsePrive(event:Consumer<StompMessage>) = subTopic("/sujet/reponseprive", event)

    fun subChangePlace(event:Consumer<StompMessage>) = subTopic("/sujet/lstLieux", event)

    fun subMAJCompte(event:Consumer<StompMessage>) = subTopic("/sujet/MAJCompte", event)

    fun subLstComptes(event:Consumer<StompMessage>) = subTopic("/sujet/lstComptes", event)

    fun subInfoCombat(event: Consumer<StompMessage>) = subTopic("/sujet/infoCombat", event)

    fun subAttacks(event: Consumer<StompMessage>) = subTopic("/sujet/ChoixCombat", event)

    fun subResultFight(event: Consumer<StompMessage>) = subTopic("/sujet/resultCombat", event)

    /*
     * Send
     */
    private fun sendMessage(destination:String, message:String) {
        Log.i("STOMP", "Sending message to $destination")
        client.send(destination, message).subscribe()
    }

    private fun jsonMessage(acc: Account): String = "{ " +
            "\"de\": \"${acc.email}\", " +
            "\"session\": \"${acc.sessionId}\", " +
            "\"creationTemps\": 0, " +
            "\"contenu\": \"\" }"

    fun sendMessagePrivate(acc: Account?) {
        if (acc != null)
            sendMessage("/app/privatemsg", jsonMessage(acc))
    }

    fun sendMessagePublic(acc:Account?) {
        if (acc != null)
            sendMessage("/app/publicmsg", jsonMessage(acc))
    }

    fun sendChangePlace(acc:Account?, position:String, arbitre:Boolean) {
        if (acc != null)
            sendMessage(
                "/app/lieux",
                "{ \"courriel\": \"${acc.email}\", " +
                        "\"session\": \"${acc.sessionId}\", " +
                        "\"position\": \"$position\", " +
                        "\"arbitre\": \"$arbitre\" }"
            )
    }

    fun sendGetLstComptes() {
        Log.i("STOMP", "Sending message to /app/getLstComptes")
        client.send("/app/getLstComptes").subscribe()
    }
}
