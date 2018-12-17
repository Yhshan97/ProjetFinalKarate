package ca.corentinbrunel.web.karatefrontendv3

import android.os.Bundle
import android.service.voice.AlwaysOnHotwordDetector
import android.support.design.internal.NavigationMenuItemView
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account
import ca.corentinbrunel.web.karatefrontendv3.Fragments.ConnectionFragment
import ca.corentinbrunel.web.karatefrontendv3.Fragments.HistoryFragment
import ca.corentinbrunel.web.karatefrontendv3.Fragments.MessagesFragment
import ca.corentinbrunel.web.karatefrontendv3.Fragments.WaitingRoomFragment
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ConnectionFragment.OnConnection {
    /*
     * Variables
     */
    private val httpConnection = HttpConnection()
    private val stompConnection = StompConnection("ws://10.0.2.2:8080/webSocket/websocket")
    private val accounts: LinkedHashMap<String, Account> = linkedMapOf()
    private var current: Account? = null
    private lateinit var currentFragment: Fragment
    private lateinit var connectionFragment: ConnectionFragment
    private val messagesFragment = MessagesFragment()
    private val waitingRoomFragment = WaitingRoomFragment()
    private val historyFragment = HistoryFragment()

    /*
     * Helper functions
     */
    /*private fun showConnectionFragment() {
        val ft = supportFragmentManager.beginTransaction()

        ft.detach(currentFragment)
        ft.show(connectionFragment)
        currentFragment = connectionFragment
        ft.addToBackStack(null)

        ft.commitAllowingStateLoss()
        supportFragmentManager.executePendingTransactions()
    }
    private fun hideConnectionFragment(frag: Fragment?) {
        if (frag != null) {
            val ft = supportFragmentManager.beginTransaction()

            ft.hide(currentFragment)
            ft.attach(frag)
            currentFragment = frag
            ft.addToBackStack(null)

            ft.commitAllowingStateLoss()
            supportFragmentManager.executePendingTransactions()
        }
    }*/
    private fun switchFragment(frag: Fragment?) {
        if (frag != null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.hide(currentFragment)
            ft.show(frag)
            currentFragment = frag
            ft.addToBackStack(null)

            ft.commitAllowingStateLoss()
            supportFragmentManager.executePendingTransactions()
        }
    }

    private fun updateInfoAccount() {
        val currentAccount = current
        if (currentAccount != null) {
            tvEmailAccount.text = currentAccount.email
            imgAccount.setImageBase64WithHead(currentAccount.avatar)
            tvInfoAccount.text = "${currentAccount.fullName}, ${currentAccount.groupe}, ${currentAccount.role}, points: ${currentAccount.points}, credits: ${currentAccount.credits}"
        }
    }

    @Throws(JSONException::class)
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
    }

    private fun updateLstAccounts(updateCurrent: Boolean, start: Boolean) {
        httpConnection.executeRequest(
            "http://10.0.2.2:8080/lstComptes",
            { _, _ -> this@MainActivity.runOnUiThread { Toast.makeText(applicationContext, "La liste des comptes n'a pas pu être retournée", Toast.LENGTH_SHORT).show() } },
            { _, response ->
                try {
                    val res = response.body()?.string()
                    Log.i("LstComptes", res)
                    val jsonObject = JSONObject(res)
                    updateAccountList(jsonObject, updateCurrent)

                    if (start) {
                        connectionFragment = ConnectionFragment()
                        currentFragment = connectionFragment
                        val args = Bundle()
                        args.putStringArrayList(ConnectionFragment.lstEmailsName, java.util.ArrayList(accounts.values.map { acc -> acc.email }))
                        connectionFragment.arguments = args

                        this@MainActivity.runOnUiThread {
                            //changeFragment(connectionFragment)
                            val fm = supportFragmentManager
                            val ft = fm.beginTransaction()

                            ft.add(R.id.frame_content, connectionFragment)
                            ft.add(R.id.frame_content, messagesFragment)
                            ft.add(R.id.frame_content, waitingRoomFragment)
                            ft.add(R.id.frame_content, historyFragment)
                            ft.hide(messagesFragment)
                            ft.hide(waitingRoomFragment)
                            ft.hide(historyFragment)

                            ft.commit()
                        }
                    }

                    this@MainActivity.runOnUiThread {
                        if (waitingRoomFragment.isInit)
                            waitingRoomFragment.updateRecyclerViews()

                        if (updateCurrent)
                            updateInfoAccount()
                    }
                } catch (ex: JSONException) { ex.printStackTrace() }
            }
        )
    }

    private fun resetInfoAccount() {
        tvEmailAccount.setText(R.string.anonymous)
        imgAccount.setImageResource(R.mipmap.ic_launcher_round)
        tvInfoAccount.text = ""
    }

    private fun updateAccountsStomp(payload: String) {
        try {
            val jsonObject = JSONObject(payload)
            updateAccountList(jsonObject, true)

            this@MainActivity.runOnUiThread {
                if (waitingRoomFragment.isInit)
                    waitingRoomFragment.updateRecyclerViews()

                updateInfoAccount()
            }
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
    }

    /*
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_connection)

        messagesFragment.stompConnection = stompConnection
        waitingRoomFragment.stompConnection = stompConnection
        waitingRoomFragment.httpConnection = httpConnection
        waitingRoomFragment.accounts = accounts
        historyFragment.httpConnection = httpConnection

        updateLstAccounts(false, true)

        stompConnection.subLstComptes(Consumer { updateAccountsStomp(it.payload) })
    }

    /*
     * Navigation Drawer
     */
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val fragment: Fragment? = when (item.itemId) {
            R.id.nav_connection -> connectionFragment
            R.id.nav_messages -> messagesFragment
            R.id.nav_waiting_room -> waitingRoomFragment
            R.id.nav_history -> {
                historyFragment.onShow()
                historyFragment
            }
            else -> null
        }

        if (fragment != null)
            switchFragment(fragment)

        drawer_layout.closeDrawer(GravityCompat.START)
        // Make sure that the fragment exists to have the option checked
        return fragment != null
    }

    /*
     * Connection (ConnectionFragment)
     */
    override fun onConnect(email: String) {
        Log.i("OnConnect", email)

        httpConnection.executeRequest(
            "http://10.0.2.2:8080/login/$email",
            { _, _ -> this@MainActivity.runOnUiThread { Toast.makeText(applicationContext, "Le compte n'a pas pu être connecté", Toast.LENGTH_SHORT).show() } },
            { _, response ->
                val res = response.body()?.string()

                val newCurrent = accounts[email]!!
                newCurrent.sessionId = res

                current = newCurrent

                waitingRoomFragment.current = newCurrent
                historyFragment.current = newCurrent
                messagesFragment.current = newCurrent
                historyFragment.current = newCurrent
                //historyFragment.onShow()

                this@MainActivity.runOnUiThread {
                    nav_view.menu.findItem(R.id.nav_history).isEnabled = true

                    updateInfoAccount()
                }
            }
        )

    }

    override fun onDisconnect(email: String) {
        Log.i("OnDisconnect", email)

        httpConnection.executeRequest(
            "http://10.0.2.2:8080/logout/${current?.sessionId}",
            { _, _ -> this@MainActivity.runOnUiThread { Toast.makeText(applicationContext, "Le compte n'a pas pu être déconnecté", Toast.LENGTH_SHORT).show() }},
            { _, _ ->
                current = null

                waitingRoomFragment.current = null
                historyFragment.current = null
                messagesFragment.current = null
                historyFragment.current = null

                this@MainActivity.runOnUiThread {
                    nav_view.menu.findItem(R.id.nav_history).isEnabled = false

                    resetInfoAccount()
                    messagesFragment.resetMessages()
                }
            }
        )
    }
}
