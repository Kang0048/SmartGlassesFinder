package com.example.finditproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finditproject.ui.theme.FindITProjectTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FindITProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        onRegisterClick = {
                            startActivity(Intent(this, RegisterObjectActivity::class.java))
                        },
                        onViewClick = {
                            startActivity(Intent(this, ViewObjectsActivity::class.java))
                        },
                        onViewListClick = {
                            startActivity(Intent(this, ViewObjectsListActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onRegisterClick: () -> Unit = {},
    onViewClick: () -> Unit = {},
    onViewListClick :() -> Unit = {},
) {
    // 스마트 안경 분위기의 네온+글래스 느낌 그라데이션 배경
    val bg = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A0E27),
            Color(0xFF0F2247),
            Color(0xFF0C2E5A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 타이틀
            Column {
                // 로고 느낌의 원형 글래스 포인트
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👓", fontSize = 24.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "FindIT Glass",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "스마트 안경과 함께 물건을 등록하고,\n필요할 때 바로 찾아보세요.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                )
            }

            // 메인 액션 카드 2개
            Column {
                ActionGlassCard(
                    title = "물건 등록",
                    subtitle = "내 물건을 안경에 등록",
                    icon = Icons.Outlined.AddAPhoto,
                    accent = Color.White.copy(alpha = 0.08f), //
                    onClick = onRegisterClick
                )
                Spacer(Modifier.height(20.dp))
                ActionGlassCard(
                    title = "물건 목록",
                    subtitle = "등록한 모든 물건 확인",
                    icon = Icons.Filled.MenuBook, // 책 느낌 아이콘
                    accent = Color.White.copy(alpha = 0.08f),
                    onClick = onViewListClick
                )
                Spacer(Modifier.height(20.dp))
                ActionGlassCard(
                    title = "물건 찾기",
                    subtitle = "감지된 사진을 확인",
                    icon = Icons.Outlined.Search,
                    accent = Color.White.copy(alpha = 0.08f), // 카드와 동일
                    onClick = onViewClick
                )

            }
        }

    }
}

@Composable
fun ActionGlassCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    // 유리(글래스) 카드 느낌: 반투명 + 살짝 테두리 + 소프트 그림자
    val cardColor = Color.White.copy(alpha = 0.08f)
    val borderColor = Color.White.copy(alpha = 0.22f)

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 배지
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f)), // 반투명 흰색 배경
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White, // 아이콘도 흰색
                    modifier = Modifier.size(28.dp)
                )
            }


            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.78f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            // 우측 가이드 칩(선택)
            AssistChip(
                onClick = onClick,
                label = {
                    Text("클릭", color = Color.White)
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    labelColor = Color.White
                )
            )
        }
    }
}
