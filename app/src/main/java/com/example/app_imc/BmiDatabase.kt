package com.example.app_imc

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [Bmi::class], version = 2, exportSchema = false)
abstract class BmiDatabase : RoomDatabase() {
    abstract fun bmiDao(): BmiDao

    companion object {
        @Volatile
        private var INSTANCE: BmiDatabase? = null

        fun getDatabase(context: Context): BmiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BmiDatabase::class.java,
                    "bmi_database"
                )
                    .fallbackToDestructiveMigration() // Adiciona esta linha para recriar o banco de dados se o esquema mudar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}