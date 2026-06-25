package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.model.Alimento
import com.example.diadoc.model.DetalleDieta
import com.example.diadoc.model.Dieta
import com.example.diadoc.model.RecetaPersonalizada
import com.example.diadoc.repository.CatalogoAlimentosRepository
import com.example.diadoc.repository.DietaRepository
import com.example.diadoc.repository.PerfilMedicoRepository
import com.example.diadoc.repository.PlanDiarioRepository
import com.example.diadoc.repository.RecetaPersonalizadaRepository
import com.example.diadoc.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanNutricionalViewModel(
    private val planRepository: PlanDiarioRepository = PlanDiarioRepository(),
    private val dietaRepository: DietaRepository = DietaRepository(),
    private val catalogoRepository: CatalogoAlimentosRepository = CatalogoAlimentosRepository(),
    private val perfilRepository: PerfilMedicoRepository = PerfilMedicoRepository(),
    private val recetaRepository: RecetaPersonalizadaRepository = RecetaPersonalizadaRepository()
) : ViewModel() {

    private val _dietaState = MutableStateFlow<Resource<Pair<Dieta, Map<DetalleDieta, List<Alimento>>>>?>(null)
    val dietaState: StateFlow<Resource<Pair<Dieta, Map<DetalleDieta, List<Alimento>>>>?> = _dietaState

    private val _catalogoResultados = MutableStateFlow<List<Alimento>>(emptyList())
    val catalogoResultados: StateFlow<List<Alimento>> = _catalogoResultados

    private val _recetasGlobales = MutableStateFlow<List<RecetaPersonalizada>>(emptyList())
    val recetasGlobales: StateFlow<List<RecetaPersonalizada>> = _recetasGlobales

    private val _alertaRestriccion = MutableStateFlow<String?>(null)
    val alertaRestriccion: StateFlow<String?> = _alertaRestriccion

    fun cargarDietaDeHoy(codUsuario: String) {
        viewModelScope.launch {
            _dietaState.value = Resource.Loading
            try {
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val plan = planRepository.obtenerPlanDeHoy(codUsuario, fechaHoy)

                if (plan != null) {
                    val dieta = dietaRepository.obtenerDietaPorPlan(plan.codPlan)
                    if (dieta != null) {
                        val menuCompleto = dietaRepository.obtenerMenuCompleto(dieta.codDieta)
                        _dietaState.value = Resource.Success(Pair(dieta, menuCompleto))
                    } else {
                        _dietaState.value = Resource.Error("El plan existe pero no tiene una dieta asignada.")
                    }
                } else {
                    _dietaState.value = Resource.Error("Aún no has generado tu plan de hoy.")
                }
            } catch (e: Exception) {
                _dietaState.value = Resource.Error("Error al cargar la dieta.")
            }
        }
    }

    fun alternarConsumoComida(codPlan: String, codDieta: String, uid: String, codDetDieta: String, consumidoActual: Boolean) {
        viewModelScope.launch {
            val nuevoEstado = !consumidoActual
            val exito = dietaRepository.marcarComidaComoConsumida(codDieta, codDetDieta, nuevoEstado)
            if (exito) {
                val menuCompleto = dietaRepository.obtenerMenuCompleto(codDieta)
                val totalComidas = menuCompleto.size
                val comidasConsumidas = menuCompleto.keys.count { it.consumido }
                val nuevoProgreso = if (totalComidas > 0) comidasConsumidas.toDouble() / totalComidas.toDouble() else 0.0
                planRepository.actualizarProgresoDieta(codPlan, nuevoProgreso)
                cargarDietaDeHoy(uid)
            }
        }
    }

    // --- Lógica del Buscador de Recetas Globales ---
    fun buscarRecetasGlobales(uid: String, query: String, tipoComida: String) {
        viewModelScope.launch {
            val categoriaFiltro = if (tipoComida == "Todas") null else tipoComida
            val todasLasRecetas = recetaRepository.obtenerRecetas(uid, categoriaFiltro)

            if (query.isBlank()) {
                _recetasGlobales.value = todasLasRecetas
            } else {
                _recetasGlobales.value = todasLasRecetas.filter {
                    it.nombreReceta.contains(query, ignoreCase = true) ||
                            it.instruccionesReceta.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun limpiarBuscadorRecetas() {
        _recetasGlobales.value = emptyList()
    }

    // --- Motor de Reemplazo Estructural ---
    fun reemplazarRecetaCompleta(codDieta: String, detalleActual: DetalleDieta, nuevaReceta: RecetaPersonalizada, uid: String) {
        viewModelScope.launch {
            _dietaState.value = Resource.Loading
            try {
                // 1. Parseamos las instrucciones estructuradas de la IA guardadas en la receta global
                val partes = nuevaReceta.instruccionesReceta.split("|||")
                val descripcion = partes.getOrNull(0) ?: "Receta importada del catálogo"
                val ingredientesStr = partes.getOrNull(1) ?: ""
                val pasosStr = partes.getOrNull(2) ?: ""

                val preparacionList = if (pasosStr.isNotBlank()) pasosStr.split("@@") else emptyList()
                val ingredientesList = if (ingredientesStr.isNotBlank()) ingredientesStr.split("@@") else emptyList()

                val nuevosAlimentos = ingredientesList.mapNotNull { ingData ->
                    val ingParts = ingData.split("::")
                    val nombre = ingParts.getOrNull(0)
                    val kcal = ingParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                    if (nombre != null) {
                        Alimento(
                            nombreAlimento = nombre,
                            kcalBase = kcal,
                            proteinasBase = 0.0, carbohidratosBase = 0.0, grasasBase = 0.0, indiceGlucemico = 0
                        )
                    } else null
                }

                // 2. Operación Transaccional en Firestore
                val db = FirebaseFirestore.getInstance()
                val detalleRef = db.collection("dietas").document(codDieta)
                    .collection("detalles_comidas").document(detalleActual.codDetDieta)

                // A. Borramos los alimentos viejos
                val viejosAlimentos = detalleRef.collection("alimentos_detalle").get().await()
                for (doc in viejosAlimentos) {
                    doc.reference.delete().await()
                }

                // B. Actualizamos el encabezado del plato
                detalleRef.update(
                    mapOf(
                        "nombrePlato" to nuevaReceta.nombreReceta,
                        "descripcionPlato" to descripcion,
                        "preparacion" to preparacionList,
                        "cantDetDieta" to nuevosAlimentos.size,
                        "consumido" to false
                    )
                ).await()

                // C. Insertamos los nuevos alimentos
                for (alim in nuevosAlimentos) {
                    val nuevoDoc = detalleRef.collection("alimentos_detalle").document()
                    nuevoDoc.set(alim.copy(codAlimento = nuevoDoc.id)).await()
                }

                // 3. Recargamos la interfaz
                cargarDietaDeHoy(uid)

            } catch (e: Exception) {
                _dietaState.value = Resource.Error("Error al reemplazar la receta.")
            }
        }
    }

    // Lógica de Catálogo de Ingredientes Individuales
    fun buscarEnCatalogo(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _catalogoResultados.value = catalogoRepository.buscarAlimentos(query)
            } else {
                _catalogoResultados.value = emptyList()
            }
        }
    }

    fun limpiarCatalogo() { _catalogoResultados.value = emptyList() }
    fun limpiarAlerta() { _alertaRestriccion.value = null }

    fun agregarAlimento(codDieta: String, uid: String, codDetDieta: String, alimento: Alimento) {
        viewModelScope.launch {
            val perfil = perfilRepository.obtenerPerfilPorUsuario(uid)
            if (perfil != null) {
                val restriccionesUsuario = perfilRepository.obtenerRestriccionesDelPerfil(perfil.codPerfil)
                val conflicto = alimento.alergenos.firstOrNull { restriccionesUsuario.contains(it) }

                if (conflicto != null) {
                    _alertaRestriccion.value = "Bloqueado: Este alimento contiene ingredientes ($conflicto) prohibidos por tu perfil médico."
                    return@launch
                }
            }

            val exito = dietaRepository.agregarAlimentoAComida(codDieta, codDetDieta, alimento)
            if (exito) {
                cargarDietaDeHoy(uid)
            }
        }
    }

    fun eliminarAlimento(codDieta: String, uid: String, codDetDieta: String, codAlimento: String) {
        viewModelScope.launch {
            val exito = dietaRepository.eliminarAlimentoDeComida(codDieta, codDetDieta, codAlimento)
            if (exito) {
                cargarDietaDeHoy(uid)
            }
        }
    }
}