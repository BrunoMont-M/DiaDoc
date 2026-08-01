package com.example.diadoc.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diadoc.viewmodel.CatalogoAlimentosViewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarAlimentoScreen(
    onNavigateBack: () -> Unit,
    viewModel: CatalogoAlimentosViewModel = viewModel()
) {
    val context = LocalContext.current

    val scanner = remember { GmsBarcodeScanning.getClient(context) }
    val coroutineScope = rememberCoroutineScope()

    val alimentosRecientes by viewModel.alimentos.collectAsState()
    val alimentoIA by viewModel.alimentoIA.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estados para los campos manuales y edición
    var idEnEdicion by remember { mutableStateOf<String?>(null) }
    var nombreAlimento by remember { mutableStateOf("") }
    var calorias by remember { mutableStateOf("") }
    var grasas by remember { mutableStateOf("") }
    var carbohidratos by remember { mutableStateOf("") }
    var proteinas by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFF121214)
    val cardColor = Color(0xFF1E1E24)
    val primaryColor = MaterialTheme.colorScheme.primary
    val infoBlue = Color(0xFF29B6F6)

    val camposCompletos = nombreAlimento.isNotBlank() && calorias.isNotBlank() &&
            grasas.isNotBlank() && carbohidratos.isNotBlank() && proteinas.isNotBlank()

    LaunchedEffect(alimentoIA) {
        alimentoIA?.let {
            idEnEdicion = null
            nombreAlimento = it.nombreAlimento
            calorias = it.kcalBase.toString()
            grasas = it.grasasBase.toString()
            carbohidratos = it.carbohidratosBase.toString()
            proteinas = it.proteinasBase.toString()
            viewModel.limpiarAlimentoIA()
            Toast.makeText(context, "Análisis de IA completado", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            Toast.makeText(context, "Analizando imagen...", Toast.LENGTH_SHORT).show()
            viewModel.analizarImagenConIA(bitmap)
        } else {
            Toast.makeText(context, "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Mis Alimentos", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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

                Text(
                    text = "AÑADIR NUEVO ALIMENTO",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val codigoEscaneado = barcode.rawValue ?: ""
                                    if (codigoEscaneado.contains(",")) {
                                        try {
                                            val partes = codigoEscaneado.split(",")
                                            idEnEdicion = null
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

                                                        withContext(Dispatchers.Main) {
                                                            idEnEdicion = null
                                                            nombreAlimento = if (brand.isNotEmpty()) "$name ($brand)" else name
                                                            calorias = nutriments.optString("energy-kcal_100g", "0")
                                                            grasas = nutriments.optString("fat_100g", "0")
                                                            carbohidratos = nutriments.optString("carbohydrates_100g", "0")
                                                            proteinas = nutriments.optString("proteins_100g", "0")
                                                            Toast.makeText(context, "¡Producto Encontrado!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "Producto no registrado", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cardColor)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR", color = Color.White, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CÁMARA IA", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = if (idEnEdicion != null) "EDITAR ALIMENTO" else "CARGA MANUAL (Detalles por 100g)",
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
                                    viewModel.guardarAlimento(
                                        codAlimento = idEnEdicion,
                                        nombre = nombreAlimento,
                                        kcal = calorias.toDoubleOrNull() ?: 0.0,
                                        grasas = grasas.toDoubleOrNull() ?: 0.0,
                                        carbohidratos = carbohidratos.toDoubleOrNull() ?: 0.0,
                                        proteinas = proteinas.toDoubleOrNull() ?: 0.0
                                    )
                                    idEnEdicion = null
                                    nombreAlimento = ""
                                    calorias = ""
                                    grasas = ""
                                    carbohidratos = ""
                                    proteinas = ""
                                    Toast.makeText(context, "Alimento guardado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = camposCompletos,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (idEnEdicion != null) "ACTUALIZAR" else "GUARDAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }

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
                        if (alimentosRecientes.isEmpty() && !isLoading) {
                            Text(
                                text = "No hay alimentos registrados.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            alimentosRecientes.take(10).forEach { alimento ->
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
                                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = null,
                                            tint = Color(0xFF4FC3F7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = alimento.nombreAlimento,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            idEnEdicion = alimento.codAlimento
                                            nombreAlimento = alimento.nombreAlimento
                                            calorias = alimento.kcalBase.toString()
                                            grasas = alimento.grasasBase.toString()
                                            carbohidratos = alimento.carbohidratosBase.toString()
                                            proteinas = alimento.proteinasBase.toString()
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.eliminarAlimento(alimento.codAlimento) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

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

            // --- OVERLAY GLOBAL DE CARGA ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Procesando...",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}