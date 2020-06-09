package com.conversantmedia.access

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.conversantmedia.access.retrofit.DefaultResponse
import com.conversantmedia.access.retrofit.RetrofitClient
import com.conversantmedia.access.roomDB.AccessUserViewModel
import com.conversantmedia.access.roomDB.UserEntity
import kotlinx.android.synthetic.main.fragment_authentication.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Authentication : Fragment() {

    private lateinit var viewModel: AccessUserViewModel
    private val TAG = javaClass.simpleName
    private lateinit var theProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity()).get(AccessUserViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_authentication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        theProgressBar = progressBar
        enableUI()

        val navController = findNavController()

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AccessUserViewModel.AuthenticationState.AUTHENTICATED -> {
                    navController.popBackStack()
                    navController.navigate(R.id.QRCodeButton)
                }

                AccessUserViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    loginErrorMessage.visibility = View.VISIBLE
                    usernameField.background = resources.getDrawable(R.drawable.error_text_field, null)
                    passwordField.background = resources.getDrawable(R.drawable.error_text_field, null)
                    theProgressBar.visibility = View.GONE
                }
            }
        })

        viewModel.userEntity.observe(viewLifecycleOwner, Observer {user ->
            user?.emailAddress?.let {
                usernameField.setText(it)
            }

        })

        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            disableUI()
            RetrofitClient.instance.authenticateUser(
                username, password
            ).enqueue(object : Callback<DefaultResponse> {
                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.INVALID_AUTHENTICATION
                }
                override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                    if (response.code() == 200) {
                        var user = UserEntity()
                        user.userId = 1
                        user.token = response.body()?.token
                        if (rememberMe.isChecked) {
                            user.emailAddress = usernameField.text.toString()
                            user.rememberMe = true
                        }
                        viewModel.insert(user)
                        viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.AUTHENTICATED
                    } else {
                        viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.INVALID_AUTHENTICATION
                        enableUI()
                    }

                }
            })
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(resources.getString(R.string.forgot_password_url))
            startActivity(intent)
        }


    }

    private fun disableUI() {
        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false
        usernameField.isEnabled = false
        passwordField.isEnabled = false
    }

    private fun enableUI() {
        progressBar.visibility = View.GONE
        loginButton.isEnabled = true
        usernameField.isEnabled = true
        passwordField.isEnabled = true
    }
}
