package com.vdcodeassociate.webrtccallingapplication.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.vdcodeassociate.webrtccallingapplication.R
import com.vdcodeassociate.webrtccallingapplication.databinding.ActivityCallBinding
import com.vdcodeassociate.webrtccallingapplication.databinding.ActivityMainBinding
import com.vdcodeassociate.webrtccallingapplication.service.MainServiceRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityCallBinding

    private var target: String? = null
    private var isVideoCall: Boolean = true
    private var isCaller: Boolean = true

    @Inject
    lateinit var mainServiceRepository: MainServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.apply {

            // init
            init()

            // on click listeners
            onClickListeners()
        }
    }

    // on click listeners
    private fun onClickListeners() {
        binding.apply {

            // end a call
            endCallButton.setOnClickListener {
                finish()
            }
        }
    }

    private fun init() {
        intent.getStringExtra("target")?.let {
            this.target = it
        } ?: kotlin.run {
            finish()
        }

        isVideoCall = intent.getBooleanExtra("isVideoCall", true)
        isCaller = intent.getBooleanExtra("isCaller", true)

        binding.apply {
            if (!isVideoCall) {
                toggleCameraButton.isVisible = false
                screenShareButton.isVisible = false
                switchCameraButton.isVisible = false
            }

            mainServiceRepository.setupViews(isVideoCall, isCaller, target)
        }
    }
}