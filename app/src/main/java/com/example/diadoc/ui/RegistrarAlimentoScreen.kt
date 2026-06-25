package com.example.diadoc.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// 1. CLASE RESTAURADA (Soluciona los errores de tipos y listas)
data class AlimentoReciente(
    val id: String,
    val nombre: String,
    val esManual: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarAlimentoScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Inicialización del escáner y la corrutina para llamados de red
    val scanner = remember { GmsBarcodeScanning.getClient(context) }
    val coroutineScope = rememberCoroutineScope()

    // Estados para los campos de carga manual
    var nombreAlimento by remember { mutableStateOf("") }
    var calorias by remember { mutableStateOf("") }
    var grasas by remember { mutableStateOf("") }
    var carbohidratos by remember { mutableStateOf("") }
    var proteinas by remember { mutableStateOf("") }

    // Lista mutable simulando "Mi Despensa (Recientes)"
    val alimentosRecientes = remember {
        mutableStateListOf(
            AlimentoReciente("1", "Leche Descremada (La Serenísima)", esManual = false),
            AlimentoReciente("2", "Galletas de Arroz", esManual = false),
            AlimentoReciente("3", "Mi Granola Casera (Manual)", esManual = true)
        )
    }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary
    val infoBlue = Color(0xFF29B6F6)

    val camposCompletos = nombreAlimento.isNotBlank() && calorias.isNotBlank() &&
            grasas.isNotBlank() && carbohidratos.isNotBlank() && proteinas.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Alimentos y Recetas", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- SECCIÓN 1: AÑADIR NUEVO ALIMENTO (QR) ---
                Text(
                    text = "AÑADIR NUEVO ALIMENTO",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Button(
                    onClick = {
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                val codigoEscaneado = barcode.rawValue ?: ""

                                if (codigoEscaneado.contains(",")) {
                                    try {
                                        val partes = codigoEscaneado.split(",")
                                        nombreAlimento = partes[0].trim()
                                        calorias = partes[1].trim()
                                        grasas = partes[2].trim()
                                        carbohidratos = partes[3].trim()
                                        proteinas = partes[4].trim()
                                        Toast.makeText(context, "Alimento importado con éxito", Toast.LENGTH_SHORT).show()
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Formato QR no compatible", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Buscando en la base de datos...", Toast.LENGTH_SHORT).show()

                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val url = URL("https://world.openfoodfacts.org/api/v0/product/$codigoEscaneado.json")
                                            val connection = url.openConnection() as HttpURLConnection
                                            connection.requestMethod = "GET"

                                            if (connection.responseCode == 200) {
                                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                                val jsonObject = JSONObject(response)

                                                if (jsonObject.getInt("status") == 1) {
                                                    val product = jsonObject.getJSONObject("product")
                                                    val nutriments = product.optJSONObject("nutriments") ?: JSONObject()

                                                    val name = product.optString("product_name", "Producto sin nombre")
                                                    val brand = product.optString("brands", "")

                                                    val kcal = nutriments.optString("energy-kcal_100g", "0")
                                                    val fat = nutriments.optString("fat_100g", "0")
                                                    val carbs = nutriments.optString("carbohydrates_100g", "0")
                                                    val pro = nutriments.optString("proteins_100g", "0")

                                                    withContext(Dispatchers.Main) {
                                                        nombreAlimento = if (brand.isNotEmpty()) "$name ($brand)" else name
                                                        calorias = kcal
                                                        grasas = fat
                                                        carbohidratos = carbs
                                                        proteinas = pro
                                                        Toast.makeText(context, "¡Producto Encontrado!", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Producto no registrado en Open Food Facts", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Error de red: revisa tu conexión", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Escaneo cancelado: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cardColor)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = primaryColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("ESCANEAR CÓDIGO QR / BARRAS", color = Color.White, fontWeight = FontWeight.Medium)
                }

                // --- SECCIÓN 2: CARGA MANUAL ---
                Text(
                    text = "CARGA MANUAL (Detalles por 100g)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nombreAlimento,
                            onValueChange = { nombreAlimento = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = calorias,
                                onValueChange = { calorias = it },
                                label = { Text("Calorías (kcal)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray
                                )
                            )
                            OutlinedTextField(
                                value = grasas,
                                onValueChange = { grasas = it },
                                label = { Text("Grasas (g)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = carbohidratos,
                                onValueChange = { carbohidratos = it },
                                label = { Text("Carbohidratos (g)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray
                                )
                            )
                            OutlinedTextField(
                                value = proteinas,
                                onValueChange = { proteinas = it },
                                label = { Text("Proteínas (g)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (camposCompletos) {
                                    alimentosRecientes.add(
                                        AlimentoReciente(
                                            id = System.currentTimeMillis().toString(),
                                            nombre = nombreAlimento,
                                            esManual = true
                                        )
                                    )
                                    // Limpiar formulario
                                    nombreAlimento = ""
                                    calorias = ""
                                    grasas = ""
                                    carbohidratos = ""
                                    proteinas = ""
                                }
                            },
                            enabled = camposCompletos,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GUARDAR ALIMENTO", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- SECCIÓN 3: MI DESPENSA (RECIENTES) ---
                Text(
                    text = "MI DESPENSA (Recientes)",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alimentosRecientes.forEach { alimento ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (alimento.esManual) Icons.AutoMirrored.Filled.Assignment else Icons.Default.LocalGroceryStore,
                                        contentDescription = null,
                                        tint = if (alimento.esManual) Color(0xFF4FC3F7) else Color(0xFFFFD54F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = alimento.nombre,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }

                                Row {
                                    IconButton(onClick = { /* Editar */ }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { alimentosRecientes.remove(alimento) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // --- NOTA INFORMATIVA INFERIOR ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = infoBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Asegúrate de completar todos los macronutrientes para un conteo calórico exacto.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}