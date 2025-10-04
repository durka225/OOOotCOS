package com.notfound.rescuesignal

import android.R.attr.text
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen(
                onOpenMessage = {startActivity(Intent(this, MessageActivity::class.java)) }
            )
        }
    }
}

@Composable
@Preview
fun MainScreen(
    onOpenMessage: () -> Unit = {}
) {

    val interactionSource1 = remember { MutableInteractionSource() }
    val isPressed1 by interactionSource1.collectIsPressedAsState()

    // Кнопка 2
    val interactionSource2 = remember { MutableInteractionSource() }
    val isPressed2 by interactionSource2.collectIsPressedAsState()

    RescueSignalTheme (
        dynamicColor = false
    ) {
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
                Column() {
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
                            .padding(bottom = 60.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
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
                            onClick = { },
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
    }
}