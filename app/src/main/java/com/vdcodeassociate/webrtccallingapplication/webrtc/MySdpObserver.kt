package com.vdcodeassociate.webrtccallingapplication.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

// SDP (Session Description Protocol)
open class MySdpObserver: SdpObserver {

    override fun onCreateSuccess(p0: SessionDescription?) {
    }

    override fun onSetSuccess() {
    }

    override fun onCreateFailure(p0: String?) {
    }

    override fun onSetFailure(p0: String?) {
    }
}