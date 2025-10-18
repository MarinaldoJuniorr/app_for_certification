package com.example.app_for_certification.data.repository

import com.example.app_for_certification.data.local.AppDatabase
import com.example.app_for_certification.data.local.toDomain
import com.example.app_for_certification.data.local.toDomainOrNull
import com.example.app_for_certification.data.local.toEntity
import com.example.app_for_certification.data.remote.Api
import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.repository.CountryRepositoryDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class CountryRepositoryImpl(
    private val api: Api,
    private val db: AppDatabase
) : CountryRepositoryDomain {

    private val dao = db.countryDao()

    private val field = listOf(
        "name", "cca2", "cca3", "region", "capital", "population",
        "flags","languages","currencies","demonyms"
    ).joinToString(",")

    override fun observeAll(): Flow<List<CountryDomain>> =
        dao.observeAll().map { it.map { e -> e.toDomain() } }

    override fun observeSearch(query: String): Flow<List<CountryDomain>> =
        dao.observeByName(query).map { it.map { e -> e.toDomain() } }

    override fun observeByCode(code: String): Flow<CountryDomain?> =
        dao.observeByCode(code).map { it?.toDomain() }

    override suspend fun syncAll(force: Boolean) {
        try {
            val remote = api.getAll(field)
            val now = System.currentTimeMillis()
            val list = remote.mapNotNull { dto ->
                val domain = dto.toDomainOrNull() ?: return@mapNotNull null
                domain.toEntity(now)
            }
            dao.upsertAll(list)
        } catch (e: IOException) {
            throw IOException("No internet connection and no cached data yet")
        }
    }

    override suspend fun syncByCode(code: String) {
        val clean = code.filter { it.isLetter() }.take(3).uppercase()
        try {
            val remote = api.getByAlphaQuery(clean, field)
            val dto = remote.firstOrNull() ?: return
            val domain = dto.toDomain()
            dao.upsert(domain.toEntity(System.currentTimeMillis()))
        } catch (e: IOException) {
            throw IOException("No internet connection and country ($clean) is not cached")
        }
    }

    override suspend fun getAll(): List<CountryDomain> {
        var entities = dao.observeAll().first()
        if (entities.isEmpty()) {
            syncAll(force = true)
            entities = dao.observeAll().first()
        }
        return entities.map { it.toDomain() }
    }

    override suspend fun searchByName(name: String): List<CountryDomain> {
        var entities = dao.observeByName(name).first()
        if (entities.isEmpty()) {
            syncAll(force = true)
            entities = dao.observeByName(name).first()
        }
        return entities.map { it.toDomain() }
    }

    override suspend fun getByCode(code: String): CountryDomain {
        val cached = dao.observeByCode(code).first()
        if (cached != null) return cached.toDomain()
        syncByCode(code)
        return dao.observeByCode(code).first()?.toDomain()
            ?: error("Country not found")
    }
}