package com.abhinandankahol.whatsappclone

data class UserModel(
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    var profileUrl: String = "",
    var deviceToken: String = ""
)


