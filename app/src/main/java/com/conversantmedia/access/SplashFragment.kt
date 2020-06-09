package com.conversantmedia.access


import android.os.Bundle
import android.os.TokenWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.conversantmedia.access.retrofit.RetrofitClient
import com.conversantmedia.access.retrofit.TokenHolder
import com.conversantmedia.access.retrofit.ValidationResponse
import com.conversantmedia.access.roomDB.AccessUserViewModel
import com.conversantmedia.access.roomDB.UserEntity
import kotlinx.android.synthetic.main.fragment_authentication.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SplashFragment : Fragment() {

    lateinit var viewModel: AccessUserViewModel
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AccessUserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authState ->
            when (authState) {
                AccessUserViewModel.AuthenticationState.AUTHENTICATED -> {
                    navController.navigate(R.id.QRCodeButton)
                }
                else -> {
                    navController.navigate(R.id.authentication)
                }
            }

        })

        viewModel.userEntity.observe(viewLifecycleOwner, Observer { user ->
            if (user == null) {
                navController.navigate(R.id.authentication)
            } else {
                TokenHolder.token = user.token
                checkToken(user)
            }
        }
        )
    }

    private fun checkToken(userEntity: UserEntity) {
        val token = userEntity.token
        if (!token.isNullOrEmpty())  {
            RetrofitClient.instance.validateUser().enqueue(
                object : Callback<ValidationResponse> {
                    override fun onFailure(call: Call<ValidationResponse>, t: Throwable) {
                        viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.INVALID_AUTHENTICATION
                    }

                    override fun onResponse(call: Call<ValidationResponse>, response: Response<ValidationResponse>) {
                        if (!response.body()?.userName.isNullOrEmpty()) {
                            viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.AUTHENTICATED
                        } else {
                            viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.UNAUTHENTICATED
                        }
                    }
                }
            )
        } else {
            viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.UNAUTHENTICATED
        }
    }

}
