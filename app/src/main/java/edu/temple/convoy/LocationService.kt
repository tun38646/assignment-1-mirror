package edu.temple.convoy

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng

class LocationService : Service() {
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var notification: Notification? = null
    var handler: Handler? = null

    inner class LocationBinder : Binder() {
        fun setHandler(handler: Handler) {
            this@LocationService.handler = handler
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocationBinder()
    }

    override fun onCreate() {
        super.onCreate()

        locationManager = getSystemService(LocationManager::class.java)
        locationListener = LocationListener { location: Location ->
            if (handler != null) {
                val msg: Message = Message.obtain()
                msg.obj = LatLng(location.latitude, location.longitude)
                handler!!.sendMessage(msg)
            }
        }

        notification = NotificationCompat.Builder(this)
            .setContentTitle("Group active")
            .setContentText("Currently in group []")
            .setChannelId("default")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startForeground(1, notification);
            Log.d("Location Service", "Started")

            locationListener?.apply {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, this)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        handler = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener!!)
    }
}