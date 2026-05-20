package com.minedu.gob.pe.enaprescalidad.ui.components.maps

enum class PrecisionQuality { EXCELENTE, REGULAR, DEFICIENTE, DESCONOCIDA }

enum class MapFlowStep {
    CHECKING,          // Evaluando estado inicial
    MAP_OK,            // Todo correcto, sin ningún overlay
    GPS_REQUIRED,      // GPS apagado — overlay con botón activar
    BLOCKED_SETTINGS   // Permisos denegados — overlay con botón ajustes
}

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val quality: PrecisionQuality = PrecisionQuality.DESCONOCIDA,
    val calculationFinished: Boolean = false,
) {
    val tieneCoordenadasValidas: Boolean get() = latitude != 0.0 || longitude != 0.0
}

data class MapUiState(
    val step: MapFlowStep = MapFlowStep.CHECKING,
    val showRationaleDialog: Boolean = false,
    val isGpsDialogActive: Boolean = false,
    val isAirplaneMode: Boolean = false,
    val showAirplaneDialog: Boolean = false,
    val rpshowAirplaneDialog: Boolean = false,
    val locationData: LocationData = LocationData(),
    val isCalculatingLocation: Boolean = false,
    val showTimeoutDialog: Boolean = false,
    val remainingSeconds: Int = 120
)
