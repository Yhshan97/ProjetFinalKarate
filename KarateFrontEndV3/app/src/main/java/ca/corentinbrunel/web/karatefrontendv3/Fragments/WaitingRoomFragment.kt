package ca.corentinbrunel.web.karatefrontendv3.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account
import ca.corentinbrunel.web.karatefrontendv3.Helper.RecyclerViewHelper
import ca.corentinbrunel.web.karatefrontendv3.HttpConnection
import ca.corentinbrunel.web.karatefrontendv3.R
import ca.corentinbrunel.web.karatefrontendv3.StompConnection
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_waiting_room.*
import org.json.JSONException
import org.json.JSONObject

class WaitingRoomFragment : Fragment() {
    lateinit var httpConnection: HttpConnection
    lateinit var stompConnection: StompConnection
    lateinit var accounts: LinkedHashMap<String, Account>
    var current: Account? = null

    var isInit = false

    private val emailLieu: HashMap<String, String> = hashMapOf()
    private lateinit var helperArbitrators: RecyclerViewHelper
    private lateinit var helperElsewhere: RecyclerViewHelper
    private lateinit var helperSpectating: RecyclerViewHelper
    private lateinit var helperWaiting: RecyclerViewHelper

    fun updateRecyclerViews() {
        helperArbitrators.clear()
        helperElsewhere.clear()
        helperSpectating.clear()
        helperWaiting.clear()

        emailLieu.forEach { email, lieu ->
            when (lieu) {
                "arbitre" -> helperArbitrators.add(accounts[email]!!)
                "ailleurs" -> helperElsewhere.add(accounts[email]!!)
                "spectateur" -> helperSpectating.add(accounts[email]!!)
                "attente" -> helperWaiting.add(accounts[email]!!)
            }
        }

        helperArbitrators.update()
        helperElsewhere.update()
        helperSpectating.update()
        helperWaiting.update()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waiting_room, container, false)
    }

    private fun simpleStompCurrent(dest: String) {
        val curAcc = current
        if (curAcc != null)
            httpConnection.executeRequest(
                "http://10.0.2.2:8080/$dest/${curAcc.email}/${curAcc.sessionId}",
                { _, _ -> activity?.runOnUiThread { Toast.makeText(activity, "$dest impossible", Toast.LENGTH_SHORT).show() }},
                { _, response ->
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "$dest: ${response.body()?.string()}", Toast.LENGTH_SHORT).show()
                    }

                    if (dest == "examen1")
                        stompConnection.sendGetLstComptes()
                }
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
         * Setup
         */
        helperArbitrators = RecyclerViewHelper(rvArbitrators)
        helperElsewhere = RecyclerViewHelper(rvElsewhere)
        helperSpectating = RecyclerViewHelper(rvSpectators)
        helperWaiting = RecyclerViewHelper(rvWaiting)

        helperArbitrators.setUp(activity!!)
        helperElsewhere.setUp(activity!!)
        helperSpectating.setUp(activity!!)
        helperWaiting.setUp(activity!!)

        /*
         * Subscribes
         */
        stompConnection.subChangePlace(Consumer {
            val res = it.payload

            Log.i("StompChangePlace", res)
            emailLieu.clear()
            val jsonObject = JSONObject(res)
            jsonObject.keys().forEachRemaining { key ->
                try {
                    val place = jsonObject.getString(key)
                    emailLieu[key] = place
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                }
            }

            if (accounts.size > 0) {
                activity?.runOnUiThread { updateRecyclerViews() }
            }
        })

        /*
         * Buttons and click events
         */
        cbArbitrator.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                stompConnection.sendChangePlace(current, "attente", true)
            else {
                stompConnection.sendChangePlace(current, "attente", false)
                rbWaiting.isChecked = true
            }
        }

        rgPlace.setOnCheckedChangeListener { _, checkedId ->
            Log.i("PlaceChange", "Place changed for ${current?.fullName}")
            when (checkedId) {
                R.id.rbElsewhere -> stompConnection.sendChangePlace(current, "ailleurs", false)
                R.id.rbSpectator -> stompConnection.sendChangePlace(current, "spectateur", false)
                R.id.rbWaiting -> stompConnection.sendChangePlace(current, "attente", false)
            }
        }

        btnFightRed.setOnClickListener {
            Log.i("Fight", "Red fight")
            simpleStompCurrent("combat1")
        }
        btnFightWhite.setOnClickListener {
            Log.i("Fight", "White fight")
            simpleStompCurrent("combat2")
        }
        btnFightTie.setOnClickListener {
            Log.i("Fight", "White fight")
            simpleStompCurrent("combat3")
        }
        btnArbiterRed.setOnClickListener {
            Log.i("Arbiter", "Arbiter red")
            simpleStompCurrent("arbitrer1")
        }
        btnArbiterRedFault.setOnClickListener {
            Log.i("Arbitrer", "Arbiter red with fault")
            simpleStompCurrent("arbitrer2")
        }
        btnPassExam.setOnClickListener {
            Log.i("Exam", "Pass exam")
            simpleStompCurrent("examen1")
        }
        btnFailExam.setOnClickListener {
            Log.i("Exam", "Fail exam")
            simpleStompCurrent("examen2")
        }
        btnChangeRole.setOnClickListener {
            Log.i("Role", "Change role")
            simpleStompCurrent("passage")
        }

        isInit = true
    }

}
