package com.abhinandankahol.whatsappclone

data class MessageModel(
    var message: String = "",
    val senderId: String = "",
    val timeStamp: String = "",
    val reaction: Int = 0,
    var imageUrl: String = ""
)