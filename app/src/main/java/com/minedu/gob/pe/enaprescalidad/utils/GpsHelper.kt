package com.minedu.gob.pe.enaprescalidad.utils

import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.IntentSenderRequest


// ─────────────────────────────────────────────────────────────────────────────
//  FUNCIONES AUXILIARES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Pide al sistema que muestre el diálogo de activar GPS.
 * CORRECCIÓN BUG 1: usa launcher (ActivityResultContract) en lugar de
 * startResolutionForResult directo, que no funciona bien desde Compose
 * porque el Activity puede no estar en primer plano cuando se llama.
 */

//fun requestGpsEnable(context: Context, onSenderReady: (android.app.PendingIntent) -> Unit) {
//    // Aquí implementas el flujo estándar con LocationSettingsRequest (Google Play Services)
//    // El cual provee el PendingIntent devuelto mediante el callback 'onSenderReady'
//}
fun requestGpsEnable(
    context: Context,
    launcher: (IntentSenderRequest) -> Unit,
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
        .build()
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)   // Fuerza que el dialog aparezca siempre
        .build()

    LocationServices.getSettingsClient(context)
        .checkLocationSettings(settingsRequest)
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {

                launcher(
                    IntentSenderRequest.Builder(exception.resolution).build()
                )
            }
        }
        .addOnSuccessListener {
        }
}

fun isGpsEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun isAirplaneMode(context: Context): Boolean {
    return Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.AIRPLANE_MODE_ON, 0
    ) != 0
}

fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data  = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
}

fun openAirplaneSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
}


