package com.example.app_for_certification.domain.usercase

import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.repository.CountryRepositoryDomain

class CodeUseCases(
    private val repo: CountryRepositoryDomain
) {
    suspend fun fetchAll(): List<CountryDomain> = repo.getAll()
    suspend fun fetchSearch(name: String): List<CountryDomain> = repo.searchByName(name)
    suspend fun fetchByCode(code: String): CountryDomain = repo.getByCode(code)
}