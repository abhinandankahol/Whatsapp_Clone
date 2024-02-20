package com.abhinandankahol.whatsappclone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhinandankahol.whatsappclone.databinding.FragmentChatBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.messaging

class ChatFragment : Fragment() {
    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }
    private lateinit var userList: ArrayList<UserModel>
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Firebase.messaging.token.addOnSuccessListener { token ->
            val map = hashMapOf<String, Any>(
                "deviceToken" to token
            )
            Firebase.database.reference.child("users").child(Firebase.auth.currentUser!!.uid)
                .updateChildren(map)
        }
        userList = ArrayList()
        getValue()


    }

    private fun getValue() {
        Firebase.database.reference.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    for (user in snapshot.children) {
                        val data = user.getValue(UserModel::class.java)
                        if (data!!.userId != Firebase.auth.currentUser!!.uid) {
                            userList.add(data)

                        }

                    }
                    adapter = ChatAdapter(userList, requireContext())
                    binding.chatRecView.layoutManager = LinearLayoutManager(requireContext())
                    binding.chatRecView.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }



    override fun onResume() {
        super.onResume()
        Firebase.database.reference.child("presence").child(Firebase.auth.currentUser!!.uid)
            .setValue("Online")

    }

    override fun onPause() {
        super.onPause()
        Firebase.database.reference.child("presence").child(Firebase.auth.currentUser!!.uid)
            .setValue("")
    }


}