package com.example.finditproject
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DetectedObject(
    val name: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)

data class DetectedFolder(
    val name: String,
    val ref: StorageReference
)

class ViewObjectsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DetectedMatchesScreen() }
    }
}

private enum class ViewMode { Folders, Images }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectedMatchesScreen() {
    val storage = remember { FirebaseStorage.getInstance() }
    val ROOT_PATH = ""
    val rootRef = remember(ROOT_PATH) {
        if (ROOT_PATH.isBlank()) storage.reference else storage.reference.child(ROOT_PATH)
    }

    var mode by remember { mutableStateOf(ViewMode.Folders) }
    var folders by remember { mutableStateOf(listOf<DetectedFolder>()) }
    var objects by remember { mutableStateOf(listOf<DetectedObject>()) }
    var selectedFolder by remember { mutableStateOf<DetectedFolder?>(null) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun fetchFolders() {
        loading = true
        error = null
        try {
            val result = rootRef.listAll().await()
            val list = result.prefixes.filter{it.name.lowercase() != "objects"}
                .map { prefixRef ->
                DetectedFolder(name = prefixRef.name, ref = prefixRef)
            }.sortedBy { it.name.lowercase() }
            folders = list
        } catch (e: Exception) {
            error = e.message ?: "알 수 없는 오류(폴더 로드)"
        } finally {
            loading = false
        }
    }

    suspend fun fetchImages(folder: DetectedFolder) {
        loading = true
        error = null
        try {
            val result = folder.ref.listAll().await()
            val list = result.items.map { itemRef ->
                val url = itemRef.downloadUrl.await().toString()
                val fileName = itemRef.name
                val ts = fileName.substringBefore("_", "").toLongOrNull() ?: 0L
                DetectedObject(fileName, url, ts)
            }.sortedByDescending { it.timestamp }
            objects = list
        } catch (e: Exception) {
            error = e.message ?: "알 수 없는 오류(이미지 로드)"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchFolders()
    }

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
                title = {
                    Text(
                        when (mode) {
                            ViewMode.Folders -> "폴더 선택"
                            ViewMode.Images -> selectedFolder?.name ?: "사진"
                        },
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (mode == ViewMode.Images) {
                        IconButton(onClick = {
                            mode = ViewMode.Folders
                            selectedFolder = null
                            // 뒤로 가면 폴더 리스트는 그대로 두고 필요 시 당겨새로고침만
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.White)
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (mode == ViewMode.Folders) {
                                    fetchFolders()
                                } else {
                                    selectedFolder?.let { fetchImages(it) }
                                }
                            }
                        },
                        enabled = !loading
                    ) {
                        Text(if (loading) "불러오는 중..." else "새로고침", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent)
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
            Column(modifier = Modifier.fillMaxSize()) {
                if (error != null) {
                    Text("에러: $error", color = Color.Red)
                    Spacer(Modifier.height(8.dp))
                }

                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(top = 24.dp)) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                when (mode) {
                    ViewMode.Folders -> {
                        if (!loading && folders.isEmpty()) {
                            Text("표시할 폴더가 없어요.", color = Color.White)
                            Spacer(Modifier.height(12.dp))
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(folders) { folder ->
                                FolderCardStyled(
                                    folderName = folder.name,
                                    onClick = {
                                        selectedFolder = folder
                                        mode = ViewMode.Images
                                        scope.launch { fetchImages(folder) }
                                    }
                                )
                            }
                        }
                    }

                    ViewMode.Images -> {
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
    }
}

@Composable
fun FolderCardStyled(folderName: String, onClick: () -> Unit) {
    val cardColor = Color.White.copy(alpha = 0.08f)
    val borderColor = Color.White.copy(alpha = 0.22f)

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = "폴더",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = folderName,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
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
