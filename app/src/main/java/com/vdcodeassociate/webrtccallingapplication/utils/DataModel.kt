package com.vdcodeassociate.webrtccallingapplication.utils

enum class DataModelType {
    StartAudioCall, StartVideoCall, Offer, Answer, IceCandidates, EndCall
}

data class DataModel(
    val sender: String? = null,
    val target: String,
    val type: DataModelType,
    val data: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

fun DataModel.isValid(): Boolean {
    return System.currentTimeMillis() - this.timestamp < 60000
}