package com.example.app_for_certification.data.model

data class CountryDto(
    val name: NameDto? = null,
    val cca2: String? = null,
    val cca3: String? = null,
    val region: String? = null,
    val capital: List<String>? = null,
    val population: Long? = null,
    val flags: FlagsDto? = null,
    val languages: Map<String, String>? = null,
    val currencies: Map<String, CurrencyDto>? = null,
    val demonyms: DemonymsDto? = null
)

data class NameDto(val common: String? = null)
data class FlagsDto(val png: String? = null, val svg: String? = null)
data class CurrencyDto(val name: String? = null, val symbol: String? = null)
data class DemonymsDto(val eng: DemonymGenderDto? = null)
data class DemonymGenderDto(val m: String? = null, val f: String? = null)