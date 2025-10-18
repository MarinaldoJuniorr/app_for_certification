package com.example.app_for_certification.data.repository

import com.example.app_for_certification.data.model.CountryDto
import com.example.app_for_certification.data.network.Api
import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.repository.CountryRepositoryDomain

class CountryRepositoryImpl(
    private val api: Api
) : CountryRepositoryDomain {

    private val FIELDS = listOf(
        "name","cca2","cca3","region","capital","population",
        "flags","languages","currencies","demonyms"
    ).joinToString(",")

    override suspend fun getAll(): List<CountryDomain> =
        api.getAll(FIELDS).mapNotNull { it.toDomainOrNull() }

    override suspend fun searchByName(name: String): List<CountryDomain> =
        api.getSearchByName(name, FIELDS).mapNotNull { it.toDomainOrNull() }

    override suspend fun getByCode(code: String): CountryDomain {
        val clean = code.filter { it.isLetter() }.take(3).uppercase()
        require(clean.length in 2..3) { "Invalid country code: '$code'" }

        val list = api.getByAlphaQuery(clean, FIELDS)
        val dto = list.firstOrNull() ?: error("Country not found for code=$clean")
        return dto.toDomain()
    }
}

private fun CountryDto.toDomainOrNull(): CountryDomain? {
    val finalCode = when {
        !cca3.isNullOrBlank() -> cca3.trim().uppercase()
        !cca2.isNullOrBlank() -> cca2.trim().uppercase()
        else -> null
    } ?: return null
    return toDomain(finalCode)
}

private fun CountryDto.toDomain(
    finalCode: String = (cca3 ?: cca2 ?: "").trim().uppercase()
): CountryDomain {
    val curr = currencies?.values?.mapNotNull { c ->
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
        currencies = curr,
        nationality = demonym
    )
}