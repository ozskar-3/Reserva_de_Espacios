package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.*
import com.example.myapplication.notification.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReservationFormUiState(
    val courseName: String = "",
    val requesterUnit: String = "",
    val date: String = "2026-09-01",
    val startTime: String = "08:30",
    val endTime: String = "10:00",
    val expectedParticipants: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isOverlapError: Boolean = false,
    val isLoading: Boolean = false
)

class ReservationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).reservationDao()
    private val notificationHelper = NotificationHelper(application)

    private val _currentUserEmail = MutableStateFlow("relator@therionlabs.cl")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _uiState = MutableStateFlow(ReservationFormUiState())
    val uiState: StateFlow<ReservationFormUiState> = _uiState.asStateFlow()

    val allRooms: StateFlow<List<RoomEntity>> = dao.getAllRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReservations: StateFlow<List<ReservationEntity>> = _currentUserEmail
        .flatMapLatest { email -> dao.getReservationsByUser(email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReservations: StateFlow<List<ReservationEntity>> = dao.getPendingReservations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserEmail(email: String) {
        _currentUserEmail.value = if (email.isBlank()) "relator@therionlabs.cl" else email
    }

    fun onCourseNameChange(v: String) { _uiState.update { it.copy(courseName = v, errorMessage = null, isOverlapError = false) } }
    fun onRequesterUnitChange(v: String) { _uiState.update { it.copy(requesterUnit = v, errorMessage = null) } }
    fun onDateChange(v: String) { _uiState.update { it.copy(date = v, errorMessage = null, isOverlapError = false) } }
    fun onStartTimeChange(v: String) { _uiState.update { it.copy(startTime = v, errorMessage = null, isOverlapError = false) } }
    fun onEndTimeChange(v: String) { _uiState.update { it.copy(endTime = v, errorMessage = null, isOverlapError = false) } }
    fun onParticipantsChange(v: String) { _uiState.update { it.copy(expectedParticipants = v, errorMessage = null) } }

    fun clearFormState() {
        _uiState.value = ReservationFormUiState()
    }

    // REGISTRO DE SOLICITUD DE RESERVA CON VALIDACIÓN
    fun submitReservationRequest(roomId: String, roomName: String, onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.courseName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese el nombre de la actividad o curso.") }
            return
        }
        if (state.requesterUnit.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Especifique la unidad o escuela solicitante.") }
            return
        }
        val participants = state.expectedParticipants.toIntOrNull() ?: 0
        if (participants <= 0) {
            _uiState.update { it.copy(errorMessage = "Ingrese una cantidad válida de participantes (> 0).") }
            return
        }
        if (state.startTime >= state.endTime) {
            _uiState.update { it.copy(errorMessage = "La hora de término debe ser posterior a la hora de inicio.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // PREVENCIÓN DE CRUCES/DUPLICADOS EN BASE DE DATOS LOCAL
            val overlapCount = dao.countOverlappingConfirmedReservations(
                roomId = roomId,
                date = state.date,
                startTime = state.startTime,
                endTime = state.endTime
            )

            if (overlapCount > 0) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOverlapError = true,
                        errorMessage = "¡CRUCE DETECTADO! La sala $roomName ya se encuentra RESERVADA Y CONFIRMADA en el bloque ${state.startTime} - ${state.endTime} del día ${state.date}."
                    )
                }
                return@launch
            }

            // Si no hay tope, se inserta como SOLICITADA
            val entity = ReservationEntity(
                roomId = roomId,
                roomName = roomName,
                courseName = state.courseName,
                requesterUnit = state.requesterUnit,
                startTime = state.startTime,
                endTime = state.endTime,
                date = state.date,
                userEmail = _currentUserEmail.value,
                expectedParticipants = participants,
                status = ReservationStatus.SOLICITADA
            )

            dao.insertReservation(entity)

            // Emitir Notificación Nativa al usuario
            notificationHelper.sendNotification(
                title = "Solicitud Registrada",
                message = "Tu solicitud para '$roomName' (${state.courseName}) fue enviada al coordinador Francisco para aprobación."
            )

            _uiState.update { it.copy(isLoading = false, successMessage = "Solicitud enviada exitosamente") }
            onSuccess()
        }
    }

    // ACCIÓN DEL COORDINADOR (FRANCISCO): APROBAR O RECHAZAR SOLICITUD
    fun updateStatusByCoordinator(reservation: ReservationEntity, newStatus: ReservationStatus) {
        viewModelScope.launch {
            if (newStatus == ReservationStatus.CONFIRMADA) {
                // Verificar nuevamente que no exista cruce antes de confirmar de forma definitiva
                val overlapCount = dao.countOverlappingConfirmedReservations(
                    roomId = reservation.roomId,
                    date = reservation.date,
                    startTime = reservation.startTime,
                    endTime = reservation.endTime,
                    excludeId = reservation.id
                )

                if (overlapCount > 0) {
                    notificationHelper.sendNotification(
                        title = "Error de Aprobación",
                        message = "No se puede aprobar la solicitud de ${reservation.roomName}. Existe un cruce de horario confirmado."
                    )
                    return@launch
                }
            }

            dao.updateReservationStatus(reservation.id, newStatus)

            if (newStatus == ReservationStatus.CONFIRMADA) {
                notificationHelper.sendNotification(
                    title = "¡Reserva APROBADA!",
                    message = "La sala ${reservation.roomName} fue CONFIRMADA para ${reservation.courseName} (${reservation.startTime} - ${reservation.endTime}). Espacio BLOQUEADO."
                )
            } else if (newStatus == ReservationStatus.RECHAZADA) {
                notificationHelper.sendNotification(
                    title = "Solicitud Rechazada",
                    message = "La solicitud para ${reservation.roomName} fue rechazada por la coordinación."
                )
            }
        }
    }

    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            dao.deleteReservation(reservationId)
            notificationHelper.sendNotification(
                title = "Reserva Cancelada",
                message = "La reserva ha sido eliminada y el espacio fue liberado en el calendario."
            )
        }
    }

    fun triggerTestNotification() {
        notificationHelper.sendNotification(
            title = "Prueba de Recurso Nativo",
            message = "Notificación del sistema THERIONLABS funcionando correctamente."
        )
    }
}

