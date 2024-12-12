package com.vdcodeassociate.webrtccallingapplication.service

import android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vdcodeassociate.webrtccallingapplication.R
import com.vdcodeassociate.webrtccallingapplication.repository.MainRepository
import com.vdcodeassociate.webrtccallingapplication.utils.DataModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service(), MainRepository.Listener {

    private val TAG = "MainService"

    private var isServiceRunning = false
    private var username : String? = null

    @Inject
    lateinit var mainRepository: MainRepository

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TAG_PIG_099", "this reached 2!")

        intent?.let { incomingIntent ->
            when (incomingIntent.action) {
                MainServiceActions.START_SERVICE.name -> {
                    Log.d("TAG_PIG_099", "this reached 3!")
                    handleStartService(incomingIntent)
                }

                else -> {

                }
            }
        }
        return START_STICKY
    }

    private fun handleStartService(incomingIntent: Intent) {
        // start foreground service
        if (!isServiceRunning) {
            Log.d("TAG_PIG_099", "this reached 4!")
            isServiceRunning = true
            username = incomingIntent.getStringExtra("username")
            startServiceWithNotification()
            // start service here
            mainRepository.listener = this
            mainRepository.initFirebase()
        } else {
            Unit
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startServiceWithNotification() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "channel1", "foreground", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
            val notification = NotificationCompat.Builder(
                this, "channel1"
            ).setSmallIcon(R.mipmap.ic_launcher)
            startForeground(1, notification.build())
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onLatestEventReceived(event: DataModel) {
        Log.d(TAG, "onLatestEventReceived : $event")
    }
}