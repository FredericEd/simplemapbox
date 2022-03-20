package com.fveloz.mapdirections.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import com.fveloz.mapdirections.MapApplication

class NetworkUtils {
    companion object{
        fun isConnected(context: Context? = null): Boolean {
            val connectivityManager = MapApplication.getAppContext().getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
}