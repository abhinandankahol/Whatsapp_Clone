package com.abhinandankahol.whatsappclone

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.abhinandankahol.whatsappclone.MainActivity.Companion.remoteConfig
import com.abhinandankahol.whatsappclone.databinding.ActivityChatBinding
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var list: ArrayList<MessageModel>
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var adapter: MessageAdapter
    private var image: Uri? = null
    private lateinit var name: String
    private lateinit var token: String
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        remoteConfig.fetchAndActivate().addOnSuccessListener {
            val color = remoteConfig.getString("toolbarColor")

            binding.materialToolbar.setBackgroundColor(Color.parseColor(color))

        }


        senderUid = Firebase.auth.currentUser!!.uid
        receiverUid = intent.getStringExtra("uid").toString()
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        list = ArrayList()
        val profile = intent.getStringExtra("image")
        name = intent.getStringExtra("name").toString()
        token = intent.getStringExtra("token").toString()

        adapter = MessageAdapter(this, list, senderUid, receiverRoom)

        call(senderUid)
        setVideCall(receiverUid)


        binding.name.text = name
        binding.imageView4.load(intent.getStringExtra("image"))


        binding.imageView10.setOnClickListener {
            finish()
        }

        Firebase.database.reference.child("presence").child(receiverUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)

                        if (!status.isNullOrEmpty()) {
                            binding.status.text = status
                            binding.status.visibility = View.VISIBLE

                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        binding.send.setOnClickListener {
            validataion()

        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                image = it.data!!.data


            }

        }
        val handler = Handler()
        binding.enterMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Firebase.database.reference.child("presence").child(senderUid).setValue("Typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(run, 1000)
            }

            val run = Runnable {
                Firebase.database.reference.child("presence").child(senderUid).setValue("Online")

            }


        })


        binding.camera.setOnClickListener {
            openGallery()
        }

        Firebase.database.reference.child("Chats").child(senderRoom).child("message")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    for (snap in snapshot.children) {
                        val userData = snap.getValue(MessageModel::class.java)
                        list.add(userData!!)
                    }
                    binding.message.adapter = adapter
                    adapter.notifyDataSetChanged()
                    binding.message.scrollToPosition(list.size - 1)

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun uploadData() {
        Firebase.storage.reference.child("ChatImg/${Date().time}").putFile(image!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {
                    if (image != null) {
                        uploadMessage(it.toString())
                    }

                }
            }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.type = ("image/*")
        launcher.launch(intent)

    }


    private fun validataion() {
        if (image != null) {
            uploadData()
        } else if (binding.enterMessage.text.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid format", Toast.LENGTH_SHORT).show()
        } else {
            text()
        }


    }


    private fun text() {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val message =
            MessageModel(
                binding.enterMessage.text.toString(),
                senderUid,
                currentTime.toString(),
                0,
            )

        binding.enterMessage.setText("")


        val randromKey = Firebase.database.reference.push().key

        val last = hashMapOf<String, Any>(
            "lastMsg" to message.message,
            "lastMsgTime" to message.timeStamp
        )

        Firebase.database.reference.child("Chats").child(senderRoom).updateChildren(last)
        Firebase.database.reference.child("Chats").child(receiverRoom).updateChildren(last)

        Firebase.database.reference.child("Chats").child(senderRoom).child("message")
            .child(randromKey!!).setValue(message).addOnSuccessListener {


                Firebase.database.reference.child("Chats").child(receiverRoom)
                    .child("message").child(randromKey).setValue(message)
                    .addOnSuccessListener {
                        if (name != null) {
                            if (token != null) {
                                sendNotifications(name, message.message, token)
                            }
                        }


                    }

            }

    }


    private fun uploadMessage(url: String) {

        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val message =
            MessageModel(
                binding.enterMessage.text.toString(),
                senderUid,
                currentTime.toString(),
                0,
                url

            )

        message.imageUrl = url
        message.message = "photo"

        binding.enterMessage.setText("")
        image = null

        val randromKey = Firebase.database.reference.push().key

        val last = hashMapOf<String, Any>(
            "lastMsg" to message.message,
            "lastMsgTime" to message.timeStamp
        )

        Firebase.database.reference.child("Chats").child(senderRoom).updateChildren(last)
        Firebase.database.reference.child("Chats").child(receiverRoom).updateChildren(last)

        Firebase.database.reference.child("Chats").child(senderRoom).child("message")
            .child(randromKey!!).setValue(message).addOnSuccessListener {


                Firebase.database.reference.child("Chats").child(receiverRoom)
                    .child("message").child(randromKey).setValue(message)
                    .addOnSuccessListener {

                        if (name != null) {
                            if (token != null) {
                                sendNotifications(name, message.message, token)
                            }
                        }


                    }

            }

    }

    private fun sendNotifications(name: String, message: String, token: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://fcm.googleapis.com/fcm/send"

        val data = JSONObject()
        val notifications = JSONObject()
        data.put("title", name)
        data.put("body", message)
        notifications.put("notification", data)
        notifications.put("to", token)

        val request: JsonObjectRequest =
            object : JsonObjectRequest(url, notifications, Response.Listener {
            }, Response.ErrorListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()


            }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "key=AAAAEFmlEfQ:APA91bE0CEniFgvcSVTUb_0ossQUfOw_cpZepk1ouQl8nG0nF9PaiTiT8w_0vI5Y8v_7ogIl-Eo4p2VuhpvuceigirYB413s7tw3ySCERzykwz7zBNX0oD2YlbUzHs5ckKq6oXwZ6JRE"
                    headers["Content-Type"] = "application/json"
                    return headers
                }


            }
        queue.add(request)
    }

    override fun onResume() {
        super.onResume()
        Firebase.database.reference.child("presence").child(Firebase.auth.currentUser!!.uid)
            .setValue("Online")


    }


    override fun onPause() {
        super.onPause()

        Firebase.database.reference.child("presence").child(Firebase.auth.currentUser!!.uid)
            .setValue("offline")


    }

    fun call(source: String) {
        val application: Application = application
        val appID: Long = 1682540261
        val appSign =
            "7fc8cc363349398b13918344c45f5fd9ebcf00fc1cb21d5d9f30f887c39322ab"  // yourAppSign
        val userID = source
        val userName = userID


        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()

        val notify = ZegoNotificationConfig()
        notify.sound = "zego_uikit_sound_call"
        notify.channelID = "CallInvitation"
        notify.channelName = "CallInvitation"

        ZegoUIKitPrebuiltCallInvitationService.init(
            getApplication(),
            appID,
            appSign,
            userID,
            userName,
            callInvitationConfig
        )
    }

    fun setVideCall(target: String) {
        val vc: ZegoSendCallInvitationButton = binding.video
        vc.setIsVideoCall(true)
        vc.resourceID = "zego_uikit_call"
        vc.setInvitees(Collections.singletonList(ZegoUIKitUser(target)))


    }


}

