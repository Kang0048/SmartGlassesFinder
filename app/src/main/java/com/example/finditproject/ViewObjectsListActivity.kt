package com.example.finditproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.finditproject.ui.theme.FindITProjectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewObjectsListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FindITProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ObjectListScreen()
                }
            }
        }
    }
}

@Composable
fun ObjectListScreen() {
    val bg = Brush.linearGradient(
        colors = listOf(Color(0xFF0A0E27), Color(0xFF0F2247), Color(0xFF0C2E5A))
    )
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var groupedObjects by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    val context = LocalContext.current
    // Firebase에서 데이터 가져오기
    LaunchedEffect(Unit) {
        if (uid == null) return@LaunchedEffect
        db.collection("detected_objects")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                val grouped = snapshot.documents.groupBy(
                    { it.getString("name") ?: "Unknown" },
                    { it.getString("imageUrl") ?: "" }
                )
                groupedObjects = grouped
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "데이터 불러오기 실패",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedObjects.forEach { (name, images) ->
                item {
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(images) { imageUrl ->
                            if (imageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
                                    contentDescription = name,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
