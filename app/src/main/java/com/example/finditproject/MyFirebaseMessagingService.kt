package com.example.finditproject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    // 1. 새 FCM 토큰이 생성될 때 호출되는 메서드
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        val db = FirebaseFirestore.getInstance()
        val tokenData = hashMapOf("fcmToken" to token)

        db.collection("tokens").document("user_token")
            .set(tokenData)
            .addOnSuccessListener {
                Log.d(TAG, "FCM 토큰이 Firestore에 성공적으로 저장되었습니다.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "FCM 토큰 저장 중 오류가 발생했습니다.", e)
            }
    }

    // 2. 푸시 알림 메시지를 수신했을 때 호출되는 메서드
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 메시지에 데이터 페이로드가 포함되어 있는지 확인
        remoteMessage.data.let {
            if (it.isNotEmpty()) {
                Log.d(TAG, "Message data payload: $it")
            }
        }


        // 메시지에 알림(notification) 페이로드가 포함되어 있는지 확인
        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            Log.d(TAG, "Message Notification Title: $title")
            Log.d(TAG, "Message Notification Body: $body")

            // 받은 알림을 사용자에게 표시
            sendNotification(title, body)
        }
    }

    // 3. 알림을 생성하고 표시하는 메서드
    private fun sendNotification(title: String?, body: String?) {
        val channelId = "fcm_default_channel"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘 설정 (필수)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0(Oreo) 이상에서는 알림 채널이 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}