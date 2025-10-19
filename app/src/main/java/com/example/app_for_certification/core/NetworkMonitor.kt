package com.example.app_for_certification.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkMonitor(context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(isConnected())
    val isOnline: StateFlow<Boolean> = _isOnline

    private val cb = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = isConnected()
        }

        override fun onUnavailable() {
            _isOnline.value = isConnected()
        }
    }

    fun start() {
        cm.registerDefaultNetworkCallback(cb)
        _isOnline.value = isConnected()
    }

    fun stop() {
        runCatching { cm.unregisterNetworkCallback(cb) }
    }

    private fun isConnected(): Boolean {
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}




