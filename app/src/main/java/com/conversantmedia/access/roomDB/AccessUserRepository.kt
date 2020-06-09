package com.conversantmedia.access.roomDB

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class AccessUserRepository(private val userDao: UserDao) {
    val user: LiveData<UserEntity> = userDao.getUser()

    @WorkerThread
    fun insert(userEntity: UserEntity) {
        userDao.insertUser(userEntity)
    }

    @WorkerThread
    fun logOut() {
        userDao.logOut()
    }

    @WorkerThread
    fun delete() {
        userDao.delete()
    }
}