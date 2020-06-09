package com.conversantmedia.access.roomDB

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AccessUserViewModel(application: Application): AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    private val repository: AccessUserRepository

    val authenticationState = MutableLiveData<AuthenticationState>()

    val userEntity: LiveData<UserEntity>

    init {
        val userDao = UserDb.getDatabase(application).userDao()
        repository = AccessUserRepository(userDao)
        userEntity = repository.user
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    fun insert(userEntity: UserEntity) = scope.launch(Dispatchers.IO) {
        repository.insert(userEntity)
    }

    fun delete() = scope.launch(Dispatchers.IO) {
        repository.delete()
    }

    fun logOut() = scope.launch(Dispatchers.IO) {
        repository.logOut()

    }

    enum class AuthenticationState {
        AUTHENTICATED,
        UNAUTHENTICATED,
        INVALID_AUTHENTICATION
    }
}