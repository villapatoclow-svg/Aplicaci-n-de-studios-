package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.model.HomeworkScanResult
import com.example.data.model.SlideAnalysisResult
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState
import java.io.File

@Composable
fun CameraHomeworkAndSlidesTab(
    homeworkScanState: AiCallState<HomeworkScanResult>,
    slideAnalysisState: AiCallState<SlideAnalysisResult>,
    onScanHomework: (Bitmap, String) -> Unit,
    onSaveHomeworkTask: (HomeworkScanResult) -> Unit,
    onAnalyzeSlide: (Bitmap, String) -> Unit,
    onSaveSlideTask: (SlideAnalysisResult) -> Unit
) {
    val context = LocalContext.current
    var cameraMode by remember { mutableIntStateOf(0) } // 0 = Tarea con Cámara, 1 = Diapositivas
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var questionHint by remember { mutableStateOf("") }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    fun createImageUri(ctx: Context): Uri {
        val dir = File(ctx.cacheDir, "homework").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }

    fun loadScaledBitmap(ctx: Context, uri: Uri): Bitmap {
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(ctx.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
        }
        val maxDim = 1280
        val scale = minOf(maxDim.toFloat() / raw.width, maxDim.toFloat() / raw.height, 1f)
        return if (scale < 1f) {
            Bitmap.createScaledBitmap(raw, (raw.width * scale).toInt(), (raw.height * scale).toInt(), true)
        } else raw
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCaptureUri
        if (success && uri != null) {
            runCatching { loadScaledBitmap(context, uri) }.onSuccess { bmp ->
                previewBitmap = bmp
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
            permissionMessage = null
        } else {
            permissionMessage = "Permiso de cámara denegado. Puedes usar 'Galería' sin permisos."
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching { loadScaledBitmap(context, uri) }.onSuccess { bmp ->
                previewBitmap = bmp
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (cameraMode == 0) "Cámara para Tareas IA" else "Ayuda de Diapositivas y Pizarrón",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (cameraMode == 0) {
                            "Fotografía un ejercicio, hoja de tarea o problema escrito a mano. La IA te da el resultado y el procedimiento completo."
                        } else {
                            "Captura diapositivas o apuntes de clase y recibe resumen sencillo, puntos clave y tarea de repaso."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = cameraMode == 0,
                            onClick = { cameraMode = 0 },
                            label = { Text("Resolver Tarea") }
                        )
                        FilterChip(
                            selected = cameraMode == 1,
                            onClick = { cameraMode = 1 },
                            label = { Text("Diapositivas") }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    val uri = createImageUri(context)
                                    pendingCaptureUri = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("camera_homework_button")
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tomar Foto")
                        }
                        FilledTonalButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gallery_homework_button")
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Galería")
                        }
                    }

                    if (permissionMessage != null) {
                        Text(permissionMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    previewBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Imagen capturada para resolver",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        OutlinedTextField(
                            value = questionHint,
                            onValueChange = { questionHint = it },
                            label = {
                                Text(
                                    if (cameraMode == 0) "Indicación opcional (ej. 'Usa fórmula general')"
                                    else "¿Qué duda tienes de esta diapositiva?"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        val isBusy = (cameraMode == 0 && homeworkScanState is AiCallState.Loading) ||
                            (cameraMode == 1 && slideAnalysisState is AiCallState.Loading)
                        Button(
                            onClick = {
                                if (cameraMode == 0) onScanHomework(bmp, questionHint)
                                else onAnalyzeSlide(bmp, questionHint)
                            },
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analyze_captured_image_button")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (cameraMode == 0) "Resolver Tarea Paso a Paso" else "Explicar Diapositiva con IA")
                        }
                    }
                }
            }
        }

        if (cameraMode == 0) {
            item {
                when (homeworkScanState) {
                    is AiCallState.Idle -> Unit
                    is AiCallState.Loading -> LoadingVisionCard("Leyendo ejercicio y resolviendo paso a paso...")
                    is AiCallState.Error -> ErrorVisionCard(homeworkScanState.message)
                    is AiCallState.Success -> {
                        val res = homeworkScanState.data
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "${res.subject} · ${res.suggestedTaskTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = res.problemStatement,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = StatusSuccess.copy(alpha = 0.14f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Respuesta: ${res.finalAnswer}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text("Procedimiento paso a paso:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                res.steps.forEachIndexed { idx, step ->
                                    Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    text = "Comprobación: ${res.verificationTip}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FilledTonalButton(
                                    onClick = { onSaveHomeworkTask(res) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.AddTask, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Guardar en Tareas Inteligentes")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item {
                when (slideAnalysisState) {
                    is AiCallState.Idle -> Unit
                    is AiCallState.Loading -> LoadingVisionCard("Analizando diapositiva con visión IA...")
                    is AiCallState.Error -> ErrorVisionCard(slideAnalysisState.message)
                    is AiCallState.Success -> {
                        val res = slideAnalysisState.data
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(res.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(res.simpleExplanation, style = MaterialTheme.typography.bodyMedium)
                                Text("Puntos clave:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                res.keyPoints.forEach { pt ->
                                    Text("• $pt", style = MaterialTheme.typography.bodyMedium)
                                }
                                FilledTonalButton(
                                    onClick = { onSaveSlideTask(res) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.AddTask, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Guardar como Tarea Inteligente")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingVisionCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Text(text)
        }
    }
}

@Composable
private fun ErrorVisionCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(message, modifier = Modifier.padding(16.dp))
    }
}
