package com.example.app_for_certification.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val code: String,
    val name: String,
    val region: String,
    val capital: String,
    val population: Long,
    val flagUrl: String,
    val languages: String,
    val currencies: String,
    val nationality: String,
    val updatedAt: Long
)