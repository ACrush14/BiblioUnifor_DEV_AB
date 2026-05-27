package com.example.bibliounifornew.data

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class EmailRepository {

    private val client = OkHttpClient()

    fun enviarCodigo(
        email: String,
        codigo: String,
        callback: (Boolean) -> Unit
    ) {

        val json = JSONObject().apply {

            put(
                "service_id",
                "service_idwdglm"
            )

            put(
                "template_id",
                "template_q7kedmq"
            )

            put(
                "user_id",
                "zeW0gsboIU8rFSNG6"
            )

            put(
                "accessToken",
                "rmApdJh0IO-eTpbixHFHz"
            )

            put(
                "template_params",
                JSONObject().apply {

                    put(
                        "email",
                        email
                    )

                    put(
                        "passcode",
                        codigo
                    )
                }
            )
        }


        Log.d(
            "EMAILJS",
            "JSON=$json"
        )


        val body =
            json.toString()
                .toRequestBody(
                    "application/json"
                        .toMediaType()
                )


        val request =
            Request.Builder()

                .url(
                    "https://api.emailjs.com/api/v1.0/email/send"
                )

                .addHeader(
                    "Content-Type",
                    "application/json"
                )

                .addHeader(
                    "origin",
                    "http://localhost"
                )

                .post(body)

                .build()



        client.newCall(request)

            .enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    Log.e(
                        "EMAILJS",
                        "ERRO=${e.message}"
                    )

                    callback(false)
                }



                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    val texto =
                        response.body?.string()

                    Log.d(
                        "EMAILJS",
                        "STATUS=${response.code}"
                    )

                    Log.d(
                        "EMAILJS",
                        "BODY=$texto"
                    )

                    callback(
                        response.isSuccessful
                    )

                    response.close()
                }
            })
    }
}