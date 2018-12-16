package ca.corentinbrunel.web.karatefrontendv3

import okhttp3.*
import java.io.IOException

class HttpConnection {
    private val httpConnection: OkHttpClient = OkHttpClient()

    fun executeRequest(url: String, onRequestFailure: (Call, IOException) -> Unit, onRequestResponse: (Call, Response) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        httpConnection.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onRequestFailure(call, e)
            }

            override fun onResponse(call: Call, response: Response) = onRequestResponse(call, response)
        })
    }
}