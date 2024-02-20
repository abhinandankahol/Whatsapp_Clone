package com.abhinandankahol.whatsappclone

data class UserStatusModel(
    var name: String = "",
    var image: String = "",
    var lastUpdated: String = "",
    var statuses: ArrayList<StatusModel> = arrayListOf()
)