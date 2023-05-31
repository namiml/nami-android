package com.namiml.app.test

import android.content.Context
import java.io.InputStream

fun readInitialConfigFromAsset(context: Context): String? {
    val jsonString = try {
        val inputStream: InputStream = context.assets.open("nami_initial_config.json")
        inputStream.bufferedReader().use { it.readText() }
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
    return jsonString
}