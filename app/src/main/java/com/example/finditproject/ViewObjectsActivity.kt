package com.example.finditproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class DetectedObject(
    val name: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)

class ViewObjectsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        setContent {
            var objects by remember { mutableStateOf(listOf<DetectedObject>()) }

            // Firebase Firestore에서 데이터 가져오기
            LaunchedEffect(uid) {
                uid?.let {
                    FirebaseFirestore.getInstance()
                        .collection("detected_objects")
                        .whereEqualTo("userId", uid)
                        .get()
                        .addOnSuccessListener { result ->
                            val list = result.mapNotNull { doc ->
                                doc.toObject(DetectedObject::class.java)
                            }
                            objects = list
                        }
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(objects) { item ->
                        ObjectItemView(item)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ObjectItemView(item: DetectedObject) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "물건 이름: ${item.name}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Image(
            painter = rememberAsyncImagePainter(item.imageUrl),
            contentDescription = "사진",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Crop
        )
    }
}
