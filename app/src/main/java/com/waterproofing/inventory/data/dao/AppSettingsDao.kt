package com.waterproofing.inventory.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.waterproofing.inventory.data.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)

    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getSettingValueDirect(key: String): String?

    @Query("SELECT value FROM app_settings WHERE key = :key")
    fun getSettingValueFlow(key: String): Flow<String?>

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
}
