package com.example.bibliounifornew.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.scale
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MeuFirebaseMessagingService : FirebaseMessagingService() {

    // É chamado quando o app recebe uma notificação estando ABERTO
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val titulo = message.notification?.title ?: message.data["title"] ?: "BiblioUnifor"
        val corpo = message.notification?.body ?: message.data["body"] ?: "Você tem uma nova notificação"

        mostrarNotificacao(titulo, corpo)
    }

    // É chamado quando o Firebase gera um novo Token exclusivo para este aparelho
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Enviar este token para o Firestore e salvar no perfil do usuário
        println("Novo token gerado: $token")
    }

    private fun calcularInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun mostrarNotificacao(titulo: String, corpo: String) {
        val intent = Intent(this, TelaRF01BemVindo::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "canal_bibliounifor"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canais de notificação são obrigatórios do Android 8 (Oreo) em diante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avisos da Biblioteca",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Calcula o tamanho ideal para o LargeIcon (evita estouro de 144MB no Canvas do SystemUI)
        val largeIcon = try {
            val resId = R.drawable.unifor_marca
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true 
            }
            BitmapFactory.decodeResource(resources, resId, options)
            
            // O SystemUI geralmente escala o ícone para ~64dp. 
            // 256px é mais que suficiente para qualquer densidade de tela e é seguro.
            val targetSize = 256
            options.inSampleSize = calcularInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
            
            val decodedBitmap = BitmapFactory.decodeResource(resources, resId, options)
            
            // Garante que o bitmap resultante seja pequeno o suficiente para o SystemUI
            if (decodedBitmap != null && (decodedBitmap.width > targetSize || decodedBitmap.height > targetSize)) {
                Bitmap.createScaledBitmap(decodedBitmap, targetSize, targetSize, true)
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            null
        }

        val notificacao = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIcon)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Random.nextInt(), notificacao)
    }
}
