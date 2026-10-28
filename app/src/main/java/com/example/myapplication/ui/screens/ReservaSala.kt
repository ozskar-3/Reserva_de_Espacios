package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaSalaScreen(
    roomId: String,
    roomName: String,
    viewModel: ReservationViewModel,
    onReservationComplete: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cardBorderColor by animateColorAsState(
        targetValue = if (uiState.isOverlapError) StatusOccupied else PrimaryBlue,
        label = "BorderColorAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Reserva", color = Color.White) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Espacio Seleccionado", fontSize = 12.sp, color = TextMuted)
                    Text(text = roomName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text(text = "Código: $roomId", fontSize = 12.sp, color = TextDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Formulario de Solicitud de Agendamiento", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.courseName,
                onValueChange = { viewModel.onCourseNameChange(it) },
                label = { Text("Nombre del Curso / Actividad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.requesterUnit,
                onValueChange = { viewModel.onRequesterUnitChange(it) },
                label = { Text("Unidad / Escuela Solicitante (ej: Diseño, CITT)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { viewModel.onDateChange(it) },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.expectedParticipants,
                    onValueChange = { viewModel.onParticipantsChange(it) },
                    label = { Text("N° Asistentes") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.startTime,
                    onValueChange = { viewModel.onStartTimeChange(it) },
                    label = { Text("Hora Inicio (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.endTime,
                    onValueChange = { viewModel.onEndTimeChange(it) },
                    label = { Text("Hora Término (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // RETROALIMENTACIÓN VISUAL EN CASO DE CRUCE DE HORARIOS O CAMPOS FALTANTES
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isOverlapError) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (uiState.isOverlapError) StatusOccupied else StatusPending
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = if (uiState.isOverlapError) StatusOccupied else TextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.submitReservationRequest(roomId, roomName) {
                        onReservationComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cardBorderColor),
                shape = RoundedCornerShape(8.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Enviar Solicitud de Reserva", fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}
