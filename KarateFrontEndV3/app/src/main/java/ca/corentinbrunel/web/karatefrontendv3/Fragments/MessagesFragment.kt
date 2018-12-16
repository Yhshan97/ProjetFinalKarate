package ca.corentinbrunel.web.karatefrontendv3.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account
import ca.corentinbrunel.web.karatefrontendv3.R
import ca.corentinbrunel.web.karatefrontendv3.StompConnection
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_messages.*
import org.json.JSONObject
import java.util.*




class MessagesFragment : Fragment() {
    lateinit var stompConnection: StompConnection
    var current: Account? = null

    fun resetMessages() {
        tvPrivateMessage.setText(R.string.message_private)
        tvPublicMessage.setText(R.string.message_public)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
         * Subscribe
         */
        stompConnection.subReponsePublique(Consumer {
            val res = it.payload
            Log.i("MessagePublic", res)
            val rep = JSONObject(res)
            val creation = Date(rep.getLong("creationTemps"))
            activity?.runOnUiThread {
                tvPublicMessage.setText(rep.getString("de") + ", " + creation.toString() + ", " + rep.getString("contenu"))
            }
        })

        stompConnection.subReponsePrive(Consumer {
            val res = it.payload
            Log.i("MessagePrivate", res)
            if (current != null) {
                val rep = JSONObject(res)
                val creation = Date(rep.getLong("creationTemps"))
                activity?.runOnUiThread {
                    tvPrivateMessage.setText(rep.getString("de") + ", " + creation.toString() + ", " + rep.getString("contenu"))
                }
            }
        })

        btnPublicMessage.setOnClickListener { stompConnection.sendMessagePublic(current) }
        btnPrivateMessage.setOnClickListener { stompConnection.sendMessagePrivate(current) }
    }
}
