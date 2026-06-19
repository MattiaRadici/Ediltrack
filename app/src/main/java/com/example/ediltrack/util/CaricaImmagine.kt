package com.example.ediltrack.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object CaricaImmagine {

    private const val REQUEST_CODE_PERMISSION = 1001
    private const val REQUEST_CODE_PICK_IMAGE = 1002

    private var callback: ((Uri?) -> Unit)? = null

    fun caricaImmagine(fragment: Fragment, onImagePicked: (Uri?) -> Unit) {
        callback = onImagePicked

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Controllo permesso
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(fragment.requireActivity(), arrayOf(permission), REQUEST_CODE_PERMISSION)
        } else {
            openGallery(fragment)
        }
    }

    private fun openGallery(fragment: Fragment) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        } else {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    fun handlePermissionResult(
        fragment: Fragment,
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery(fragment)
        } else {
            callback?.invoke(null) // permesso negato
        }
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            callback?.invoke(data?.data)
        }
    }
}