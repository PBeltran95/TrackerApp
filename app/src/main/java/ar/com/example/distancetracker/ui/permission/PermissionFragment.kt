package ar.com.example.distancetracker.ui.permission

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ar.com.example.distancetracker.R
import ar.com.example.distancetracker.application.Permission.hasLocationPermission
import ar.com.example.distancetracker.application.Permission.requestLocationPermission
import ar.com.example.distancetracker.databinding.FragmentPermissionBinding
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class PermissionFragment : Fragment(R.layout.fragment_permission), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentPermissionBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPermissionBinding.bind(view)
        hideActionBar()
        buttonFunction()
    }

    private fun buttonFunction() {
        binding.btnContinue.setOnClickListener {
            if (hasLocationPermission(requireContext())) {
                findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
            } else {
                requestLocationPermission(this)
            }
        }
    }

    private fun hideActionBar() {
        (activity as AppCompatActivity).supportActionBar?.hide()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            SettingsDialog.Builder(requireActivity()).build().show()
        }else{
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
    }


}