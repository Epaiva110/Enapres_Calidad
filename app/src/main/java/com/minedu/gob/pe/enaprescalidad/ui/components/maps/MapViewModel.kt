package com.minedu.gob.pe.enaprescalidad.ui.components.maps

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.minedu.gob.pe.enaprescalidad.utils.isAirplaneMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null
    private var countdownJob: Job? = null

    init {
        // Carga inicial del estado de hardware real
        val airplaneOn = isAirplaneMode(application)
        _uiState.update { it.copy(isAirplaneMode = airplaneOn) }
    }

    fun setStep(step: MapFlowStep) = _uiState.update { it.copy(step = step) }
    fun showRationale(show: Boolean) = _uiState.update { it.copy(showRationaleDialog = show) }
    fun setGpsDialogActive(active: Boolean) = _uiState.update { it.copy(isGpsDialogActive = active) }
    fun dismissTimeoutDialog() = _uiState.update { it.copy(showTimeoutDialog = false) }
    fun dismissAirplaneDialog() = _uiState.update { it.copy(showAirplaneDialog = false, rpShowAirplaneDialog = true) }



    fun onHardwareChanged(gpsOn: Boolean, airplaneOn: Boolean, calculateLocation: Boolean, isInitialCheck: Boolean = false) {
        val anteriorAirplane = _uiState.value.isAirplaneMode
        _uiState.update { it.copy(isAirplaneMode = airplaneOn) }

        when {
            airplaneOn && !anteriorAirplane -> {
                _uiState.update { it.copy(showAirplaneDialog = true, rpShowAirplaneDialog = false) }
            }
            airplaneOn && isInitialCheck -> {
                _uiState.update { it.copy(showAirplaneDialog = true) }
            }
            !airplaneOn && anteriorAirplane -> {
                _uiState.update { it.copy(showAirplaneDialog = false) }
                if (gpsOn) {
                    setStep(MapFlowStep.MAP_OK)
                } else {
                    setStep(MapFlowStep.GPS_REQUIRED)
                }
            }
            !gpsOn -> {
                cancelLocationJob()
                setStep(MapFlowStep.GPS_REQUIRED)
            }
            gpsOn -> {
                if (_uiState.value.step != MapFlowStep.MAP_OK) {
                    setStep(MapFlowStep.MAP_OK)
                }
                if (calculateLocation && !_uiState.value.locationData.calculationFinished) {
                    startLocationCalculation()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationCalculation() {
        cancelLocationJob()
        _uiState.update { it.copy(isCalculatingLocation = true, showTimeoutDialog = false, remainingSeconds = 120) }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
        .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return

                val newQuality = when {
                    loc.accuracy < 10f  -> PrecisionQuality.EXCELENTE
                        loc.accuracy <= 50f -> PrecisionQuality.REGULAR
                    else                -> PrecisionQuality.DEFICIENTE
                }

                _uiState.update { state ->
                    val currentData = state.locationData
                    val debeActualizar = !currentData.tieneCoordenadasValidas || loc.accuracy < currentData.accuracy

                    if (debeActualizar) {
                        state.copy(
                            locationData = LocationData(
                                latitude = loc.latitude,
                        longitude = loc.longitude,
                        altitude = loc.altitude,
                        accuracy = loc.accuracy,
                        quality = newQuality
                        )
                        )
                    } else state
                }

                if (newQuality == PrecisionQuality.EXCELENTE) {
                    stopWithSuccess()
                }
            }
        }

        locationCallback = callback
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Inicialización segura del temporizador
        countdownJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }

            val data = _uiState.value.locationData
            if (data.tieneCoordenadasValidas) {
                stopWithSuccess()
            } else {
                cancelLocationJob()
                _uiState.update { it.copy(showTimeoutDialog = true) }
            }
        }
    }

    fun onRetryAfterTimeout() {
        _uiState.update { it.copy(showTimeoutDialog = false) }
        startLocationCalculation()
    }

    fun onAcceptManualAfterTimeout() {
        _uiState.update { it.copy(showTimeoutDialog = false) }
    }

    private fun stopWithSuccess() {
        cleanLocationResources()
        _uiState.update { state ->
            state.copy(
                isCalculatingLocation = false,
                locationData = state.locationData.copy(calculationFinished = true)
            )
        }
    }

    fun cancelLocationJob() {
        cleanLocationResources()
        _uiState.update { it.copy(isCalculatingLocation = false) }
    }

    fun recalculateLocation() {
        cancelLocationJob()
        _uiState.update { state ->
            state.copy(
                locationData = LocationData(),
            isCalculatingLocation = true,
            remainingSeconds = 120
            )
        }
        startLocationCalculation()
    }

    private fun cleanLocationResources() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanLocationResources()
    }
}