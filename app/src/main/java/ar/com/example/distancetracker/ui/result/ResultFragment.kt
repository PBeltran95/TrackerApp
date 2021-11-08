package ar.com.example.distancetracker.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import ar.com.example.distancetracker.R
import ar.com.example.distancetracker.databinding.FragmentResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ResultFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentResultBinding
    private val args : ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        parseDataWithTv()
        shareButton()
        return binding.root
    }

    private fun shareButton() {
        binding.btnShare.setOnClickListener {
            shareResult()
        }
    }

    private fun shareResult() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I went ${args.result.distance} km in ${args.result.time}!")
        }
        startActivity(shareIntent)
    }

    private fun parseDataWithTv() {
        binding.tvDistanceValue.text = getString(R.string.distance_value, args.result.distance)
        binding.tvTimeValue.text = args.result.time
    }
    //R.layout.fragment_result
}