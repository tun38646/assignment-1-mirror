package edu.temple.convoy

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ConvoyManager(_context: Context) {

    private val url = "https://kamorris.com/lab/convoy/convoy.php"
    private val context = _context

    fun create(username: String, sessionKey: String) {
        val postData = JSONObject().apply {
            put("action", "CREATE")
            put("username", username)
            put("session_key", sessionKey)
        }

        Log.d("ConvoyManager.create", postData.toString())
        sendRequest(postData)
    }

    fun end(username: String, sessionKey: String, convoyId: String) {
        val postData = JSONObject().apply {
            put("action", "END")
            put("username", username)
            put("session_key", sessionKey)
            put("convoy_id", convoyId)
        }

        Log.d("ConvoyManager.end", postData.toString())
        sendRequest(postData)
    }

    fun query(username: String, sessionKey: String) {
        val postData = JSONObject().apply {
            put("action", "QUERY")
            put("username", username)
            put("session_key", sessionKey)
        }

        Log.d("ConvoyManager.query", postData.toString())
        sendRequest(postData)
    }
    private fun sendRequest(postData: JSONObject) {
        val queue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, postData,
            { response ->
                Log.d("ConvoyManager.sendRequest", response.toString())
            },
            { error ->
                Log.e("ConvoyManager.sendRequest", error.message.toString())
            }
        )

        queue.add(jsonObjectRequest)

    }

}