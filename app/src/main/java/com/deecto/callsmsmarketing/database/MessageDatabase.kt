package com.deecto.callsmsmarketing.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deecto.callSMSmarketing.database.DayWhatsappDao
import com.deecto.callsmsmarketing.model.DaySMSCounter
import com.deecto.callsmsmarketing.model.DayWhatsappCounter
import com.deecto.callsmsmarketing.model.Message
import com.deecto.callsmsmarketing.model.PhoneCall
import com.deecto.callsmsmarketing.utility.DATABASE_NAME

@Database(entities = arrayOf(Message::class, PhoneCall::class, DaySMSCounter::class), version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun getMessageDao(): MessageDao
    abstract fun getWhatsappDao(): WhatsappDao
    abstract fun getPhoneCallDao(): PhoneCallDao
    abstract fun getDaySMSCounterDao(): DaySMSCounterDao
    abstract fun getDayWhatsappCounterDao(): DayWhatsappDao


    companion object {
        @Volatile
        private var INSTANCE: MessageDatabase? = null

        fun getDatabase(context: Context): MessageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageDatabase::class.java,
                    DATABASE_NAME
                ).build()

                INSTANCE = instance

                instance

            }
        }
    }

}