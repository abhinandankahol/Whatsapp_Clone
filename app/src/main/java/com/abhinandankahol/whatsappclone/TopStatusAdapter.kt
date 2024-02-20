package com.abhinandankahol.whatsappclone

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abhinandankahol.whatsappclone.databinding.ItemStatusBinding

class TopStatusAdapter(val context: Context, val list: ArrayList<UserStatusModel>) :
    RecyclerView.Adapter<TopStatusAdapter.StatusVH>() {
    class StatusVH(val binding: ItemStatusBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStatusAdapter.StatusVH {
        return StatusVH(binding = ItemStatusBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TopStatusAdapter.StatusVH, position: Int) {


    }

    override fun getItemCount(): Int {
        return list.size

    }
}