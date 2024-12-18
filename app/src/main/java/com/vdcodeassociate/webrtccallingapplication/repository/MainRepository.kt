package com.vdcodeassociate.webrtccallingapplication.repository

import android.util.Log
import com.example.webrtccallingapplication.utils.UserStatus
import com.vdcodeassociate.webrtccallingapplication.firebaseClient.FirebaseClient
import com.vdcodeassociate.webrtccallingapplication.model.User
import com.vdcodeassociate.webrtccallingapplication.prefdata.PreferenceImpl
import com.vdcodeassociate.webrtccallingapplication.utils.DataModel
import com.vdcodeassociate.webrtccallingapplication.utils.DataModelType
import com.vdcodeassociate.webrtccallingapplication.webrtc.MyPeerObserver
import com.vdcodeassociate.webrtccallingapplication.webrtc.WebRTCClient
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val preferenceImpl: PreferenceImpl,
    private val webRTCClient: WebRTCClient
) : WebRTCClient.Listener {

    private var target : String? = null
    var listener: Listener? = null

    fun login(username: String, password: String, done: (Boolean, String?) -> Unit) {
        firebaseClient.login(username, password, done)
    }

    fun logout(username: String, done: (Boolean, String?) -> Unit) {
        firebaseClient.logout(username, done)
    }

    fun observeUserStates(status: (List<User>) -> Unit) {
        firebaseClient.observeUserStates(status)
    }

    fun getLoggedUsername() = preferenceImpl.getLoggedUsername()

    fun initFirebase() {
        firebaseClient.subscribeForLatestEvents(object : FirebaseClient.Listener {
            override fun onLatestEventReceived(event: DataModel) {
                listener?.onLatestEventReceived(event)
//                when (event.type) {
//
//                    event -> Unit
//                }
            }
        })
    }

    fun sendConnectionRequest(targetUser: String, isVideoCall: Boolean, success: (Boolean) -> Unit) {
        firebaseClient.sendMessageToOtherClients(
            DataModel(
                sender = getLoggedUsername(),
                type = if (!isVideoCall) DataModelType.StartAudioCall else DataModelType.StartVideoCall,
                target = targetUser
            ), success
        )
    }

    fun setTarget(target: String?) {
        this.target = target
    }

    fun startCall() {
        Log.d("TAG_MAIN_RPO", "Start Call via service.")
    }

    interface Listener {
        fun onLatestEventReceived(event : DataModel)
    }

    fun initWebrtcClient(username: String) {
        webRTCClient.listener = this
        webRTCClient.initWebRTCClient(username, object : MyPeerObserver() {
            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                // notify creator that there is a new stream available
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                p0?.let {
                    webRTCClient.sendIceCandidate(target!!, it)
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                super.onConnectionChange(newState)
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    // 1. change my status to in call
                    // changeMyStatus(UserStatus.IN_CALL)
                    // 2. clear latest event inside my user section in firebase database
                    // firebaseClient.clearLatestEvent()
                }
            }
        })
    }

    override fun omTransferEventSocket(data: DataModel) {

    }
}