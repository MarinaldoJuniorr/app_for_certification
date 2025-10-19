package com.example.app_for_certification.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.usercase.CodeUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountryDetailViewModel(
    private val useCases: CodeUseCases
) : ViewModel() {

    private val _state = MutableLiveData<CountryUiState<CountryDomain>>(CountryUiState.Empty)
    val state: LiveData<CountryUiState<CountryDomain>> = _state

    fun load(code: String) {
        val sanitized = code.filter { it.isLetter() }.take(3).uppercase()
        if (sanitized.length !in 2..3) {
            _state.value = CountryUiState.Error("Invalid country code.")
            return
        }

        _state.value = CountryUiState.Loading
        viewModelScope.launch {
            try {
                val country = withContext(Dispatchers.IO) { useCases.fetchByCode(sanitized) }
                _state.value = CountryUiState.Success(country)
            } catch (e: Throwable) {
                val msg = e.message?.lowercase().orEmpty()
                val pretty = when {
                    "400" in msg || "invalid" in msg -> "Invalid country code."
                    "404" in msg || "not found" in msg -> "Country not found."
                    "unable to resolve host" in msg -> "No internet connection."
                    "timeout" in msg -> "Request timed out."
                    else -> e.message ?: "Unknown error"
                }
                _state.value = CountryUiState.Error(pretty)
            }
        }
    }
}

