package ca.corentinbrunel.web.karatefrontendv3.Helper

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import ca.corentinbrunel.web.karatefrontendv3.Adapters.AccountAdapter
import ca.corentinbrunel.web.karatefrontendv3.Entities.Account

class RecyclerViewHelper(
    val rv: RecyclerView
) {
    val list: ArrayList<Account> = arrayListOf()
    val adapter: AccountAdapter = AccountAdapter(list)

    fun clear() = list.clear()

    fun add(acc: Account) = list.add(acc)

    fun update() = adapter.notifyDataSetChanged()

    fun setUp(context: Context) {
        val lm: RecyclerView.LayoutManager = LinearLayoutManager(context)
        rv.layoutManager = lm
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = adapter
    }
}