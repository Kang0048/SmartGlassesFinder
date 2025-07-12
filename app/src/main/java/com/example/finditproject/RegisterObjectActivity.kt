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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUriState.value = photoUri
        }
    }

    private val imageUriState = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission()

        setContent {
            FindITProjectTheme {
                var objectName by remember { mutableStateOf("") }
                val context = LocalContext.current

                Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = objectName,
                            onValueChange = { objectName = it },
                            label = { Text("물품 이름") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(onClick = { takePicture() }) {
                            Text("사진 촬영")
                        }

                        imageUriState.value?.let { uri ->
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.height(200.dp))
                        }

                        Button(
                            onClick = {
                                if (objectName.isBlank() || imageUriState.value == null) {
                                    Toast.makeText(context, "이름과 사진을 입력해주세요", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                uploadToFirebase(objectName, imageUriState.value!!)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("등록하기")
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

    private fun uploadToFirebase(name: String, uri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()
        val fileRef = FirebaseStorage.getInstance().reference
            .child("objects/$uid/$timestamp.jpg")

        val inputStream = contentResolver.openInputStream(uri) ?: return
        val uploadTask = fileRef.putStream(inputStream)

        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val metadata = hashMapOf(
                    "userId" to uid,
                    "name" to name,
                    "timestamp" to timestamp,
                    "imageUrl" to downloadUri.toString()
                )
                FirebaseFirestore.getInstance().collection("detected_objects")
                    .add(metadata)
                    .addOnSuccessListener {
                        Toast.makeText(this, "물건 등록 완료!", Toast.LENGTH_SHORT).show()
                        finish()
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
