package com.example.app_for_certification.domain.repository

import com.example.app_for_certification.domain.model.CountryDomain

interface CountryRepositoryDomain {
    suspend fun getAll(): List<CountryDomain>
    suspend fun searchByName(name: String): List<CountryDomain>
    suspend fun getByCode(code: String): CountryDomain
}