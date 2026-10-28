package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ReservationEntity::class, RoomEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "therionlabs_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.reservationDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: ReservationDao) {
                val initialRooms = listOf(
                    RoomEntity("R101", "Sala 101 - Capacitación", "Sala", 30, "Proyector, Climatización", hasProjector = true, hasComputers = false),
                    RoomEntity("R102", "Lab 201 - Redes y Software", "Sala", 25, "25 PCs High-End, Proyector", hasProjector = true, hasComputers = true),
                    RoomEntity("R103", "Teatro Institucional Viña", "Teatro", 120, "Sistema Audio Pro, Iluminación LED", hasProjector = true, hasComputers = false),
                    RoomEntity("R104", "Estudio de Grabación 1", "Estudio Grabación", 10, "Micrófonos Condensador, Mixer", hasProjector = false, hasComputers = true),
                    RoomEntity("R105", "Taller Abierto de Diseño", "Taller", 40, "Mesas de Dibajo, Impresoras 3D", hasProjector = true, hasComputers = true)
                )
                dao.insertRooms(initialRooms)
            }
        }
    }
}
