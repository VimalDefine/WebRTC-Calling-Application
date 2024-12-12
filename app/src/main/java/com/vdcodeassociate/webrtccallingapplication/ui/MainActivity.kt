package com.vdcodeassociate.webrtccallingapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vdcodeassociate.webrtccallingapplication.R
import com.vdcodeassociate.webrtccallingapplication.repository.MainRepository
import com.vdcodeassociate.webrtccallingapplication.adapters.ActiveUserAdapter
import com.vdcodeassociate.webrtccallingapplication.databinding.ActivityMainBinding
import com.vdcodeassociate.webrtccallingapplication.service.MainServiceRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ActiveUserAdapter.Listener {

    // view binding
    private lateinit var binding: ActivityMainBinding
    private var username: String? = null

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var mainServiceRepository: MainServiceRepository

    // Adapters
    private lateinit var recyclerAdapter: ActiveUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.apply {

            // recycler view setup
            setupRecyclerView()

            // logout click listener
            logoutImg.setOnClickListener {
                mainRepository.logout(
                    mainRepository.getLoggedUsername()
                ) { isDone, _ ->
                    if (!isDone) {
                        Toast.makeText(this@MainActivity, "Error in logout", Toast.LENGTH_SHORT).show()
                    } else {
                        this@MainActivity.finish()
                    }
                }
            }

            username = mainRepository.getLoggedUsername()
            if (username.isNullOrEmpty()) {
                finish()
            } else {
                userNameTv.text = getString(R.string.logout_description_value, username)
            }

            // observe other user states
            subscribeObservers()
            // start foreground service to listen negotiations and calls
            startMyService()
        }
    }

    private fun subscribeObservers() {
        mainRepository.observeUserStates {
            Log.d("MAIN_TAG", "Subscribed Observers: $it")
            recyclerAdapter.updateList(it)
        }
    }

    private fun startMyService() {
        if (checkPermission()) {
            mainServiceRepository.startService(username!!)
        }
    }

    // recycler view setup
    private fun setupRecyclerView() {
        recyclerAdapter = ActiveUserAdapter(this)
        binding.apply {
            mainRecyclerView.apply {
                adapter = recyclerAdapter
                layoutManager = LinearLayoutManager(this@MainActivity)
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
                ), 1122
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1122) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mainServiceRepository.startService(username!!)
            } else {
                AlertDialog.Builder(this)
                    .setMessage("We need the camera permission to take photos.")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this, arrayOf(
                                Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
                            ), 1122
                        )
                    }.show()
            }
        }
    }

    override fun onVideoCallClicked(username: String) {
        // checking permission for mic & camera
        if (checkPermission()) {
            mainRepository.sendConnectionRequest(username,true) {
                if (it) {

                }
            }
        } else {
            Toast.makeText(this, "Camera & mic permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAudioCallClicked(username: String) {
        // checking permission for mic & camera
        if (checkPermission()) {
            mainRepository.sendConnectionRequest(username,true) {
                if (it) {

                }
            }
        } else {
            Toast.makeText(this, "Camera & mic permission required!", Toast.LENGTH_SHORT).show()
        }
    }
}