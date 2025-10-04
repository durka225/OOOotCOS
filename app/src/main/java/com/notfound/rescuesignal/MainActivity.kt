package com.notfound.rescuesignal

import android.R.attr.onClick
import android.R.attr.text
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notfound.rescuesignal.ui.theme.RescueSignalTheme
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen(
                context = this,
                onOpenMessage = {startActivity(Intent(this, MessageActivity::class.java)) },
                onOpenCamera = {startActivity(Intent(this, CameraActivity::class.java)) }
            )
        }
    }
}

@Composable
@Preview
fun MainScreen(
    context: Context? = null,
    onOpenMessage: () -> Unit = {},
    onOpenCamera: () -> Unit = {}
) {

    val interactionSource1 = remember { MutableInteractionSource() }
    val isPressed1 by interactionSource1.collectIsPressedAsState()

    val interactionSource2 = remember { MutableInteractionSource() }
    val isPressed2 by interactionSource2.collectIsPressedAsState()

    var showOverlay by remember { mutableStateOf(false) }

    RescueSignalTheme (
        dynamicColor = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp, start = 30.dp, end = 30.dp, bottom = 30.dp)
                ) {
                    Column(
                        modifier = Modifier.then(if (showOverlay) Modifier.blur(10.dp) else Modifier)
                    ) {
                    Text(
                        text = "СИГНАЛ \nСПАСЕНИЯ",
                        style = TextStyle(
                            fontSize = 54.sp,
                            lineHeight = 64.sp,
                            fontFamily = FontFamily(Font(R.font.rubik_doodle_shadow)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFF0E0E),

                            letterSpacing = 0.15.sp,
                        ),
                        modifier = Modifier
                            .padding(bottom = 44.dp)
                    )
                    Button(
                        onClick = { showOverlay = true },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text(
                            text = "Ввести ip-адрес",
                            style = TextStyle(
                                fontSize = 20.sp,
                                lineHeight = 24.sp,
                                //fontFamily = FontFamily(Font(R.font.ruda)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFFFFFF),

                                letterSpacing = 0.15.sp,
                            )
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .padding(top = 29.dp, bottom = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenMessage() },
                            interactionSource = interactionSource2,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isPressed2) Color.Red else Color.Black,
                                contentColor = Color(0xFFEAD6D6)
                            ),
                            border = BorderStroke(1.dp, Color.Red),
                            modifier = Modifier
                                .width(191.dp)
                                .height(130.dp)
                        ) {
                            Text(
                                text = "напиши \nсам",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 36.sp,
                                    //fontFamily = FontFamily(Font(R.font.ruda)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFEAD6D6),

                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.15.sp,
                                )
                            )
                        }
                        Image(
                            painter = painterResource(R.drawable.photo_camera),
                            contentDescription = "photo-camera",
                            modifier = if (isPressed1) Modifier.size(106.dp)
                                        else if (isPressed2) Modifier.size(75.dp) else Modifier.size(87.dp)
                        )
                    }
                    Row (
                        horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.Start),
                        //verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.sms),
                            contentDescription = "sms",
                            modifier = if (isPressed1) Modifier.size(78.dp)
                            else if (isPressed2) Modifier.size(94.dp) else Modifier.size(83.dp)
                        )
                        OutlinedButton(
                            onClick = { onOpenCamera() },
                            interactionSource = interactionSource1,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(226.dp)
                                .height(251.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isPressed1) Color.Red else Color.Black,
                                contentColor = Color(0xFFEAD6D6)
                            ),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.vector_left),
                                    contentDescription = "vector_left",
                                    colorFilter = if (isPressed1) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Red)
                                )
                                Text(
                                    text = "наведи\nкамеру",
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        lineHeight = 36.sp,
                                        //fontFamily = FontFamily(Font(R.font.ruda)),
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFFEAD6D6),
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 0.15.sp,
                                    )
                                )
                                Image(
                                    painter = painterResource(R.drawable.vector_right),
                                    contentDescription = "vector_right",
                                    colorFilter = if (isPressed1) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Red)
                                )
                            }
                        }
                    }
                }
            }
        }

            if (showOverlay) {
                loadIP(
                    context = context,
                    onClose = { showOverlay = false }
                )
            }
        }
    }
}

@Composable
@Preview
fun loadIP (
    context: Context? = null,
    onClose: () -> Unit = {}
) {
    val savedIP = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        ?.getString("ip_address", "10.173.96.203") ?: "10.173.96.203"
    
    var IP by remember { mutableStateOf(savedIP) }

    RescueSignalTheme (
        dynamicColor = false
    ) {


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.5f)
        ) {}

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onClose() },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp, bottom = 210.dp)
                        .size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close loading IP",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    
                    OutlinedTextField(
                        value = IP,
                        onValueChange = { IP = it },
                        placeholder = {
                            Text(
                                "192.168.1.100",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                ),
                                color = Color.Black
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp, end = 30.dp, bottom = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Red,
                            focusedBorderColor = Color.Red,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Button(
                        onClick = {
                            context?.let {
                                val sharedPreferences = it.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("ip_address", IP).apply()
                                
                                // Логируем для отладки
                                val fullUrl = "http://$IP:80"
                                Log.d("MainActivity", "Сохранён IP: $IP")
                                Log.d("MainActivity", "Полный BASE_URL: $fullUrl")
                            }
                            onClose()
                        },
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .width(200.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(
                        text = "Сохранить",
                        style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        //fontFamily = FontFamily(Font(R.font.ruda)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),

                        letterSpacing = 0.15.sp,
                    ))
                }
                }
            }
        }
    }
    }
}