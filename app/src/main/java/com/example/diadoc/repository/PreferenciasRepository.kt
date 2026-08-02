package com.example.diadoc.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes_usuario")

class PreferenciasRepository(private val context: Context) {

    companion object {
        val USAR_MGDL = booleanPreferencesKey("usar_mgdl")
        val USAR_KG = booleanPreferencesKey("usar_kg")
        val USAR_CM = booleanPreferencesKey("usar_cm")
        val USAR_ML = booleanPreferencesKey("usar_ml")

        val TEMA_APP = intPreferencesKey("tema_app")
        val PALETA_APP = intPreferencesKey("paleta_app")

        val NOTIF_COMIDAS = booleanPreferencesKey("notif_comidas")
        val NOTIF_RUTINAS = booleanPreferencesKey("notif_rutinas")
        val NOTIF_SISTEMA = booleanPreferencesKey("notif_sistema")
    }

    val usarMgdlFlow: Flow<Boolean> = context.dataStore.data.map { it[USAR_MGDL] ?: true }
    val usarKgFlow: Flow<Boolean> = context.dataStore.data.map { it[USAR_KG] ?: true }
    val usarCmFlow: Flow<Boolean> = context.dataStore.data.map { it[USAR_CM] ?: true }
    val usarMlFlow: Flow<Boolean> = context.dataStore.data.map { it[USAR_ML] ?: true }

    val temaAppFlow: Flow<Int> = context.dataStore.data.map { it[TEMA_APP] ?: 0 }
    val paletaAppFlow: Flow<Int> = context.dataStore.data.map { it[PALETA_APP] ?: 0 }

    val notifComidasFlow: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_COMIDAS] ?: true }
    val notifRutinasFlow: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_RUTINAS] ?: true }
    val notifSistemaFlow: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_SISTEMA] ?: true }

    suspend fun setUsarMgdl(valor: Boolean) { context.dataStore.edit { it[USAR_MGDL] = valor } }
    suspend fun setUsarKg(valor: Boolean) { context.dataStore.edit { it[USAR_KG] = valor } }
    suspend fun setUsarCm(valor: Boolean) { context.dataStore.edit { it[USAR_CM] = valor } }
    suspend fun setUsarMl(valor: Boolean) { context.dataStore.edit { it[USAR_ML] = valor } }

    suspend fun setTemaApp(valor: Int) { context.dataStore.edit { it[TEMA_APP] = valor } }
    suspend fun setPaletaApp(valor: Int) { context.dataStore.edit { it[PALETA_APP] = valor } }

    suspend fun setNotifComidas(valor: Boolean) { context.dataStore.edit { it[NOTIF_COMIDAS] = valor } }
    suspend fun setNotifRutinas(valor: Boolean) { context.dataStore.edit { it[NOTIF_RUTINAS] = valor } }
    suspend fun setNotifSistema(valor: Boolean) { context.dataStore.edit { it[NOTIF_SISTEMA] = valor } }
}