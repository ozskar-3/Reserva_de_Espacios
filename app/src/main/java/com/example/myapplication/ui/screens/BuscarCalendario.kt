package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarCalendarioScreen(
    viewModel: ReservationViewModel,
    onRoomSelected: (roomId: String, roomName: String) -> Unit,
    onBack: () -> Unit
) {
    val rooms by viewModel.allRooms.collectAsState(initial = emptyList())
    var selectedTypeFilter by remember { mutableStateOf("TODOS") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo y Disponibilidad", color = Color.White) },
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
            Text("Filtrar por Tipo de Espacio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("TODOS", "Sala", "Teatro", "Estudio Grabación", "Taller").forEach { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredRooms = if (selectedTypeFilter == "TODOS") {
                rooms
            } else {
                rooms.filter { it.spaceType == selectedTypeFilter }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredRooms) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoomSelected(room.id, room.name) },
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(3.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(room.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                                Text("Tipo: ${room.spaceType} | Capacidad: ${room.capacity} pers.", fontSize = 12.sp, color = TextDark)
                                Text("Equipamiento: ${room.technicalEquipment}", fontSize = 11.sp, color = TextMuted)
                            }
                            Button(
                                onClick = { onRoomSelected(room.id, room.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Solicitar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
