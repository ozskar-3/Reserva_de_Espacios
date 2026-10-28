package com.example.myapplication.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.ReservationStatus
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(
    viewModel: ReservationViewModel,
    onBack: () -> Unit
) {
    val reservations by viewModel.userReservations.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes y Reservas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.triggerTestNotification() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Probar Recurso Nativo: Notificación Local")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reservations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes solicitudes ni reservas registradas.", color = TextMuted)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(reservations) { res ->
                        val (statusColor, statusText) = when (res.status) {
                            ReservationStatus.SOLICITADA -> Pair(StatusPending, "SOLICITADA (Pendiente)")
                            ReservationStatus.CONFIRMADA -> Pair(StatusAvailable, "CONFIRMADA (Bloqueada)")
                            ReservationStatus.RECHAZADA -> Pair(StatusOccupied, "RECHAZADA")
                            ReservationStatus.CANCELADA -> Pair(TextMuted, "CANCELADA")
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            elevation = CardDefaults.cardElevation(3.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(res.roomName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Actividad: ${res.courseName}", fontSize = 13.sp, color = TextDark)
                                Text("Unidad: ${res.requesterUnit} | Asistentes: ${res.expectedParticipants}", fontSize = 12.sp, color = TextMuted)
                                Text("Fecha: ${res.date} | Bloque: ${res.startTime} - ${res.endTime}", fontSize = 12.sp, color = TextDark)

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { viewModel.cancelReservation(res.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusOccupied)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
