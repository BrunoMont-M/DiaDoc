package com.example.diadoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diadoc.utils.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GestionCuentaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _estadoAccion = MutableStateFlow<Resource<String>?>(null)
    val estadoAccion: StateFlow<Resource<String>?> = _estadoAccion

    fun limpiarEstado() {
        _estadoAccion.value = null
    }

    fun cambiarPassword(passwordActual: String, passwordNueva: String) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email == null) {
            _estadoAccion.value = Resource.Error("Error de sesión. Por favor, vuelve a ingresar a la app.")
            return
        }

        if (passwordNueva.length < 6) {
            _estadoAccion.value = Resource.Error("La nueva contraseña debe tener al menos 6 caracteres.")
            return
        }

        viewModelScope.launch {
            _estadoAccion.value = Resource.Loading
            try {
                // 1. Reautenticación estricta
                val credential = EmailAuthProvider.getCredential(email, passwordActual)
                user.reauthenticate(credential).await()

                // 2. Cambio de contraseña
                user.updatePassword(passwordNueva).await()
                _estadoAccion.value = Resource.Success("CONTRASEÑA_CAMBIADA")

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error desconocido"
                if (errorMsg.contains("INVALID_LOGIN_CREDENTIALS") || errorMsg.contains("wrong password")) {
                    _estadoAccion.value = Resource.Error("La contraseña actual es incorrecta.")
                } else {
                    _estadoAccion.value = Resource.Error("Error al cambiar contraseña: Verifica tu conexión.")
                }
            }
        }
    }

    fun eliminarCuenta(passwordActual: String) {
        val user = auth.currentUser
        val email = user?.email
        val uid = user?.uid

        if (user == null || email == null || uid == null) {
            _estadoAccion.value = Resource.Error("Error de sesión.")
            return
        }

        viewModelScope.launch {
            _estadoAccion.value = Resource.Loading
            try {
                // 1. Reautenticación estricta para confirmar identidad
                val credential = EmailAuthProvider.getCredential(email, passwordActual)
                user.reauthenticate(credential).await()

                // 2. HISTORIZACIÓN DE ESTADOS (BORRADO LÓGICO RELACIONAL)
                val fechaHoy = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

                // A) Buscar el estado actual abierto (fechaHasta vacía) y CERRARLO
                val estadosAbiertos = db.collection("usuarioEstados")
                    .whereEqualTo("codUsuario", uid)
                    .whereEqualTo("fechaHastaUEstado", "")
                    .get().await()

                for (doc in estadosAbiertos.documents) {
                    db.collection("usuarioEstados").document(doc.id)
                        .update("fechaHastaUEstado", fechaHoy).await()
                }

                // B) Crear un NUEVO registro de estado vinculando al usuario con la "BAJA"
                val nuevoDocRef = db.collection("usuarioEstados").document()
                val nuevoEstado = hashMapOf(
                    "codUsuarioEstado" to nuevoDocRef.id,
                    "codUsuario" to uid,
                    "codEstadoU" to "ESTADO_BAJA",
                    "fechaDesdeUEstado" to fechaHoy,
                    "fechaHastaUEstado" to ""
                )
                nuevoDocRef.set(nuevoEstado).await()

                // C) Desactivar el Perfil Médico
                val perfilSnapshot = db.collection("perfilesMedicos").whereEqualTo("codUsuario", uid).get().await()
                if (!perfilSnapshot.isEmpty) {
                    db.collection("perfilesMedicos").document(perfilSnapshot.documents[0].id)
                        .update("estadoActivo", false).await()
                }

                // 3. Borramos la credencial de Auth para revocar acceso y liberar el correo
                user.delete().await()

                _estadoAccion.value = Resource.Success("CUENTA_ELIMINADA")

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error desconocido"
                if (errorMsg.contains("INVALID_LOGIN_CREDENTIALS") || errorMsg.contains("wrong password")) {
                    _estadoAccion.value = Resource.Error("La contraseña es incorrecta. Eliminación abortada.")
                } else {
                    _estadoAccion.value = Resource.Error("Error al eliminar la cuenta. Inténtalo más tarde.")
                }
            }
        }
    }
}