package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM reservations")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE userEmail = :email")
    fun getReservationsByUser(email: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE status = 'SOLICITADA'")
    fun getPendingReservations(): Flow<List<ReservationEntity>>

    @Query("""
        SELECT COUNT(*) FROM reservations 
        WHERE roomId = :roomId 
          AND date = :date 
          AND status = 'CONFIRMADA' 
          AND id != :excludeId
          AND ((startTime <= :startTime AND endTime > :startTime) OR (startTime < :endTime AND endTime >= :endTime) OR (startTime >= :startTime AND endTime <= :endTime))
    """)
    suspend fun countOverlappingConfirmedReservations(
        roomId: String,
        date: String,
        startTime: String,
        endTime: String,
        excludeId: Long = 0L
    ): Int

    @Query("UPDATE reservations SET status = :status WHERE id = :id")
    suspend fun updateReservationStatus(id: Long, status: ReservationStatus)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteReservation(id: Long)
}
