package com.abhinandankahol.whatsappclone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.abhinandankahol.whatsappclone.databinding.ItemCahtUserLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ChatAdapter(private var list: ArrayList<UserModel>, val context: Context) :
    RecyclerView.Adapter<ChatAdapter.ChatVH>() {
    class ChatVH(val binding: ItemCahtUserLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        return ChatVH(
            binding = ItemCahtUserLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        val model = list[position]

        val senderId = Firebase.auth.currentUser!!.uid
        val senderRoom = senderId + model.userId

        Firebase.database.reference.child("Chats").child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lstMsg = snapshot.child("lastMsg").getValue(String::class.java)
                        val lastTime = snapshot.child("lastMsgTime").getValue(String::class.java)

                        holder.binding.textView4.text = lastTime.toString()

                        holder.binding.taptochat.text = lstMsg.toString()
                    } else {
                        holder.binding.taptochat.text = "Tap to chat"
                        holder.binding.textView4.text = ""

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })



        holder.apply {
            binding.imageView.load(model.profileUrl) {
                transformations(CircleCropTransformation())
            }
            binding.name.text = model.name
            itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("uid", model.userId)
                intent.putExtra("image", model.profileUrl)
                intent.putExtra("name", model.name)
                intent.putExtra("token",model.deviceToken)
                context.startActivity(intent)
            }

        }
    }
}