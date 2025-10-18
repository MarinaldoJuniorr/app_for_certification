package com.example.app_for_certification.data.local

import com.example.app_for_certification.data.model.CountryDto
import com.example.app_for_certification.domain.model.CountryDomain


fun CountryDto.toDomainOrNull(): CountryDomain? {
    val finalCode = when {
        !cca3.isNullOrBlank() -> cca3.trim().uppercase()
        !cca2.isNullOrBlank() -> cca2.trim().uppercase()
        else -> null
    } ?: return null
    return toDomain(finalCode)
}

fun CountryDto.toDomain(
    finalCode: String = (cca3 ?: cca2 ?: "").trim().uppercase()
): CountryDomain {
    val currenciesList = currencies?.values?.mapNotNull { c ->
        val n = c.name?.trim().orEmpty()
        val s = c.symbol?.trim().orEmpty()
        when {
            n.isNotEmpty() && s.isNotEmpty() -> "$n ($s)"
            n.isNotEmpty() -> n
            s.isNotEmpty() -> s
            else -> null
        }
    } ?: emptyList()

    val flag = flags?.png?.takeIf { it.isNotBlank() }
        ?: flags?.svg?.takeIf { it.isNotBlank() }
        ?: ""

    val demonym = demonyms?.eng?.m ?: demonyms?.eng?.f ?: ""

    return CountryDomain(
        name = name?.common.orEmpty(),
        code = finalCode,
        region = region.orEmpty(),
        capital = capital?.firstOrNull().orEmpty(),
        population = population ?: 0L,
        flagUrl = flag,
        languages = languages?.values?.toList() ?: emptyList(),
        currencies = currenciesList,
        nationality = demonym
    )
}

private fun List<String>.joinForDb() = joinToString("|")
private fun String.splitForDomain() = if (isBlank()) emptyList() else split("|")

fun CountryDomain.toEntity(now: Long) = CountryEntity(
    code = code,
    name = name,
    region = region,
    capital = capital,
    population = population,
    flagUrl = flagUrl,
    languages = languages.joinForDb(),
    currencies = currencies.joinForDb(),
    nationality = nationality,
    updatedAt = now
)

fun CountryEntity.toDomain() = CountryDomain(
    name = name,
    code = code,
    region = region,
    capital = capital,
    population = population,
    flagUrl = flagUrl,
    languages = languages.splitForDomain(),
    currencies = currencies.splitForDomain(),
    nationality = nationality
)