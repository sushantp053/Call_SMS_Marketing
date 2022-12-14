package com.deecto.callsmsmarketing.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deecto.callsmsmarketing.model.Message
import com.deecto.callsmsmarketing.utility.DATABASE_NAME
import java.time.Instant

@Database(entities = arrayOf(Message::class), version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun getMessageDao(): MessageDao

    companion object{

        @Volatile
        private var INSTANCE : MessageDatabase? = null

        fun getDatabase(context : Context) : MessageDatabase{
            return  INSTANCE ?: synchronized(this){
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