package com.example.bondoc_rpggame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.bondoc_rpggame.userinterface.GameScreen
import com.example.bondoc_rpggame.ui.theme.BONDOC_RPGGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BONDOC_RPGGameTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    GameScreen()
                }
            }
        }
    }
}
