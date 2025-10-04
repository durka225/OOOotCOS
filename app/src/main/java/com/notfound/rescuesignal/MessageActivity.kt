package com.notfound.rescuesignal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.notfound.rescuesignal.ui.theme.RescueSignalTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notfound.rescuesignal.retrofit.RestApi
import com.notfound.rescuesignal.services.MessageRequest
import com.notfound.rescuesignal.services.MessageService
import com.notfound.rescuesignal.services.Response
import retrofit2.Call
import retrofit2.Callback

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageScreen(
                onOpenMain = {startActivity(Intent(this, MainActivity::class.java)) }
            )
        }
    }
}
@Composable
@Preview
fun MessageScreen(
    onOpenMain: () -> Unit = {}
) {
    var text by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("1") }
    val context = LocalContext.current
    val retrofit = RestApi(context).instance
    val messageService = retrofit.create(MessageService::class.java)

    RescueSignalTheme (
        dynamicColor = false
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp)
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.Top
                ){
                    Row(
                        modifier = Modifier
                                .padding(bottom = 26.dp)
                                .fillMaxWidth(),
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
                            text = "Преобразователь текста",
                            style = TextStyle(
                                fontSize = 20.sp,
                                lineHeight = 24.sp,
                                //fontFamily = FontFamily(Font(R.font.ruda)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFA1111),

                                letterSpacing = 0.15.sp,
                            )
                        )
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(507.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Red,
                            focusedBorderColor = Color.Red
                        ),
                        placeholder = {
                            Text(
                                text = "Введите текст...",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    lineHeight = 30.sp,
                                    //fontFamily = FontFamily(Font(R.font.ruda)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFEAD6D6),

                                    letterSpacing = 0.15.sp,
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = repeat,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                repeat = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Red,
                            focusedBorderColor = Color.Red
                        ),
                        label = {
                            Text(
                                text = "Количество повторений",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFA1111),
                                )
                            )
                        },
                        placeholder = {
                            Text(
                                text = "1",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFEAD6D6),
                                )
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                val repeatCount = repeat.toIntOrNull() ?: 1

                                val request = MessageRequest(text = text, repeat = repeatCount)
                                
                                val sendMessageRequest: Call<Response> = messageService.sendMessage(request)
                                sendMessageRequest.enqueue(object : Callback<Response> {
                                    override fun onFailure(call: Call<Response>, t: Throwable) {
                                        Log.e("MessageActivity", "Send failed", t)
                                    }

                                    override fun onResponse(
                                        call: Call<Response>,
                                        response: retrofit2.Response<Response>
                                    ) {
                                        if (response.isSuccessful) {
                                            Log.d("MessageActivity", "Sent: text=$text, repeat=$repeatCount")
                                            Log.d("MessageActivity", "Response: ${response.body()}")
                                        } else {
                                            Log.w("MessageActivity", "Error code: ${response.code()}")
                                        }
                                    }
                                })
                            },
                            modifier = Modifier
                                .width(180.dp)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text(
                                text = "Преобразовать",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    //fontFamily = FontFamily(Font(R.font.ruda)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),

                                    letterSpacing = 0.15.sp,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
