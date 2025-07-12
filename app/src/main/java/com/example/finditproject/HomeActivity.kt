package com.example.finditproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finditproject.ui.theme.FindITProjectTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FindITProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen()
                }
            }
        }
    }

    @Composable
    fun HomeScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    startActivity(Intent(this@HomeActivity, RegisterObjectActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("물건 등록")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    startActivity(Intent(this@HomeActivity, ViewObjectsActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("물건 확인")
            }
        }
    }
}
