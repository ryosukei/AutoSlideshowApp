package jp.techacademy.ryosuke.aono.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer:Timer? = null

    private var mHandler = Handler()
    private var cursor: Cursor? = null
    private var isSlideShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getFirstContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getFirstContentsInfo()
        }

        // 戻るボタンを押したら前の画像を表示する
        back_btn.setOnClickListener{
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    backContent()
            }else{
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        }
        go_btn.setOnClickListener{
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                goContent()
            }else{
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        }
        start_and_stop_btn.setOnClickListener{
            if(!isSlideShow){
                go_btn.isEnabled = false
                back_btn.isEnabled = false
                start_and_stop_btn.text = "停止"
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask(){
                    override fun run() {
                        mHandler.post {
                            goContent()
                        }
                    }
                }, 2000, 2000)
            }else{
                mTimer!!.cancel()
                go_btn.isEnabled = true
                back_btn.isEnabled = true
                start_and_stop_btn.text = "再生"
            }
            isSlideShow = !isSlideShow
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFirstContentsInfo()
                }
        }
    }

    private fun getFirstContentsInfo(){
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        if (cursor!!.moveToFirst()) {
            contentsInfo()
        }
    }

    private fun backContent(){
        if(cursor!!.isFirst) {
            cursor!!.moveToLast()
        }else {
            cursor!!.moveToPrevious()
        }
        contentsInfo()
    }


    private fun goContent(){
        if(cursor!!.isLast){
            cursor!!.moveToFirst()
        }else{
            cursor!!.moveToNext()
        }
        contentsInfo()
    }

    private fun contentsInfo(){
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            image.setImageURI(imageUri)
    }


}