package com.example.deltacleaners

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.deltacleaners.admin.navigation.AdminNavGraph
import com.example.deltacleaners.ui.theme.DeltaCleanersTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeltaCleanersTheme {
                AdminNavGraph()
            }
        }
    }
}
