package com.example.finditproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.finditproject.ui.theme.FindITProjectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class RegisterObjectActivity : ComponentActivity() {

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private val imageUriList = mutableStateListOf<Uri>()

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            photoUri?.let { uri ->
                // 찍은 사진을 리스트에 추가
                imageUriList.add(uri)
            }
        }
    }

    private val imageUriState = mutableStateOf<Uri?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission()

        setContent {
            FindITProjectTheme {
                // 홈과 동일한 네이비 그라데이션 & 글래스 카드
                val bg = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF0F2247),
                        Color(0xFF0C2E5A)
                    )
                )
                val cardColor = Color.White.copy(alpha = 0.08f)
                val borderColor = Color.White.copy(alpha = 0.22f)

                var objectName by remember { mutableStateOf("") }
                val context = LocalContext.current

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("물건 등록", color = Color.White) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    },
                    containerColor = Color.Transparent
                ) { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bg)
                            .padding(inner)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = cardColor,
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(20.dp),
                            tonalElevation = 0.dp,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "내 물건 정보를 등록하세요",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )

                                OutlinedTextField(
                                    value = objectName,
                                    onValueChange = { objectName = it },
                                    label = { Text("물품 이름", color = Color.White.copy(alpha = 0.8f)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                                        disabledContainerColor = Color.White.copy(alpha = 0.04f),
                                        focusedIndicatorColor = Color.White.copy(alpha = 0.45f),
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.25f),
                                        cursorColor = Color.White,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                // 사진 촬영 버튼 (글래스 버튼)
                                Button(
                                    onClick = { takePicture() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, borderColor),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text("사진 촬영")
                                }

                                // 미리보기
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(imageUriList) { uri ->
                                        Box{
                                            val bitmap = remember(uri) {
                                                @Suppress("DEPRECATION")
                                                MediaStore.Images.Media.getBitmap(contentResolver, uri)
                                            }
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                            )
                                            IconButton(
                                                onClick = { imageUriList.remove(uri) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd) // 이미지 오른쪽 상단
                                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                                    .size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "삭제",
                                                    tint = Color.White
                                                )
                                            }
                                        }



                                    }
                                }


                                // 등록 버튼 (가득 폭, 글래스 버튼)
                                Button(
                                    onClick = {
                                        if (objectName.isBlank() || imageUriList.isEmpty()) {
                                            Toast.makeText(context, "이름과 사진을 입력해주세요", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        uploadToFirebase(objectName)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, borderColor),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text("등록하기")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun takePicture() {
        photoFile = File.createTempFile("object_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        takePictureLauncher.launch(intent)
    }


    private fun uploadToFirebase(name: String) {
        if (imageUriList.isEmpty()) return
        val timestamp = System.currentTimeMillis()

        imageUriList.forEach { uri ->
            val fileRef = FirebaseStorage.getInstance().reference
                .child("objects/$name/$timestamp.jpg") // 물건 이름 기준 폴더

            val inputStream = contentResolver.openInputStream(uri) ?: return@forEach
            val uploadTask = fileRef.putStream(inputStream)

            uploadTask.addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val metadata = hashMapOf(
                        "name" to name,
                        "timestamp" to timestamp,
                        "imageUrl" to downloadUri.toString()
                    )
                    FirebaseFirestore.getInstance().collection("detected_objects")
                        .add(metadata)
                        .addOnSuccessListener {
                            Toast.makeText(this, "물건 등록 완료!", Toast.LENGTH_SHORT).show()
                            // 업로드 완료 후 리스트 초기화
                            imageUriList.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Firestore 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
