package com.partyhub.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos principal de la aplicación usando Room.
 * 
 * Sigue el patrón Singleton para evitar múltiples instancias abiertas.
 */
@Database(entities = [MatchHistory::class], version = 1, exportSchema = false)
abstract class PartyHubDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: PartyHubDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos.
         * Usa fallbackToDestructiveMigration para simplificar el desarrollo en E3.
         */
        fun getInstance(context: Context): PartyHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PartyHubDatabase::class.java,
                    "partyhub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
