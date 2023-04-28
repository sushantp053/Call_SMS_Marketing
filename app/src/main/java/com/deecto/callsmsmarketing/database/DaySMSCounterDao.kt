package com.deecto.callsmsmarketing.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deecto.callsmsmarketing.model.DaySMSCounter

@Dao
interface DaySMSCounterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(daySMSCounter: DaySMSCounter)

    @Query("SELECT * FROM day_sms WHERE day = :day")
    fun getDayCount(day: String?): DaySMSCounter

    @Query("SELECT id FROM day_sms WHERE day = :day LIMIT 1")
    fun getDayId(day: String): Int?

    @Query("Update day_sms set counter = counter+1 where day = :d")
    fun updateDayCount(d: String?)

    @Query("SELECT SUM(counter) FROM day_sms")
    fun getTotalCount(): Int
}