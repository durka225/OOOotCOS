package com.notfound.rescuesignal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.notfound.rescuesignal.ui.theme.RescueSignalTheme
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.text.get

class CameraActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setContent {
                CameraScreen(
                    onOpenMain = { startActivity(Intent(this, MainActivity::class.java)) }
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!OpenCVLoader.initLocal()) {
            Log.e("OpenCV", "Initialization failed")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            setContent {
                CameraScreen(
                    onOpenMain = { startActivity(Intent(this, MainActivity::class.java)) }
                )
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun CameraScreen(onOpenMain: () -> Unit = {}) {
    val context = LocalContext.current
    val decodedMessage = remember { mutableStateOf("") }

    RescueSignalTheme(
        dynamicColor = false
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(200.dp))

                    CameraPreviewMorse(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        onMessageDecoded = { message ->
                            decodedMessage.value = message
                        }
                    )
                    Spacer(modifier = Modifier.height(90.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Декодировка",
                            modifier = Modifier.padding(top = 24.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                //fontFamily = FontFamily(Font(R.font.ruda)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFA1111),

                                letterSpacing = 0.15.sp,
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (decodedMessage.value.isNotEmpty()) decodedMessage.value else "Нет данных",
                            modifier = Modifier
                                .width(315.dp)
                                .height(128.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFA1111),
                                    shape = RoundedCornerShape(size = 10.dp)
                                )
                                .padding(16.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White
                            )
                        )

                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(R.drawable.switch_arrows),
                        contentDescription = "back_arrow",
                        modifier = Modifier
                            .size(42.dp)
                            .clickable {
                                (context as? ComponentActivity)?.finish()
                            }
                    )
                    Text(
                        text = "Считыватель сигнала",
                        style = TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFA1111),
                            letterSpacing = 0.15.sp,
                        )
                    )
                    Spacer(modifier = Modifier.size(42.dp))
                }

            }
        }
    }
}


@Composable
fun CameraPreviewMorse(
    modifier: Modifier = Modifier,
    onMessageDecoded: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var zoomLevel by remember { mutableStateOf(1f) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var maxZoomRatio by remember { mutableStateOf(10f) }

    val morseMap = remember {
        mapOf(
            listOf(0, 1) to 'a',
            listOf(1, 0, 0, 0) to 'b',
            listOf(1, 0, 1, 0) to 'c',
            listOf(1, 0, 0) to 'd',
            listOf(0) to 'e',
            listOf(0, 0, 1, 0) to 'f',
            listOf(1, 1, 0) to 'g',
            listOf(0, 0, 0, 0) to 'h',
            listOf(0, 0) to 'i',
            listOf(0, 1, 1, 1) to 'j',
            listOf(1, 0, 1) to 'k',
            listOf(0, 1, 0, 0) to 'l',
            listOf(1, 1) to 'm',
            listOf(1, 0) to 'n',
            listOf(1, 1, 1) to 'o',
            listOf(0, 1, 1, 0) to 'p',
            listOf(1, 1, 0, 1) to 'q',
            listOf(0, 1, 0) to 'r',
            listOf(0, 0, 0) to 's',
            listOf(1) to 't',
            listOf(0, 0, 1) to 'u',
            listOf(0, 0, 0, 1) to 'v',
            listOf(0, 1, 1) to 'w',
            listOf(1, 0, 0, 1) to 'x',
            listOf(1, 0, 1, 1) to 'y',
            listOf(1, 1, 0, 0) to 'z',
        )
    }

    val buffer = remember { mutableStateListOf<Int>() }
    val message = remember { mutableStateOf("") }
    val colorSequence = remember { mutableStateListOf<String>() }
    var previousSignal by remember { mutableStateOf<Int?>(null) }
    var blueSegmentCount by remember { mutableStateOf(0) }

    val colorBuffer = remember { mutableStateListOf<String>() }
    val colorBufferSize = 3

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                            it.setTargetRotation(previewView.display.rotation)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(executor) { imageProxy ->
                                val color = detectLedColor(imageProxy)

                                colorBuffer.add(color)
                                if (colorBuffer.size > colorBufferSize) {
                                    colorBuffer.removeAt(0)
                                }

                                val stableColor = if (colorBuffer.size >= colorBufferSize) {
                                    colorBuffer.groupBy { it }
                                        .maxByOrNull { it.value.size }
                                        ?.key ?: color
                                } else {
                                    color
                                }
                                
                                Log.d("LED Color", "Raw: $color, Stable: $stableColor")
                                
                                if (colorSequence.isEmpty() || colorSequence.last() != stableColor) {
                                    if (colorSequence.size >= 120) {
                                        colorSequence.removeAt(0)
                                    }
                                    colorSequence.add(stableColor)
                                }

                                val code = when(stableColor) {
                                    "Зелёный" -> 0
                                    "Красный" -> 1
                                    "Синий" -> 2
                                    else -> null
                                }
                                
                                if (code != null) {
                                    if (code != previousSignal) {
                                        when (code) {
                                            0, 1 -> {
                                                buffer.add(code)
                                                blueSegmentCount = 0
                                            }
                                            2 -> {
                                                if (buffer.isNotEmpty()) {
                                                    val decodedChar = morseMap[buffer.toList()] ?: '?'
                                                    message.value += decodedChar
                                                    onMessageDecoded(message.value)
                                                    buffer.clear()
                                                }

                                                blueSegmentCount += 1

                                                if (blueSegmentCount == 2) {
                                                    if (message.value.isNotEmpty() && message.value.last() != ' ') {
                                                        message.value += " "
                                                        onMessageDecoded(message.value)
                                                    }
                                                }
                                            }
                                        }
                                        previousSignal = code
                                    }
                                } else {
                                    previousSignal = null
                                }
                                imageProxy.close()
                            }
                            analysis.setTargetRotation(previewView.display.rotation)
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val boundCamera = cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalysis
                        )

                        cameraControl = boundCamera.cameraControl
                        val cameraInfo = boundCamera.cameraInfo

                        val actualMaxZoom = cameraInfo.zoomState.value?.maxZoomRatio ?: 10f
                        maxZoomRatio = minOf(actualMaxZoom, 100f)
                        Log.d("Camera", "Max zoom ratio: $maxZoomRatio")

                        try {
                            val exposureState = cameraInfo.exposureState
                            if (exposureState.isExposureCompensationSupported) {
                                val minCompensation = exposureState.exposureCompensationRange.lower
                                val compensationStep = (minCompensation * 0.10)                     .toInt()
                                cameraControl?.setExposureCompensationIndex(compensationStep)
                                Log.d("Camera", "Exposure compensation set to: $compensationStep (range: ${exposureState.exposureCompensationRange})")
                            }
                        } catch (e: Exception) {
                            Log.w("Camera", "Failed to set exposure: ${e.message}")
                        }
                    } catch (exc: Exception) {
                        Log.e("Camera", "Bind failed", exc)
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(top = 350.dp, start = 16.dp, end = 16.dp)
        ) {
            
            Slider(
                value = zoomLevel,
                onValueChange = { newZoom ->
                    zoomLevel = newZoom
                    cameraControl?.setZoomRatio(newZoom)
                },
                valueRange = 1f..maxZoomRatio,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFA1111),
                    activeTrackColor = Color(0xFFFA1111),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .border(
                    width = 2.dp,
                    color = Color.Red.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}




private fun detectLedColor(imageProxy: ImageProxy): String {
    val bitmap = imageProxy.toBitmap()
    val rgbaMat = Mat()
    Utils.bitmapToMat(bitmap, rgbaMat)

    val resizedMat = Mat()
    val scale = 0.5
    Imgproc.resize(rgbaMat, resizedMat, org.opencv.core.Size(), scale, scale, Imgproc.INTER_LINEAR)

    val hsvMat = Mat()
    Imgproc.cvtColor(resizedMat, hsvMat, Imgproc.COLOR_RGB2HSV)

    val bgrMat = Mat()
    Imgproc.cvtColor(resizedMat, bgrMat, Imgproc.COLOR_RGBA2BGR)

    val centerX = bgrMat.cols() / 2
    val centerY = bgrMat.rows() / 2
    val roiSize = 60
    val halfSize = roiSize / 2

    val startX = (centerX - halfSize).coerceAtLeast(0)
    val endX = (centerX + halfSize).coerceAtMost(bgrMat.cols())
    val startY = (centerY - halfSize).coerceAtLeast(0)
    val endY = (centerY + halfSize).coerceAtMost(bgrMat.rows())

    val roiHsv = hsvMat.submat(startY, endY, startX, endX)
    val roiBgr = bgrMat.submat(startY, endY, startX, endX)

    val filteredHsv = Mat()
    val filteredBgr = Mat()
    Imgproc.medianBlur(roiHsv, filteredHsv, 5)
    Imgproc.medianBlur(roiBgr, filteredBgr, 5)

    val hsvMean = Core.mean(filteredHsv)
    val hue = hsvMean.`val`[0]
    val saturation = hsvMean.`val`[1]
    val value = hsvMean.`val`[2]

    val bgrMean = Core.mean(filteredBgr)
    val blue = bgrMean.`val`[0]
    val green = bgrMean.`val`[1]
    val red = bgrMean.`val`[2]

    rgbaMat.release()
    resizedMat.release()
    hsvMat.release()
    bgrMat.release()
    roiHsv.release()
    roiBgr.release()
    filteredHsv.release()
    filteredBgr.release()

    if (value < 40) return "Светодиод выключен"

    if (saturation < 30 && value > 150) {
        return "Белый свет"
    }

    val result = when {
        saturation > 60 && value > 90 && (hue < 12 || hue > 348) -> "Красный"

        saturation > 55 && value > 70 && hue in 45.0..75.0 -> "Зелёный"

        saturation > 55 && value > 75 && hue in 205.0..245.0 -> "Синий"

        else -> {
            val maxChannel = max(red, max(green, blue))
            val minChannel = min(red, min(green, blue))
            val colorDiff = maxChannel - minChannel

            when {
                colorDiff < 30 && maxChannel > 120 -> "Белый свет"
                red > green + 50 && red > blue + 50 && red > 110 -> "Красный"
                green > red + 40 && green > blue + 40 && green > 100 -> "Зелёный"
                blue > red + 40 && blue > green + 40 && blue > 100 -> "Синий"
                else -> "Неопределённый цвет"
            }
        }
    }

    Log.d("LED Color Detail", "HSV: h=$hue s=$saturation v=$value, RGB: r=$red g=$green b=$blue -> $result")
    return result
}