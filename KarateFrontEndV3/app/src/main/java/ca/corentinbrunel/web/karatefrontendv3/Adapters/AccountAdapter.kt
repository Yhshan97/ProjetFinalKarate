package ca.corentinbrunel.web.karatefrontendv3.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account
import ca.corentinbrunel.web.karatefrontendv3.R
import ca.corentinbrunel.web.karatefrontendv3.setImageBase64WithHead

class AccountAdapter(private val accounts: ArrayList<Account>) : RecyclerView.Adapter<AccountAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.imgAccountImage)
        var info: TextView = view.findViewById(R.id.tvAccountInfos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.i("RecyclerView", "onBindViewHolder: $position")
        val acc = accounts[position]

        holder.image.setImageBase64WithHead(acc.avatar)
        holder.info.text = "${acc.fullName}, ${acc.groupe}, ${acc.role}"
    }

    override fun getItemCount(): Int {
        return accounts.size
    }
}
