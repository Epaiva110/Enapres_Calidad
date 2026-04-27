package com.minedu.gob.pe.enaprescalidad.models

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    val locationState: StateFlow<LocationData?> = callbackFlow {
        val context = getApplication<Application>()

        // 1. Enviar última ubicación conocida inmediatamente si existe
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                trySend(LocationData(it.latitude, it.longitude, it.altitude, it.accuracy))
            }
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(LocationData(loc.latitude, loc.longitude, loc.altitude, loc.accuracy))
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateDistanceMeters(0f)
            .setWaitForAccurateLocation(true)
            .build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }

        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}