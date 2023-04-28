package com.deecto.callsmsmarketing.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deecto.callsmsmarketing.model.DayWhatsappCounter

@Dao
interface DayWhatsappCounterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dayWhatsappCounter: DayWhatsappCounter)

    @Query("SELECT * FROM day_whatsapp WHERE day = :day")
    fun getDayCount(day: String?): DayWhatsappCounter

    @Query("SELECT id FROM day_whatsapp WHERE day = :day LIMIT 1")
    fun getDayId(day: String): Int?

    @Query("Update day_whatsapp set counter = counter+1 where day = :d")
    fun updateDayCount(d: String?)

    @Query("SELECT SUM(counter) FROM day_whatsapp")
    fun getTotalCount(): Int
}