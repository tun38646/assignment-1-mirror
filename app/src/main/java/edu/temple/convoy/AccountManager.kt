package edu.temple.convoy

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.coroutineContext

class AccountManager(_context: Context) {

    private val url = "https://kamorris.com/lab/convoy/account.php"
    private val context = _context

    fun register(username: String, firstName: String, lastName: String, password: String) {
        val postData = JSONObject().apply {
            put("action", "REGISTER")
            put("username", username)
            put("firstname", firstName)
            put("lastname", lastName)
            put("password", password)
        }

        Log.d("AccountManager.register", postData.toString())
        sendRequest(postData)
    }

    fun login(username: String, password: String) {
        val postData = JSONObject().apply {
            put("action", "LOGIN")
            put("username", username)
            put("password", password)
        }

        Log.d("AccountManager.login", postData.toString())
        sendRequest(postData)
    }

    fun logout(username: String, sessionKey: String) {
        val postData = JSONObject().apply {
            put("action", "LOGOUT")
            put("username", username)
            put("session_key", sessionKey)
        }

        Log.d("AccountManager.logout", postData.toString())
        sendRequest(postData)
    }
    private fun sendRequest(postData: JSONObject) {

        val queue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, postData,
            { response ->
                Log.d("AccountManager.sendRequest", response.toString())
            },
            { error ->
                Log.e("AccountManager.sendRequest", error.message.toString())
            }
        )

        queue.add(jsonObjectRequest)

    }

}
