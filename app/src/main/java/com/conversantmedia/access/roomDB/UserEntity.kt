package com.conversantmedia.access.roomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_user_table")
data class UserEntity(
    @PrimaryKey var userId: Int?,
    @ColumnInfo(name = "email_address") var emailAddress: String?,
    @ColumnInfo(name = "remember_me") var rememberMe: Boolean? = true,
    @ColumnInfo(name = "token") var token: String?
) {
    constructor(): this(null, null, null, null)
}