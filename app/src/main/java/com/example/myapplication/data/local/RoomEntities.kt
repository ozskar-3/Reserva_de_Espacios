package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReservationStatus {
    SOLICITADA,
    CONFIRMADA,
    RECHAZADA,
    CANCELADA
}

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey
    val roomId: String,
    val name: String,
    val type: String,
    val capacity: Int,
    val equipment: String,
    val hasProjector: Boolean,
    val hasComputers: Boolean
) {
    val id: String get() = roomId
    val spaceType: String get() = type
    val technicalEquipment: String get() = equipment
}

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val roomId: String,
    val roomName: String,
    val courseName: String,
    val requesterUnit: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val userEmail: String,
    val expectedParticipants: Int,
    val status: ReservationStatus = ReservationStatus.SOLICITADA
)
