package com.wherever.mobile.core.network

import android.util.Log
import com.wherever.mobile.BuildConfig
import javax.inject.Inject

interface HttpLogger {
    fun log(message: String)
}

class AndroidHttpLogger @Inject constructor() : HttpLogger {
    override fun log(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("WhereverHttp", message)
        }
    }
}
