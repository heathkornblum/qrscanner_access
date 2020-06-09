package com.conversantmedia.access

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.conversantmedia.access.roomDB.AccessUserViewModel
import kotlinx.android.synthetic.main.fragment_qrcode_button.*
import java.io.IOException

private const val REQUEST_CAMERA_PERMISSION = 201

class QRCodeButton : Fragment() {

    private val TAG = javaClass.simpleName
    private lateinit var viewModel: AccessUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(AccessUserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qrcode_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        showQRScanningStateAndPermission()

        qrSymbol.setOnClickListener {
            navController.navigate(R.id.QRCamera)
        }

        warningSymbol.setOnClickListener {
            navController.navigate(R.id.QRCamera)
        }

        scanQRString.setOnClickListener {
            navController.navigate(R.id.QRCamera)
        }

        errorScanMsg.setOnClickListener {
            navController.navigate(R.id.QRCamera)
        }

        buttonLogOut.setOnClickListener {
            viewModel.logOut()
            viewModel.authenticationState.value = AccessUserViewModel.AuthenticationState.UNAUTHENTICATED
        }

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->

            when (authenticationState) {
                AccessUserViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    navController.navigate(R.id.authentication)
                }
            }
        })

    }


    /**
     * Draw QR Code Scanner Button
     */
    private fun showQRScanningStateAndPermission() {
        if (PersistenceWorkaround.scanFailure) {
            circleBurst.visibility = View.GONE
            qrSymbol.visibility = View.GONE
            scanQRString.visibility = View.GONE
            scanErrorMessage.visibility = View.VISIBLE
            circleBurstRed.visibility = View.VISIBLE
            warningSymbol.visibility = View.VISIBLE
            errorScanMsg.visibility = View.VISIBLE
            scanErrorMessage.text = getString(R.string.scan_error, PersistenceWorkaround.errorBody)
            PersistenceWorkaround.scanFailure = false
        }
        try {
            if (ActivityCompat.checkSelfPermission(context!!,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }

}
