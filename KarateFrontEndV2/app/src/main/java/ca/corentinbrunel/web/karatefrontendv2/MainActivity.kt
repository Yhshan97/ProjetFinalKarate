package ca.corentinbrunel.web.karatefrontendv2

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import ca.corentinbrunel.web.karatefrontendv2.Entities.Account
import ca.corentinbrunel.web.karatefrontendv2.Fragments.ConnectionFragment
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONException
import org.json.JSONObject
import ua.naiksoftware.stomp.client.StompMessage


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener/*, ConnectionFragment.OnConnection*/ {
    //private val httpConnection = HttpConnection()
    private val stompConnection = StompConnection("ws://10.0.2.2:8080/webSocket/websocket")
    /*private val accounts: LinkedHashMap<String, Account> = linkedMapOf()
    private var current: Account? = null
    private lateinit var connectionFragment: ConnectionFragment*/

    /*
     * Helper functions
     */
    /*private fun changeFragment(frag: Fragment?) {
        if (frag != null) {
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()

            ft.replace(R.id.frame_content, frag)

            ft.commit()
        }
    }*/

    /*@Throws(JSONException::class)
    private fun updateAccountList(jsonObject: JSONObject, updateCurrent: Boolean) {
        val arrComptes = jsonObject.getJSONArray("comptes")

        for (i in 0 until arrComptes.length()) {
            val obj = arrComptes.getJSONObject(i)
            val acc = Account(
                obj.getString("courriel"),
                obj.getString("fullName"),
                obj.getString("avatar"),
                obj.getString("role"),
                obj.getString("groupe"),
                obj.getInt("points"),
                obj.getInt("credits")
            )

            var currentAccount = current
            if (currentAccount != null && updateCurrent && acc.email == currentAccount.email) {
                val sessionId = currentAccount.sessionId
                currentAccount = acc
                currentAccount.sessionId = sessionId

                current = currentAccount
            }

            accounts[acc.email] = acc
        }
    }*/

    /*private fun updateLstAccounts(updateCurrent: Boolean) {
        httpConnection.executeRequest(
            "http://10.0.2.2:8080/lstComptes",
            { _, _ -> this@MainActivity.runOnUiThread { Toast.makeText(applicationContext, "La liste des comptes n'a pas pu être retournée", Toast.LENGTH_SHORT).show() } },
            { _, response ->
                try {
                    val res = response.body()?.string()
                    Log.i("LstComptes", res)
                    val jsonObject = JSONObject(res)
                    updateAccountList(jsonObject, updateCurrent)

                    connectionFragment = ConnectionFragment()
                    val args = Bundle()
                    args.putStringArrayList(ConnectionFragment.lstEmailsName, java.util.ArrayList(accounts.values.map { acc -> acc.email }))
                    connectionFragment.arguments = args

                    this@MainActivity.runOnUiThread {
                        changeFragment(connectionFragment)
                    }
                } catch (ex: JSONException) { ex.printStackTrace() }
            }
        )
    }*/

    /*private fun lockDrawer(state: Boolean) {
        val mode = if (state) { DrawerLayout.LOCK_MODE_LOCKED_CLOSED } else { DrawerLayout.LOCK_MODE_UNLOCKED }

        drawer_layout.setDrawerLockMode(mode)
    }*/

    /*private fun updateInfoAccount() {
        val currentAccount = current
        if (currentAccount != null) {
            tvEmailAccount.text = currentAccount.email
            imgAccount.setImageBase64WithHead(currentAccount.avatar)
            tvInfoAccount.text = "${currentAccount.fullName}, ${currentAccount.groupe}, ${currentAccount.role}, points: ${currentAccount.points}, credits: ${currentAccount.credits}"
        }
    }*/

    /*
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val test = StompConnection("ws://10.0.2.2:8080/webSocket/websocket")
        test.subReponsePublique(object: Consumer<StompMessage> {
            override fun accept(t: StompMessage?) {
                val haha = t?.payload
                Log.i("TEST", haha)
            }
        })*/

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        //lockDrawer(true)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        // Set connection as default item
        nav_view.setCheckedItem(R.id.nav_connection)

        //updateLstAccounts(false)
    }

    /*
     * Navigation drawer
     */
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        /*val fragment: Fragment? = when (item.itemId) {
            R.id.nav_connection -> connectionFragment
            else -> null
        }

        changeFragment(fragment)*/

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /*
     * Connection (ConnectionFragment)
     */
    /*override fun onConnect(email: String) {
        Log.i("OnConnect", email)

        lockDrawer(false)
        httpConnection.executeRequest(
            "http://10.0.2.2:8080/login/$email",
            { _, _ -> this@MainActivity.runOnUiThread { Toast.makeText(applicationContext, "Le compte n'a pas pu être connecté", Toast.LENGTH_SHORT).show() } },
            { _, response ->
                val res = response.body()?.string()

                val newCurrent = accounts[email]!!
                newCurrent.sessionId = res

                current = newCurrent

                this@MainActivity.runOnUiThread { updateInfoAccount() }
            }
        )

    }

    override fun onDisconnect(email: String) {
        Log.i("OnDisconnect", email)

        lockDrawer(true)

        httpConnection.executeRequest(
            "http://10.0.2.2:8080/logout/${current?.sessionId}",
            { _, _ -> this@MainActivity.runOnUiThread { Toast.makeText(applicationContext, "Le compte n'a pas pu être déconnecté", Toast.LENGTH_SHORT).show() }},
            { _, _ -> current = null }
        )
    }*/

}
