package com.example.app_for_certification.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.usercase.CodeUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountryViewModel(
    private val useCases: CodeUseCases
) : ViewModel() {

    private val _state = MutableLiveData<CountryUiState<List<CountryDomain>>>(CountryUiState.Empty)
    val state: LiveData<CountryUiState<List<CountryDomain>>> = _state

    private var searchJob: Job? = null
    private var lastQuery: String = ""

    fun load(force: Boolean = false) {
        if (!force && _state.value is CountryUiState.Success && lastQuery.isBlank()) return

        _state.value = CountryUiState.Loading
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) { useCases.fetchAll() }
                _state.value = if (list.isNullOrEmpty()) CountryUiState.Empty
                else CountryUiState.Success(list)
                lastQuery = ""
            } catch (_: CancellationException) {
            } catch (e: Throwable) {
                _state.value = CountryUiState.Error(e.userFriendlyMessage())
            }
        }
    }

    fun search(rawQuery: String) {
        val query = rawQuery.trim().replace(Regex("\\s+"), " ")
        if (query.isBlank()) { load(force = true); return }
        if (query == lastQuery && _state.value is CountryUiState.Success) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = CountryUiState.Loading
            delay(300)

            try {
                val list = withContext(Dispatchers.IO) { useCases.fetchSearch(query) }
                _state.value = if (list.isNullOrEmpty()) CountryUiState.Empty
                else CountryUiState.Success(list)
                lastQuery = query
            } catch (_: CancellationException) {
                // ignorado
            } catch (e: Throwable) {
                _state.value = CountryUiState.Error(e.userFriendlyMessage())
            }
        }
    }

    fun refresh() {
        if (lastQuery.isBlank()) load(force = true) else search(lastQuery)
    }

    private fun Throwable.userFriendlyMessage(): String {
        val msg = message?.takeIf { it.isNotBlank() } ?: return "Unknown error"
        return when {
            "timeout" in msg.lowercase() -> "Request timed out. Please try again."
            "unable to resolve host" in msg.lowercase() -> "No internet connection."
            else -> msg
        }
    }
}