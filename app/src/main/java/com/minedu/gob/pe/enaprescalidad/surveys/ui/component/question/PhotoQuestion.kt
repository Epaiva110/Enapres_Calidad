package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

// ─────────────────────────────────────────────────────────────────────────────
// PhotoQuestion.kt
//
// Reemplaza PhotoQuestionPlaceholder en SurveyScreem.kt.
//
// Uso en DynamicQuestionAdapter:
//   "photo" -> PhotoQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), onValueChange)
//
// Import a añadir en SurveyScreem.kt:
//   import com.minedu.gob.pe.enaprescalidad.surveys.ui.PhotoQuestion
//
// ─────────────────────────────────────────────────────────────────────────────

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// Helpers de FileProvider
// ─────────────────────────────────────────────────────────────────────────────

/** Crea un archivo temporal en el directorio de caché de la app para la foto. */
fun crearArchivoFotoTemporal(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
    val dir = File(context.cacheDir, "survey_photos").also { it.mkdirs() }
    return File(dir, "FOTO_${timestamp}.jpg")
}

/** Obtiene el URI compatible con FileProvider para el archivo. */
fun uriParaArchivo(context: Context, file: File): Uri =
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",   // debe coincidir con AndroidManifest.xml
        file
    )

// ─────────────────────────────────────────────────────────────────────────────
// Componente principal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PhotoQuestion(
    pregunta     : Pregunta,
    fotosActuales: List<*>,
    onValueChange: (String, Any?) -> Unit,
    editable: Boolean = true
) {
    val context    = LocalContext.current
    val maxFotos   = pregunta.max_photos ?: 1
    val allowGallery = pregunta.allow_gallery ?: false

    // Lista de URIs en String (formato: "uri::content://..." o "file::...")
    val fotos = remember(fotosActuales) {
        fotosActuales.mapNotNull { it?.toString() }.toMutableStateList()
    }

    // URI del archivo temporal donde la cámara escribirá la foto
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Foto que se ve en el visor ampliado
    var fotoAmpliada by remember { mutableStateOf<String?>(null) }

    // ── Permisos de cámara ────────────────────────────────────────────────────
    var tieneCameraPermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var mostrarDialogoPermiso by remember { mutableStateOf(false) }
    var permisoFuePermanentementeDenegado by remember { mutableStateOf(false) }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        tieneCameraPermiso = granted
        if (!granted) {
            permisoFuePermanentementeDenegado = true
            mostrarDialogoPermiso = true
        }
    }

    // ── Launcher: Cámara del sistema ──────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoUri?.let { uri ->
                val nuevaLista = fotos.toMutableList() + uri.toString()
                fotos.clear(); fotos.addAll(nuevaLista)
                onValueChange(pregunta.variable, nuevaLista)
            }
        }
        pendingPhotoUri = null
    }

    // ── Launcher: Galería ─────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Conceder permiso persistente de lectura
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val nuevaLista = fotos.toMutableList() + it.toString()
            fotos.clear(); fotos.addAll(nuevaLista)
            onValueChange(pregunta.variable, nuevaLista)
        }
    }

    // ── Función: abrir cámara ─────────────────────────────────────────────────
    fun abrirCamara() {
        val archivo = crearArchivoFotoTemporal(context)
        val uri     = uriParaArchivo(context, archivo)
        pendingPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    fun intentarAbrirCamara() {
        if (tieneCameraPermiso) {
            abrirCamara()
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────────

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Galería horizontal de fotos ───────────────────────────────────────
        if (fotos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding        = PaddingValues(vertical = 4.dp),
            ) {
                itemsIndexed(fotos) { index, fotoUri ->
                    FotoMiniatura(
                        uri     = fotoUri,
                        numero  = index + 1,
                        onClick = { fotoAmpliada = fotoUri },
                        onDelete = {
                            val nuevaLista = fotos.toMutableList().also { it.removeAt(index) }
                            fotos.clear(); fotos.addAll(nuevaLista)
                            onValueChange(pregunta.variable, nuevaLista)
                        }
                    )
                }
                // Celda "+ Añadir" dentro de la fila (si hay espacio)
                if (fotos.size < maxFotos) {
                    item {
                        AgregarFotoCelda(
                            allowGallery    = allowGallery,
                            onCamera        = { intentarAbrirCamara() },
                            onGallery       = { galleryLauncher.launch("image/*") },
                        )
                    }
                }
            }
        }

        // ── Barra de progreso y botón principal ───────────────────────────────
        FotoActionBar(
            cantidad     = fotos.size,
            max          = maxFotos,
            allowGallery = allowGallery,
            puedeAnadir  = fotos.size < maxFotos,
            onCamera     = { intentarAbrirCamara() },
            onGallery    = { galleryLauncher.launch("image/*") },
        )
    }

    // ── Visor ampliado ────────────────────────────────────────────────────────
    fotoAmpliada?.let { uri ->
        FotoViewer(
            uri      = uri,
            onDismiss = { fotoAmpliada = null },
        )
    }

    // ── Diálogo de permiso denegado permanentemente ───────────────────────────
    if (mostrarDialogoPermiso) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermiso = false },
            icon  = { Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Permiso de cámara requerido") },
            text  = {
                Text(
                    if (permisoFuePermanentementeDenegado)
                        "El permiso de cámara fue denegado. Ve a Ajustes de la app para habilitarlo manualmente."
                    else
                        "Esta función necesita acceso a la cámara para capturar evidencia fotográfica."
                )
            },
            confirmButton = {
                Button(onClick = {
                    mostrarDialogoPermiso = false
                    if (!permisoFuePermanentementeDenegado) {
                        cameraPermLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        abrirAjustesApp(context)
                    }
                }) {
                    Text(if (permisoFuePermanentementeDenegado) "Ir a Ajustes" else "Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermiso = false }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Miniatura de foto con botón de borrar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FotoMiniatura(
    uri     : String,
    numero  : Int,
    onClick : () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Foto $numero",
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )

        // Número de foto (esquina inferior izquierda)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text("$numero", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        // Botón eliminar (esquina superior derecha)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .background(Color.Black.copy(alpha = 0.60f), CircleShape)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Eliminar foto $numero",
                tint     = Color.White,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Celda "+" dentro del carrusel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AgregarFotoCelda(
    allowGallery: Boolean,
    onCamera    : () -> Unit,
    onGallery   : () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .clickable {
                if (allowGallery) showMenu = true else onCamera()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.AddAPhoto,
            contentDescription = "Añadir foto",
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )

        // Menú contextual Cámara / Galería
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text       = { Text("Tomar foto") },
                leadingIcon = { Icon(Icons.Default.CameraAlt, null) },
                onClick    = { showMenu = false; onCamera() }
            )
            DropdownMenuItem(
                text       = { Text("Elegir de galería") },
                leadingIcon = { Icon(Icons.Default.PhotoLibrary, null) },
                onClick    = { showMenu = false; onGallery() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Barra de acción inferior con progreso y botón
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FotoActionBar(
    cantidad    : Int,
    max         : Int,
    allowGallery: Boolean,
    puedeAnadir : Boolean,
    onCamera    : () -> Unit,
    onGallery   : () -> Unit,
    editable: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }

    val progreso = (cantidad.toFloat() / max.coerceAtLeast(1)).coerceIn(0f, 1f)
    val colorBarra = when {
        cantidad >= max -> Color(0xFF10B981)
        cantidad > 0    -> MaterialTheme.colorScheme.primary
        else            -> MaterialTheme.colorScheme.outlineVariant
    }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Barra de progreso + contador
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Evidencias fotográficas",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "$cantidad / $max",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorBarra,
                )
            }
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color            = colorBarra,
                trackColor       = colorBarra.copy(alpha = 0.15f),
            )
        }

        // Botón de acción
        Box {
            FilledTonalButton(
                onClick  = {
                    if (!puedeAnadir) return@FilledTonalButton
                    if (allowGallery) showMenu = true else onCamera()
                },
                enabled  = editable && puedeAnadir,
                shape    = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = if (cantidad >= max) Icons.Default.CheckCircle
                    else Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier   = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = if (cantidad >= max) "Completo" else "Añadir",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text       = { Text("Tomar foto") },
                    leadingIcon = { Icon(Icons.Default.CameraAlt, null) },
                    onClick    = { showMenu = false; onCamera() }
                )
                DropdownMenuItem(
                    text       = { Text("Elegir de galería") },
                    leadingIcon = { Icon(Icons.Default.PhotoLibrary, null) },
                    onClick    = { showMenu = false; onGallery() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Visor de foto ampliada (Dialog fullscreen)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FotoViewer(uri: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = true,
            dismissOnClickOutside   = true,
        )
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model            = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto ampliada",
                contentScale     = ContentScale.Fit,
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )

            // Botón cerrar
            IconButton(
                onClick  = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.50f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun abrirAjustesApp(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}