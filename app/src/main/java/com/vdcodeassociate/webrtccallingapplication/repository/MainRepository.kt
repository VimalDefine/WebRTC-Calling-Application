package com.vdcodeassociate.webrtccallingapplication.repository

import android.util.Log
import com.vdcodeassociate.webrtccallingapplication.firebaseClient.FirebaseClient
import com.vdcodeassociate.webrtccallingapplication.model.User
import com.vdcodeassociate.webrtccallingapplication.prefdata.PreferenceImpl
import com.vdcodeassociate.webrtccallingapplication.utils.DataModel
import com.vdcodeassociate.webrtccallingapplication.utils.DataModelType
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val firebaseClient: FirebaseClient,
    private val preferenceImpl: PreferenceImpl
) {

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
}