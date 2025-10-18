package com.example.app_for_certification.presentation.model

sealed class CountryUiState<out T> {
    object Loading : CountryUiState<Nothing>()
    object Empty : CountryUiState<Nothing>()
    data class Success<T>(val data: T) : CountryUiState<T>()
    data class Error(val message: String) : CountryUiState<Nothing>()
}