package com.example.app_for_certification.domain.model

data class CountryDomain(
    val name: String,
    val code: String,
    val region: String,
    val capital: String,
    val population: Long,
    val flagUrl: String,
    val languages: List<String>,
    val currencies: List<String>,
    val nationality: String
)