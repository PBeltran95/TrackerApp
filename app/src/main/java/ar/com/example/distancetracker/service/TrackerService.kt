package ar.com.example.distancetracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import ar.com.example.distancetracker.application.AppConstants.ACTION_SERVICE_START
import ar.com.example.distancetracker.application.AppConstants.ACTION_SERVICE_STOP
import ar.com.example.distancetracker.application.AppConstants.LOCATION_FASTEST_UPDATE_INTERVAL
import ar.com.example.distancetracker.application.AppConstants.LOCATION_UPDATE_INTERVAL
import ar.com.example.distancetracker.application.AppConstants.NOTIFICATION_CHANNEL_ID
import ar.com.example.distancetracker.application.AppConstants.NOTIFICATION_CHANNEL_NAME
import ar.com.example.distancetracker.application.AppConstants.NOTIFICATION_ID
import ar.com.example.distancetracker.ui.map.MapUtil
import ar.com.example.distancetracker.ui.map.MapUtil.calculateTheDistance
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val started = MutableLiveData<Boolean>()
        val startedTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()

        val locationList = MutableLiveData<MutableList<LatLng>>()
    }


    private fun setInitialValues(){
        started.postValue(false)
        startedTime.postValue(0L)
        stopTime.postValue(0L)

        locationList.postValue(mutableListOf())
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations ->
                for (location in locations){
                    updateLocationList(location)
                    notificationPeriodically()
                }
            }
        }
    }


    private fun updateLocationList(location: Location){
        val newLetLong = LatLng(location.latitude, location.longitude)
        locationList.value?.apply {
            add(newLetLong)
            locationList.postValue(this)
        }
    }


    override fun onCreate() {
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_SERVICE_START -> {
                    started.postValue(true)
                    startForegroundService()
                    startLocationUpdates()
                }
                ACTION_SERVICE_STOP -> {
                    started.postValue(false)
                    stopForegroundService()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopForegroundService() {
        removeLocationUpdates()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID
        )
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification.build())
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        val locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
        startedTime.postValue(System.currentTimeMillis())
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun notificationPeriodically() {
        notification.apply {
            setContentTitle("Distance Travelled")
            setContentText(locationList.value?.let { calculateTheDistance(it) } + "km")
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

}