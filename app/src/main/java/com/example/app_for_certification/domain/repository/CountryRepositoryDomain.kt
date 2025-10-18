package com.example.app_for_certification.domain.repository

import com.example.app_for_certification.domain.model.CountryDomain
import kotlinx.coroutines.flow.Flow

interface CountryRepositoryDomain {
    fun observeAll(): Flow<List<CountryDomain>>
    fun observeSearch(query: String): Flow<List<CountryDomain>>
    fun observeByCode(code: String): Flow<CountryDomain?>

    suspend fun syncAll(force: Boolean = false)
    suspend fun syncByCode(code: String)

    suspend fun getAll(): List<CountryDomain>
    suspend fun searchByName(name: String): List<CountryDomain>
    suspend fun getByCode(code: String): CountryDomain
}