package com.example.app_for_certification.presentation.model

sealed class CountryUiState<out T> {
    data object Loading : CountryUiState<Nothing>()
    data object Empty : CountryUiState<Nothing>()
    data class Success<T>(val data: T, val offline: Boolean = false) : CountryUiState<T>()
    data class Error(val message: String, val offline: Boolean = false) : CountryUiState<Nothing>()
}