package ar.com.example.distancetracker.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ar.com.example.distancetracker.R
import ar.com.example.distancetracker.application.AppConstants.ACTION_SERVICE_START
import ar.com.example.distancetracker.application.AppConstants.ACTION_SERVICE_STOP
import ar.com.example.distancetracker.application.Permission.hasBackgroundLocationPermission
import ar.com.example.distancetracker.application.Permission.requestBackgroundLocationPermission
import ar.com.example.distancetracker.data.model.Result
import ar.com.example.distancetracker.databinding.FragmentMapsBinding
import ar.com.example.distancetracker.service.TrackerService
import ar.com.example.distancetracker.ui.map.MapUtil.calculateElapsedTime
import ar.com.example.distancetracker.ui.map.MapUtil.calculateTheDistance
import ar.com.example.distancetracker.ui.map.MapUtil.setCameraPosition
import ar.com.example.distancetracker.utils.ExtensionFunctions.disable
import ar.com.example.distancetracker.utils.ExtensionFunctions.enable
import ar.com.example.distancetracker.utils.ExtensionFunctions.hide
import ar.com.example.distancetracker.utils.ExtensionFunctions.show
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks,
    GoogleMap.OnMarkerClickListener{

    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap
    private var locationList = mutableListOf<LatLng>()
    private var startTime = 0L
    private var stopTime = 0L
    private var  polyLineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        binding = FragmentMapsBinding.bind(view)
        setButtons()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setButtons() {
        with(binding){
            btnStart.setOnClickListener {
                onStartButtonClick()
            }
            btnReset.setOnClickListener {
                onResetButtonClick()
            }
            btnStop.setOnClickListener {
                onStopButtonClick()
            }
        }
    }



    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMarkerClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
        }
        observeTrackerService()
    }

    private fun observeTrackerService(){
        TrackerService.locationList.observe(viewLifecycleOwner, {
            if (it != null){
                locationList = it
                if (locationList.size > 1){
                    binding.btnStop.enable()
                }
                drawPolyline()
                followPolyline()
            }
        })
        TrackerService.started.observe(viewLifecycleOwner, {
            if (it == true){
                binding.btnStart.disable()
                binding.btnStop.enable()
                binding.btnStop.show()
                binding.tvHint.hide()
            }
        })
        TrackerService.startedTime.observe(viewLifecycleOwner, {
            startTime = it
        })
        TrackerService.stopTime.observe(viewLifecycleOwner, {
            stopTime = it
            if (stopTime != 0L){
                showBiggerPicture()
                displayResults()
            }
        })
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        for (location in locationList){
            bounds.include(location)
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100),
            2000, null)
        map.animateCamera(CameraUpdateFactory.zoomTo(17f), 2000, null)
        addMarker(locationList.first())
        addMarker(locationList.last())
    }

    private fun addMarker(position:LatLng){
        val marker = map.addMarker(MarkerOptions().position(position))
        markerList.add(marker!!)
    }

    private fun drawPolyline(){
        val polyLine = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
        polyLineList.add(polyLine)
    }

    private fun followPolyline(){
        if (locationList.isNotEmpty()){
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(setCameraPosition(
                    locationList.last()
                )), 1000, null
            )
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.tvHint.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500)
            binding.tvHint.hide()
            binding.btnStart.show()
        }
        return false
    }

    private fun onStartButtonClick() {
        if (hasBackgroundLocationPermission(requireContext())){
            startCountDown()
            binding.btnStart.disable()
            binding.btnStart.hide()
            binding.btnStop.show()
        }else{ requestBackgroundLocationPermission(this) }
    }

    private fun onStopButtonClick() {
        stopForegroundService()
        binding.btnStop.hide()
        binding.btnStart.show()
    }



    private fun startCountDown() {
        binding.tvTimer.show()
        binding.btnStop.disable()
        val timer: CountDownTimer =  object : CountDownTimer(4000, 1000 ){
            override fun onTick(millisUntilFinished: Long) {
                val currentSecond = millisUntilFinished / 1000
                if (currentSecond.toString() == "0"){
                    binding.tvTimer.text = getString(R.string.go_text)
                    binding.tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }else{
                    binding.tvTimer.text = currentSecond.toString()
                    binding.tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }

            override fun onFinish() {
                binding.tvTimer.hide()
                sendActionCommandToService(ACTION_SERVICE_START)
            }

        }
        timer.start()
    }

    private fun stopForegroundService() {
        binding.btnStart.disable()
        sendActionCommandToService(ACTION_SERVICE_STOP)
    }

    private fun sendActionCommandToService(action:String){
        Intent(requireContext(), TrackerService::class.java).apply {
            this.action = action
            requireContext().startService(this)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            SettingsDialog.Builder(requireActivity()).build().show()
        }else{ requestBackgroundLocationPermission(this) }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClick()
    }

    private fun displayResults(){
        val result = Result(calculateTheDistance(locationList), calculateElapsedTime(startTime, stopTime))
        lifecycleScope.launch {
            delay(2500)
            val directions = MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(directions)
            binding.btnStart.apply {
                hide()
                enable()
            }
            binding.btnStop.hide()
            binding.btnReset.show()
        }
    }

    private fun onResetButtonClick() {
        mapReset()
    }

    @SuppressLint("MissingPermission")
    private fun mapReset() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnownLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )
            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                setCameraPosition(lastKnownLocation)
            ))
            for (polyLine in polyLineList){
                polyLine.remove()
            }
            for (marker in markerList){
                marker.remove()
            }
            locationList.clear()
            markerList.clear()
            binding.btnReset.hide()
            binding.btnStart.show()
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }

}