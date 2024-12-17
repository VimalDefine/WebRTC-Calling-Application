package com.vdcodeassociate.webrtccallingapplication.firebaseClient

import android.util.Log
import com.vdcodeassociate.webrtccallingapplication.utils.Constants.FIREBASE_LATEST_EVENT
import com.vdcodeassociate.webrtccallingapplication.utils.Constants.FIREBASE_PASSWORD
import com.vdcodeassociate.webrtccallingapplication.utils.Constants.FIREBASE_STATUS
import com.vdcodeassociate.webrtccallingapplication.utils.Constants.FIREBASE_TYPE
import com.vdcodeassociate.webrtccallingapplication.utils.Constants.FIREBASE_USERNAME
import com.vdcodeassociate.webrtccallingapplication.utils.Constants.USERS_FIREBASE
import com.example.webrtccallingapplication.utils.UserStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.vdcodeassociate.webrtccallingapplication.model.User
import com.vdcodeassociate.webrtccallingapplication.model.LatestEvent
import com.vdcodeassociate.webrtccallingapplication.prefdata.PreferenceImpl
import com.vdcodeassociate.webrtccallingapplication.utils.DataModel
import com.vdcodeassociate.webrtccallingapplication.utils.DataModelType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val dbRef: DatabaseReference,
    private val preferenceImpl: PreferenceImpl,
    private val gson: Gson
) {

    private val TAG = "FirebaseClient"

    private var currentUserName: String? = null
    private fun setUserName(username: String) {
        this.currentUserName = username
        preferenceImpl.saveUserLogin(username)
    }

    // user login & signup
    fun login(username: String, password: String, done: (Boolean, String?) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // user data path
                val userDataPath = "$USERS_FIREBASE/$username"

                // check for user existence
                if (snapshot.child(USERS_FIREBASE).hasChild(username)) {
                    // user exists, will update status
                    val dbPassword = snapshot.child(userDataPath).child(FIREBASE_PASSWORD).value
                    if (dbPassword == password) {

                        // prepare child data
                        val childData = mapOf(
                            FIREBASE_STATUS to UserStatus.ONLINE
                        )

                        // update status
                        dbRef.child(userDataPath).updateChildren(childData).addOnSuccessListener {
                            done(true, null)
                            setUserName(username)
                        }.addOnFailureListener {
                            done(false, it.message)
                        }
                    } else {
                        done(false, "Wrong Password!")
                    }
                } else {
                    // user doesn't exists, will create new user
                    // prepare data
                    val childData = mapOf(
                        FIREBASE_USERNAME to username,
                        FIREBASE_PASSWORD to password,
                        FIREBASE_STATUS to UserStatus.ONLINE,
                        "$FIREBASE_LATEST_EVENT/$FIREBASE_TYPE" to ""
                    )

                    // add data
                    dbRef.child(userDataPath).updateChildren(childData).addOnSuccessListener {
                        done(true, null)
                        setUserName(username)
                    }.addOnFailureListener {
                        done(false, it.message)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                done(false, error.message)
            }
        })
    }

    // logout user
    fun logout(username: String, done: (Boolean, String?) -> Unit) {

        // user data path
        val userDataPath = "$USERS_FIREBASE/$username"
        val childData = mapOf(
            FIREBASE_STATUS to UserStatus.OFFLINE,
        )

        // update data
        dbRef.child(userDataPath).updateChildren(childData).addOnSuccessListener {
            done(true, null)
            setUserName(username)
            preferenceImpl.saveUserLogin("")
        }.addOnFailureListener {
            done(false, it.message)
        }
    }

    // observer user states when any changes is done
    fun observeUserStates(status: (List<User>) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val usersList = mutableListOf<User>()

                // Get the "firebase_users" node
                val firebaseUsersSnapshot = snapshot.child("firebase_users")

                // Iterate through the users in the "firebase_users" node
                for (userSnapshot in firebaseUsersSnapshot.children) {
                    val name = userSnapshot.key

                    // If name is not null, proceed with parsing the user's data
                    if (name != null) {
                        val userMap = userSnapshot.value as? Map<*, *>
                        val latestEventMap = userMap?.get("latest_event") as? Map<*, *>
                        val latestEvent = LatestEvent(
                            type = latestEventMap?.get("type") as? String ?: ""
                        )
                        val userStatus = userMap?.get("status") as? String ?: "OFFLINE"

                        // Create the user object and add it to the list
                        val user = User(name, latestEvent, userStatus)
                        usersList.add(user)
                    }
                }

                // adding users in list
                if (usersList.size > 0) {
                    status(usersList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun subscribeForLatestEvents(listener: Listener) {
        try {
            // user data path
            val userDataPath = "$USERS_FIREBASE/${preferenceImpl.getLoggedUsername()}"
            dbRef.child(userDataPath).child("$FIREBASE_LATEST_EVENT/$FIREBASE_TYPE").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, snapshot.value.toString())
                        val event = try {
                            gson.fromJson(snapshot.value.toString(), DataModel::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.d(TAG, e.message.toString())
                            null
                        }
                        event?.let {
                            listener.onLatestEventReceived(event)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessageToOtherClients(message: DataModel, success: (Boolean) -> Unit) {
        val convertMessage = gson.toJson(message)

        val childData = mapOf(
            "$FIREBASE_LATEST_EVENT/$FIREBASE_TYPE" to convertMessage
        )

        dbRef.child(USERS_FIREBASE).child(message.target).updateChildren(childData)
            .addOnSuccessListener {
                success(true)
            }.addOnFailureListener {
                success(false)
            }
    }

    interface Listener {
        fun onLatestEventReceived(event : DataModel)
    }
}