package com.example.app_for_certification.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.usercase.CodeUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CountryDetailViewModel(
    private val useCases: CodeUseCases
) : ViewModel() {

    private val _state = MutableLiveData<CountryUiState<CountryDomain>>(CountryUiState.Loading)
    val state: LiveData<CountryUiState<CountryDomain>> = _state

    fun load(code: String) {
        _state.value = CountryUiState.Loading
        viewModelScope.launch {
            try {
                val country = withContext(Dispatchers.IO) { useCases.fetchByCode(code) }
                _state.value = CountryUiState.Success(country)
            } catch (_: CancellationException) {
            } catch (e: Throwable) {
                val offline = e.isOffline()
                _state.value = CountryUiState.Error(
                    message = e.userFriendlyMessage(offline),
                    offline = offline
                )
            }
        }
    }

    private fun Throwable.isOffline(): Boolean {
        if (this is CancellationException) return false
        if (this is UnknownHostException) return true
        if (this is SocketTimeoutException) return false
        if (this is IOException) {
            val msg = (message ?: "").lowercase()
            return "unable to resolve host" in msg ||
                    "failed to connect" in msg ||
                    "no internet" in msg ||
                    "network is unreachable" in msg
        }
        val msg = (message ?: "").lowercase()
        return "unable to resolve host" in msg || "no internet" in msg
    }

    private fun Throwable.userFriendlyMessage(offline: Boolean): String {
        if (offline) return "No internet connection."
        val msg = message?.takeIf { it.isNotBlank() } ?: return "Unexpected error"
        return if ("timeout" in msg.lowercase()) "Request timed out. Please try again." else msg
    }
}