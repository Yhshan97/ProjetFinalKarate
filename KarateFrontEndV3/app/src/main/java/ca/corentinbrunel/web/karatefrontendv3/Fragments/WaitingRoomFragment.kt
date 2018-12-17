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
import ca.corentinbrunel.web.karatefrontendv3.setImageBase64WithHead
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_waiting_room.*
import org.json.JSONException
import org.json.JSONObject


class WaitingRoomFragment : Fragment() {
    private data class EntityFight(val email: String, val avatar: String)

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

    private var oldPosition = ""

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

        stompConnection.subInfoCombat(Consumer {
            val res = it.payload

            Log.i("StompInfoCombat", res)

            val jsonObject = JSONObject(res)
            if (jsonObject.getString("gaucheNom") != "null") {
                val left = EntityFight(jsonObject.getString("gaucheNom"), jsonObject.getString("gaucheAvatar"))
                val right = EntityFight(jsonObject.getString("droiteNom"), jsonObject.getString("droiteAvatar"))
                val arbitrator = EntityFight(jsonObject.getString("arbitreNom"), jsonObject.getString("arbitreAvatar"))

                activity?.runOnUiThread {
                    val curAcc = current
                    if (curAcc != null) {
                        when (curAcc.email) {
                            left.email ->
                                imgLeft.setBackgroundResource(R.drawable.image_border)
                            arbitrator.email ->
                                imgArbitrator.setBackgroundResource(R.drawable.image_border)
                            right.email ->
                                imgRight.setBackgroundResource(R.drawable.image_border)
                        }
                    }

                    imgLeft.setImageBase64WithHead(left.avatar)
                    imgArbitrator.setImageBase64WithHead(arbitrator.avatar)
                    imgRight.setImageBase64WithHead(right.avatar)
                }
            } else {
                activity?.runOnUiThread {
                    imgLeft.setImageResource(R.drawable.ic_launcher_background)
                    imgArbitrator.setImageResource(R.drawable.ic_launcher_background)
                    imgRight.setImageResource(R.drawable.ic_launcher_background)
                    imgLeft.background = null
                    imgArbitrator.background = null
                    imgRight.background = null

                    imgLeftAttack.setImageBitmap(null)
                    imgRightAttack.setImageBitmap(null)
                }
            }
        })

        stompConnection.subAttacks(Consumer {
            val res = it.payload
            Log.i("StompAttacks", res)

            val jsonObject = JSONObject(res)
            val left = jsonObject.getInt("attaqueGauche")
            val right = jsonObject.getInt("attaqueDroite")
            val chooseTextId: (Int) -> Int = { id ->
                when (id) {
                    0 -> R.mipmap.roche_layer
                    1 -> R.mipmap.papier_layer
                    2 -> R.mipmap.ciseaux_layer
                    else -> 0
                }
            }

            activity?.runOnUiThread {
                imgLeftAttack.setImageResource(chooseTextId(left))
                imgLeftAttack.rotationY = 180F
                imgRightAttack.setImageResource(chooseTextId(right))
            }
        })

        stompConnection.subResultFight(Consumer {
            val res = it.payload
            Log.i("StompResultFight", res)
            val jsonObject = JSONObject(res)
            val result = jsonObject.getString("result")
            activity?.runOnUiThread {
                imgLeftAttack.rotationY = 0F
                imgRightAttack.rotationY = 0F
                when (result) {
                    "gauche" -> {
                        imgLeftAttack.setImageResource(R.mipmap.gauche_layer)
                        imgRightAttack.setImageBitmap(null)
                    }
                    "droite" -> {
                        imgLeftAttack.setImageBitmap(null)
                        imgRightAttack.setImageResource(R.mipmap.droite_layer)
                    }
                    "draw" -> {
                        imgLeftAttack.setImageResource(R.mipmap.gauche_layer)
                        imgRightAttack.setImageResource(R.mipmap.droite_layer)
                    }
                }
            }
        })

        /*
         * Buttons and click events
         */
        cbArbitrator.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                oldPosition = when {
                    rbElsewhere.isChecked -> "ailleurs"
                    rbSpectator.isChecked -> "spectateur"
                    rbWaiting.isChecked -> "attente"
                    else -> "attente"
                }
                stompConnection.sendChangePlace(current, "attente", true)
            }
            else {
                stompConnection.sendChangePlace(current, oldPosition, false)
                //rbWaiting.isChecked = true
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

        isInit = true
    }

}
