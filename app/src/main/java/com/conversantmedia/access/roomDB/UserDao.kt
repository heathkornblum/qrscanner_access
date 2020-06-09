package com.conversantmedia.access.roomDB

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM access_user_table where userId = 1")
    fun getUser(): LiveData<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("DELETE from access_user_table")
    fun delete()

    @Query("UPDATE access_user_table SET token = null WHERE userId = 1" )
    fun logOut()
}