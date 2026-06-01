package com.course.openrouterchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.course.openrouterchat.ui.ChatScreen
import com.course.openrouterchat.ui.theme.OpenRouterChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenRouterChatTheme {
                ChatScreen()
            }
        }
    }
}
