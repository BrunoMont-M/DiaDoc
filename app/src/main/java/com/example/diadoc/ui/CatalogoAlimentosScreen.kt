package com.example.diadoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel

// Definimos una estructura para el alimento dentro del mismo archivo para facilitar las cosas
data class AlimentoUI(
    val id: String,
    val nombre: String,
    val carbohidratos: String,
    val calorias: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoAlimentosScreen(
    viewModel: CatalogoAlimentosViewModel,
    onBackClick: () -> Unit = {}
) {
    // Lista de prueba local usando la nueva estructura con números reales
    var listaAlimentos by remember {
        mutableStateOf(
            listOf(
                AlimentoUI("1", "Manzana verde", "14", "52"),
                AlimentoUI("2", "Galletas de arroz", "7", "35"),
                AlimentoUI("3", "Yogurt natural", "5", "59")
            )
        )
    }

    // Estados para controlar los diálogos de Alerta
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var alimentoAEditar by remember { mutableStateOf<AlimentoUI?>(null) }

    // Campos del formulario
    var nombreInput by remember { mutableStateOf("") }
    var carbohidratosInput by remember { mutableStateOf("") }
    var caloriasInput by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryBlue = Color(0xFF00668B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Alimentos", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Limpiamos los inputs y abrimos el diálogo de agregar
                    nombreInput = ""
                    carbohidratosInput = ""
                    caloriasInput = ""
                    showAddDialog = true
                },
                containerColor = primaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Alimento")
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Panel de Administrador",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (listaAlimentos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay alimentos en el catálogo", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaAlimentos) { alimento ->
                        AlimentoItem(
                            alimento = alimento,
                            cardColor = cardColor,
                            onEditClick = {
                                alimentoAEditar = alimento
                                nombreInput = alimento.nombre
                                carbohidratosInput = alimento.carbohidratos
                                caloriasInput = alimento.calorias
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGO PARA AGREGAR ALIMENTO ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agregar Alimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    TextField(
                        value = carbohidratosInput,
                        onValueChange = { carbohidratosInput = it },
                        label = { Text("Carbohidratos (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = caloriasInput,
                        onValueChange = { caloriasInput = it },
                        label = { Text("Calorías (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nombreInput.isNotBlank()) {
                        val nuevoAlimento = AlimentoUI(
                            id = System.currentTimeMillis().toString(),
                            nombre = nombreInput,
                            carbohidratos = carbohidratosInput.ifBlank { "0" },
                            calorias = caloriasInput.ifBlank { "0" }
                        )
                        listaAlimentos = listaAlimentos + nuevoAlimento
                    }
                    showAddDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }

    // --- DIÁLOGO PARA EDITAR ALIMENTO ---
    if (showEditDialog && alimentoAEditar != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Alimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = nombreInput, onValueChange = { nombreInput = it }, label = { Text("Nombre") })
                    TextField(
                        value = carbohidratosInput,
                        onValueChange = { carbohidratosInput = it },
                        label = { Text("Carbohidratos (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = caloriasInput,
                        onValueChange = { caloriasInput = it },
                        label = { Text("Calorías (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    listaAlimentos = listaAlimentos.map {
                        if (it.id == alimentoAEditar?.id) {
                            it.copy(nombre = nombreInput, carbohidratos = carbohidratosInput, calorias = caloriasInput)
                        } else it
                    }
                    showEditDialog = false
                    alimentoAEditar = null
                }) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun AlimentoItem(alimento: AlimentoUI, cardColor: Color, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alimento.nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Carbohidratos: ${alimento.carbohidratos} g | Calorías: ${alimento.calorias} kcal",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            TextButton(onClick = onEditClick) {
                Text(text = "Editar", color = Color(0xFF00A3E0))
            }
        }
    }
}