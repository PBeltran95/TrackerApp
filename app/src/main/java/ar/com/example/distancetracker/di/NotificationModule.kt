package ar.com.example.distancetracker.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ar.com.example.distancetracker.R
import ar.com.example.distancetracker.application.AppConstants.ACTION_NAVIGATE_TO_MAPS_FRAGMENT
import ar.com.example.distancetracker.application.AppConstants.NOTIFICATION_CHANNEL_ID
import ar.com.example.distancetracker.application.AppConstants.PENDING_INTENT_REQUEST_CODE
import ar.com.example.distancetracker.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun providesPendingIntent(@ApplicationContext context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, PENDING_INTENT_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                this.action = ACTION_NAVIGATE_TO_MAPS_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    @ServiceScoped
    @Provides
    fun providesNotificationBuilder(@ApplicationContext context: Context,
                                    pendingIntent: PendingIntent) =
        NotificationCompat.Builder(
        context,NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_run)
        .setContentIntent(pendingIntent)


    @ServiceScoped
    @Provides
    fun providesNotificationManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}