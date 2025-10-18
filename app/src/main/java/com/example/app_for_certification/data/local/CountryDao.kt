package com.example.app_for_certification.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries ORDER BY name ASC")
    fun observeAll(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun observeByName(query: String): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE code = :code LIMIT 1")
    fun observeByCode(code: String): Flow<CountryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CountryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CountryEntity)

    @Query("DELETE FROM countries")
    suspend fun clear()
}