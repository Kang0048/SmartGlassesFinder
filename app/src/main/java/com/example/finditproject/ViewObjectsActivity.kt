package com.example.finditproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DetectedObject(
    val name: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)

class ViewObjectsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DetectedMatchesScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectedMatchesScreen() {
    val storage = remember { FirebaseStorage.getInstance() }
    val folderRef = remember { storage.reference.child("detected_matches") }

    var objects by remember { mutableStateOf(listOf<DetectedObject>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun fetchFromStorage() {
        loading = true
        error = null
        try {
            val result = folderRef.listAll().await()
            val list = result.items.map { itemRef ->
                val url = itemRef.downloadUrl.await().toString()
                val fileName = itemRef.name
                val ts = fileName.substringBefore("_", "").toLongOrNull() ?: 0L
                DetectedObject(fileName, url, ts)
            }
            objects = list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            error = e.message ?: "알 수 없는 오류"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { fetchFromStorage() }

    val bg = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A0E27),
            Color(0xFF0F2247),
            Color(0xFF0C2E5A)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마지막 위치 확인", color = Color.White) },
                actions = {
                    TextButton(
                        onClick = { scope.launch { fetchFromStorage() } },
                        enabled = !loading
                    ) {
                        Text(
                            if (loading) "불러오는 중..." else "사진 가져오기",
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column {
                if (error != null) {
                    Text("에러: $error", color = Color.Red)
                    Spacer(Modifier.height(8.dp))
                }
                if (!loading && objects.isEmpty()) {
                    Text("아직 불러온 사진이 없어요.", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(objects) { item ->
                        ObjectItemViewStyled(item)
                    }
                }
            }
        }
    }
}


@Composable
fun ObjectItemViewStyled(item: DetectedObject) {
    val cardColor = Color.White.copy(alpha = 0.08f)
    val borderColor = Color.White.copy(alpha = 0.22f)

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (item.name.isNotBlank()) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = "사진",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}