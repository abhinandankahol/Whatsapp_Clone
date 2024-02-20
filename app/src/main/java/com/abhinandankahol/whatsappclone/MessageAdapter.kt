package com.abhinandankahol.whatsappclone

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.abhinandankahol.whatsappclone.databinding.DialogLayoutBinding
import com.abhinandankahol.whatsappclone.databinding.ItemReceiverLayoutBinding
import com.abhinandankahol.whatsappclone.databinding.ItemSenderLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class MessageAdapter(
    val context: Context,
    private val list: ArrayList<MessageModel>,
    val senderRoom: String, val receiverRoom: String

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var ITEM_SENT = 1
    var ITEM_REC = 2

    private lateinit var dialog: AlertDialog
    private lateinit var dialogLay: DialogLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT)
            SendVH(
                binding = ItemSenderLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else ReceiverVH(
            binding = ItemReceiverLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (Firebase.auth.currentUser!!.uid == list[position].senderId) {
            ITEM_SENT
        } else ITEM_REC

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        dialog = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
        dialogLay = DialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialog.setView(dialogLay.root)
        dialog.setCanceledOnTouchOutside(true)


        val mess = list[position]
        if (holder.itemViewType == ITEM_SENT) {
            val vh = holder as SendVH

            vh.itemView.setOnLongClickListener {
                dialog.show()
                dialogLay.delteEveryone.setOnClickListener {
                    mess.message = "This message  is deleted"
                    Firebase.database.reference.child("Chats").child(senderRoom)
                        .child("message").child(mess.message).child(mess.senderId).setValue(mess)

                    Firebase.database.reference.child("Chats").child(receiverRoom)
                        .child("message").child(mess.message).setValue(mess)
                    dialog.dismiss()
                }
                true

            }

            if (mess.message.equals("photo")) {
                vh.binding.image.visibility = View.VISIBLE
                vh.binding.textView3.visibility = View.GONE

            }

            vh.binding.textView3.text = mess.message
            vh.binding.image.load(mess.imageUrl) {
                placeholder(R.drawable.avatar)
                transformations(RoundedCornersTransformation(20f))
            }


        } else {
            val vh = holder as ReceiverVH


            if (mess.message.equals("photo")) {
                vh.binding.image.visibility = View.VISIBLE
                vh.binding.textView3.visibility = View.GONE
            }
            vh.binding.textView3.text = mess.message
            vh.binding.image.load(mess.imageUrl) {
                placeholder(R.drawable.avatar)
                transformations(RoundedCornersTransformation(20f))
            }


        }


    }

    inner class SendVH(val binding: ItemSenderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    inner class ReceiverVH(val binding: ItemReceiverLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }


}