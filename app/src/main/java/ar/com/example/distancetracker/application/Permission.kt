package ar.com.example.distancetracker.application

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import ar.com.example.distancetracker.application.AppConstants.PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE
import ar.com.example.distancetracker.application.AppConstants.PERMISSION_LOCATION_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions

object Permission {

    fun hasLocationPermission(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun requestLocationPermission(fragment: Fragment){
        EasyPermissions.requestPermissions(
            fragment,
            "This application cannot work without Location Permission",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
           return EasyPermissions.hasPermissions(
               context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
           )
        }
        return true
    }

    fun requestBackgroundLocationPermission(fragment: Fragment){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                fragment,
                "Background location permission is essential to this application.",
                PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}