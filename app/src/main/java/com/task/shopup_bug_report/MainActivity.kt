package com.task.shopup_bug_report

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var screenDensity:Int = 0
    private var projectManager:MediaProjectionManager?=null;
    private var mediaProjection:MediaProjection?=null
    private var virtualDisplay:VirtualDisplay?=null
    private var mediaProjectionCallback:MediaProjectionCallback?=null
    private var mediaRecorder: MediaRecorder?=null

    internal var videoUri:String=""

    companion object{
        private val REQUEST_CODE = 1000
        private val REQUEST_PERMISSION = 1001
        private var DISPLAY_WIDTH = 700
        private var DISPLAY_HEIGHT = 1280
        private val ORIENTATION = SparseIntArray()

        init {
            ORIENTATION.append(Surface.ROTATION_0, 90)
            ORIENTATION.append(Surface.ROTATION_90, 0)
            ORIENTATION.append(Surface.ROTATION_180, 270)
            ORIENTATION.append(Surface.ROTATION_270, 180)

        }
    }


//    var toggleButton:ToggleButton = findViewById<ToggleButton>(R.id.toggleButton)
    inner class MediaProjectionCallback: MediaProjection.Callback(){


        override fun onStop() {
            if(toggleButton.isChecked){
                toggleButton.isChecked=false
                mediaRecorder?.stop()
                mediaRecorder?.reset()
            }
            mediaProjection = null
            stopScreenRecord()

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenDensity = metrics.densityDpi

        mediaRecorder = MediaRecorder()

        projectManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        DISPLAY_HEIGHT = metrics.heightPixels
        DISPLAY_WIDTH = metrics.widthPixels

        toggleButton.setOnClickListener{v ->
//            if(ContextCompat.checkSelfPermission(this@MainActivity,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this@MainActivity,
//                    Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
//                        if(ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                            || ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.RECORD_AUDIO)) {
//                            toggleButton.isChecked = false
//                            Snackbar.make(rootLayout, "Permissions", Snackbar.LENGTH_INDEFINITE)
//                                .setAction("ENABLE", {
//                                    ActivityCompat.requestPermissions(
//                                        this@MainActivity,
//                                        arrayOf(
//                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                            Manifest.permission.RECORD_AUDIO
//                                        ), REQUEST_PERMISSION
//                                    )
//                                }).show()
//                            }
//                        else{
//                            ActivityCompat.requestPermissions(
//                                this@MainActivity,
//                                arrayOf(
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                    Manifest.permission.RECORD_AUDIO
//                                ), REQUEST_PERMISSION
//                            )
//                        }
//                    }
//            else{
//                startRecording(v)
//            }
            startRecording(v) // remove later
        }


//        Log.i("TAG", "SERIAL: " + Build.SERIAL)
//        Log.i("TAG","MODEL: " + Build.MODEL)
//        Log.i("TAG","ID: " + Build.ID)
//        Log.i("TAG","Manufacture: " + Build.MANUFACTURER)
//        Log.i("TAG","brand: " + Build.BRAND)
//        Log.i("TAG","type: " + Build.TYPE)
//        Log.i("TAG","user: " + Build.USER)
//        Log.i("TAG","BASE: " + Build.VERSION_CODES.BASE)
//        Log.i("TAG", "TIME " + Build.TIME)
//        Log.i("TAG","SDK  " + Build.VERSION.SDK)
//        Log.i("TAG","BOARD: " + Build.BOARD)
//        Log.i("TAG","BRAND " + Build.BRAND)
//        Log.i("TAG","HOST " + Build.HOST)
//        Log.i("TAG","FINGERPRINT: "+ Build.FINGERPRINT)
//        Log.i("TAG","Version Code: " + Build.VERSION.RELEASE)
//        Log.i("TAG","INCREMENTAL " + Build.VERSION.INCREMENTAL)INCREMENTAL

    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when(requestCode){
//            REQUEST_PERMISSION->{
//                if(grantResults.size > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)
//                    startRecording(toggleButton)
//                else{
//                    toggleButton.isChecked = false
//                    Snackbar.make(rootLayout, "Permissions", Snackbar.LENGTH_INDEFINITE)
//                        .setAction("ENABLE", {
//                            val intent = Intent()
//                            intent.action = Settings.ACTION_APPLICATION_SETTINGS
//                            intent.addCategory(Intent.CATEGORY_DEFAULT)
//                            intent.data = Uri.parse("package:$packageName")
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
//                            startActivity(intent)
//
//                        }).show()
//
//                }
//                return
//            }
//        }
//    }
    private fun startRecording(v: View?){
        if((v as ToggleButton).isChecked){
            initRecorder()
            shareScreen()
        }
        else{
            mediaRecorder!!.stop()
            mediaRecorder!!.reset()
            stopScreenRecord()

            //play video in video view
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(Uri.parse(videoUri))
            videoView.start()
        }
    }

    private fun shareScreen(){
        if(mediaProjection == null){
            startActivityForResult(projectManager!!.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }
        virtualDisplay = createVirtualDisplay()
        mediaRecorder?.start()
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return  mediaProjection!!.createVirtualDisplay("MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mediaRecorder!!.surface, null, null)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode != REQUEST_CODE) return
        if(resultCode != Activity.RESULT_OK){
            Toast.makeText(this, "Screen cast permission denied", Toast.LENGTH_LONG).show()
            return
        }

        mediaProjectionCallback = MediaProjectionCallback()
        mediaProjection = projectManager!!.getMediaProjection(resultCode, data!!)
        mediaProjection!!.registerCallback(mediaProjectionCallback, null)
        virtualDisplay = createVirtualDisplay()
        mediaRecorder!!.start()
    }


    private fun initRecorder(){
        try{
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder!!.setAudioSamplingRate(16000)
//            val surface: Surface = MediaCodec.createPersistentInputSurface()
//            mediaRecorder!!.setInputSurface(surface)

//            var cacheDir = getCacheDir()
//            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            videoUri = getCacheDir()
                .toString() + StringBuilder("/")
                .append("shopup_")
                .append(SimpleDateFormat("dd-mm-yyyy-hh_mm_ss").format(Date()))
                .append(".mp4")
                .toString()

            mediaRecorder!!.setOutputFile(videoUri)
            mediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setVideoEncodingBitRate(512*1000)
            mediaRecorder!!.setVideoFrameRate(30)

            val rotation = windowManager.defaultDisplay.rotation
            val orientation = ORIENTATION.get(rotation + 90)
            mediaRecorder!!.setOrientationHint(orientation)
            mediaRecorder!!.prepare()
        }catch(e:IOException){
            e.printStackTrace()
        }


    }

    private fun stopScreenRecord(){
        if(virtualDisplay == null) return

        virtualDisplay!!.release()
        destroyMediaProjection()
    }

    private fun destroyMediaProjection(){
        if(mediaProjection != null){
            mediaProjection!!.unregisterCallback(mediaProjectionCallback)
            mediaProjection!!.stop()
            mediaProjection = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyMediaProjection()
    }
}