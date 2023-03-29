package com.deecto.callsmsmarketing.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deecto.callsmsmarketing.model.*
import com.deecto.callsmsmarketing.utility.DATABASE_NAME

@Database(
    entities = arrayOf(
        Message::class,
        PhoneCall::class,
        DaySMSCounter::class,
        WhatsappMessage::class,
        DayWhatsappCounter::class,
        BlockedContacts::class,
    ), version = 1, exportSchema = false
)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun getMessageDao(): MessageDao
    abstract fun getWhatsappDao(): WhatsappDao
    abstract fun getPhoneCallDao(): PhoneCallDao
    abstract fun getDaySMSCounterDao(): DaySMSCounterDao
    abstract fun getDayWhatsappCounterDao(): DayWhatsappCounterDao
    abstract fun getBlockedContactsDao(): BlockedContactsDao


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