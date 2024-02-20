package com.abhinandankahol.whatsappclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.abhinandankahol.whatsappclone.databinding.FragmentStatusBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class StatusFragment : Fragment() {
    private val binding by lazy { FragmentStatusBinding.inflate(layoutInflater) }
    private lateinit var topAdapter: TopStatusAdapter
    private lateinit var userAdapter: ChatAdapter
    private lateinit var statusList: ArrayList<UserStatusModel>
    private lateinit var userList: ArrayList<UserModel>
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private lateinit var user: UserModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = Firebase.auth
        statusList = ArrayList()
        topAdapter = TopStatusAdapter(requireContext(), statusList)
        userList = ArrayList()
        userAdapter = ChatAdapter(userList, requireContext())

        binding.status.adapter = topAdapter


        Firebase.database.reference.child("Stories")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        for (snap in snapshot.children) {
                            val status = UserStatusModel()
                            status.name = snap.child("name").getValue(String::class.java).toString()
                            status.image =
                                snap.child("image").getValue(String::class.java).toString()
                            status.lastUpdated =
                                snap.child("lastUpdated").getValue(String::class.java).toString()
                            statusList.add(status)

                            val list2 = ArrayList<StatusModel>()
                            for (snap2 in snapshot.child("Statuses").children) {
                                val sample = snap2.getValue(StatusModel::class.java)
                                if (sample != null) {

                                    list2.add(sample)
                                    statusList.clear()
                                }
                            }
                            status.statuses = list2

                        }
                        topAdapter.notifyDataSetChanged()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })





























        auth.currentUser?.let { currentUser ->
            Firebase.database.reference.child("users").child(currentUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        user = snapshot.getValue(UserModel::class.java) ?: UserModel()
                        userList.add(user)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error if needed
                    }
                })
        }

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    imageUri = data?.data

                    imageUri?.let { uri ->
                        val storageReference =
                            Firebase.storage.reference.child("PostedStatus/${UUID.randomUUID()}")
                        storageReference.putFile(uri).addOnSuccessListener { uploadTask ->
                            uploadTask.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                                val status = UserStatusModel(
                                    name = user.name,
                                    image = user.profileUrl,
                                    lastUpdated = System.currentTimeMillis().toString()
                                )

                                val ma2 = hashMapOf(
                                    "name" to status.name,
                                    "image" to status.image,
                                    "lastUpdated" to status.lastUpdated
                                )

                                val sendStatus =
                                    StatusModel(downloadUri.toString(), status.lastUpdated)

                                auth.currentUser?.let { currentUser ->
                                    Firebase.database.reference.child("Stories")
                                        .child(currentUser.uid)
                                        .updateChildren(ma2 as Map<String, Any>)

                                    Firebase.database.reference.child("Stories")
                                        .child(currentUser.uid).child("Statues").push()
                                        .setValue(sendStatus)
                                }
                            }
                        }
                    }
                }
            }

        binding.include.imageLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }
    }
}
