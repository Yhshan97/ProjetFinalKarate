package ca.corentinbrunel.web.karatefrontendv3.Fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ca.corentinbrunel.web.karatefrontendv3.R
import kotlinx.android.synthetic.main.fragment_connection.*

class ConnectionFragment : Fragment() {
    companion object {
        const val lstEmailsName = "lstEmailsComptes"
    }

    interface OnConnection {
        fun onConnect(email: String)
        fun onDisconnect(email: String)
    }

    private lateinit var iConnection: OnConnection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment this.arguments
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerEmails: Spinner = view.findViewById(R.id.spinAccounts)

        val args = this.arguments
        if (args != null) {
            val lstEmails = args.getStringArrayList(lstEmailsName)!!
            spinnerEmails.adapter = ArrayAdapter(context!!,
                R.layout.support_simple_spinner_dropdown_item, lstEmails)
        }

        view.findViewById<Button>(R.id.btnConnection).setOnClickListener {
            val selectedEmail = spinnerEmails.selectedItem.toString()

            if (btnConnection.text == resources.getText(R.string.connection_open)) {
                iConnection.onConnect(selectedEmail)

                btnConnection.setText(R.string.connection_close)
                spinnerEmails.isEnabled = false
            }
            else {
                iConnection.onDisconnect(selectedEmail)

                btnConnection.setText(R.string.connection_open)
                spinnerEmails.isEnabled = true
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        iConnection = context as OnConnection
    }
}

