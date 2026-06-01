package com.example.deltacleaners

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.example.deltacleaners.ui.navigation.NavGraph
import com.example.deltacleaners.ui.theme.DeltaCleanersTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            DeltaCleanersTheme {
                NavGraph()
            }
        }
    }
}
