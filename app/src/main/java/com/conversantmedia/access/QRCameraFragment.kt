package com.conversantmedia.access

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.conversantmedia.access.roomDB.AccessUserViewModel
import com.conversantmedia.access.roomDB.UserEntity
import com.conversantmedia.cnvrapiadapter.cnvradview.CnvrInterstitial
import com.conversantmedia.cnvrapiadapter.cnvradview.InterstitialActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_authentication.*
import kotlinx.android.synthetic.main.fragment_qrcamera.*
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import java.util.jar.Manifest

private const val REQUEST_CAMERA_PERMISSION = 201

class QRCamera : Fragment(), CnvrInterstitial.InterstitialListener {

    private lateinit var viewModel: AccessUserViewModel
    lateinit var barcodeDetector: BarcodeDetector
    var cameraSource: CameraSource? = null
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val handler = Handler(Looper.getMainLooper())

    private var  interstitial : CnvrInterstitial? = null

    private val timeout = 2000L

    private val TAG = javaClass.simpleName

    lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(AccessUserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qrcamera, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBarCamera?.visibility = View.GONE
        navController = findNavController()

        context?.let {
            interstitial = CnvrInterstitial(it)
            interstitial?.setListener(this)
        }
        cancelButton.setOnClickListener {
            navController.navigate(R.id.QRCodeButton)
        }

        prepareQRScanner()



    }

    private fun prepareQRScanner() {
        barcodeDetector = BarcodeDetector
            .Builder(context)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
        cameraSource = CameraSource
            .Builder(context, barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                try {
                    if (ActivityCompat.checkSelfPermission(context!!,
                            android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource?.start(surfaceView.holder)
                    } else {
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                    }
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource?.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Snackbar.make(cameraLayout, resources.getString(R.string.scanner_stopped), Snackbar.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                val barcodes: SparseArray<Barcode>? = detections?.detectedItems
                barcodes?.let {
                    if (barcodes.size() > 0) {
                        var urlSuccess = true
                        try {
                            val url = URL(Uri.parse(barcodes.valueAt(0).displayValue).toString())
                        } catch (exception: Exception) {
                            urlSuccess = false
                            PersistenceWorkaround.scanFailure = true
                            PersistenceWorkaround.errorBody = exception.message
                            navController.navigate(R.id.QRCodeButton)
                        }

                        if (urlSuccess) {
                            requestInterstitial(interstitial, Uri.parse(barcodes.valueAt(0).displayValue))

                        }
                    }
                }
            }
        })
    }

    private fun requestInterstitial(interstitial: CnvrInterstitial?, requestUri: Uri?) {

        scope.launch {
            withTimeout(timeout) {
                var content = ""
                try {
                    content = URL(requestUri.toString()).readText()
                    PersistenceWorkaround.scanFailure = false
                } catch (exception : Exception) {
                    PersistenceWorkaround.scanFailure = true
                    PersistenceWorkaround.errorBody = exception.message
                    navController.navigate(R.id.QRCodeButton)
                }

                handler.post {
                    if (progressBarCamera?.visibility == View.GONE) {
                        progressBarCamera?.visibility = View.VISIBLE
                        progressBarCamera?.invalidate()
                    }
                    interstitial?.setAdvertisingId("ConversantAdId")
                    interstitial?.loadInterstitial(content, requestUri.toString())
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        cameraSource?.release()
        navController.popBackStack(R.id.QRCodeButton, false)


    }

    override fun onInterstitialFailed(interstitial: CnvrInterstitial, errorMessage: String) {
        super.onInterstitialFailed(interstitial, errorMessage)
        PersistenceWorkaround.errorBody = errorMessage
    }
}
