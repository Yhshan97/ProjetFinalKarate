package ca.corentinbrunel.web.karatefrontendv3.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account
import ca.corentinbrunel.web.karatefrontendv3.HttpConnection

import ca.corentinbrunel.web.karatefrontendv3.R
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment() {
    lateinit var httpConnection: HttpConnection
    var current: Account? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //tvHistory.text = "allo\nsal\t\t\tut"
    }

    fun onShow() {
        val curAcc = current
        if (curAcc != null) {
            httpConnection.executeRequest(
                "http://10.0.2.2:8080/lstExamens/${curAcc.email}/${curAcc.sessionId}",
                { _, _ -> activity?.runOnUiThread { tvHistory.text = "Impossible de recevoir l'historique" }},
                { _, response ->
                    val res = response.body()?.string()
                    Log.i("ReceiveLstExamens", res)
                    activity?.runOnUiThread { tvHistory.text = res }
                }
            )
        }
    }

}
