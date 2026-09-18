package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.darkgo.ui.audio.SoundAndHapticManager
import com.example.darkgo.ui.navigation.DarkGoNavGraph
import com.example.ui.theme.DarkGoBackground
import com.example.ui.theme.DarkGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val soundManager = remember { SoundAndHapticManager(applicationContext) }

            DarkGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkGoBackground
                ) {
                    DarkGoNavGraph(soundManager = soundManager)
                }
            }
        }
    }
}
