/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.headless

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.core.content.ContextCompat
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.Logger.log
import com.theartofdev.edmodo.cropper.CropImage
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.MaybeOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import java.util.*

/**
 * Inner fragment hosting code for interacting with image and cropping APIs
 */

class ImageWorkerFragment : MainActivityFragment() {

    private var isPicking: Boolean = false

    private val isCropListener: Boolean
        get() = parentFragment is CropListener

    private val isMediaListener: Boolean
        get() = parentFragment is MediaListener

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val gotPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (!gotPermission) return

        when (requestCode) {
            CROP_CHOOSER -> startImagePicker()
            MULTIPLE_MEDIA_CHOOSER -> startMultipleMediaPicker()
            MEDIA_DOWNLOAD_CHOOSER -> {
                val target = parentFragment ?: return
                if (target is DownloadRequester) {
                    val requester = target as DownloadRequester
                    val team = requester.requestedTeam()
                    val started = mediaViewModel.downloadMedia(team)

                    requester.startedDownLoad(started)
                    if (started) showSnackbar(getString(R.string.media_download_started))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data ?: return
        val isCropListener = isCropListener
        val isMediaListener = isMediaListener
        val failed = resultCode != Activity.RESULT_OK

        val target = parentFragment ?: return

        val activity = activity ?: return

        if (failed && (requestCode == CROP_CHOOSER || requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE))
            isPicking = false

        if (failed || !isCropListener && !isMediaListener) return

        when {
            requestCode == CROP_CHOOSER && isCropListener -> CropImage.activity(data.data)
                    .setFixAspectRatio(true)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(80, 80)
                    .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                    .start(activity, this)

            requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && isCropListener -> {
                val result = CropImage.getActivityResult(data)
                val resultUri = result.uri
                (target as CropListener).onImageCropped(resultUri)
                isPicking = false
            }

            requestCode == MULTIPLE_MEDIA_CHOOSER && isMediaListener -> {
                val listener = target as MediaListener
                val filesMaybe = Maybe.create(MediaQuery(data)).subscribeOn(io()).observeOn(mainThread())
                disposables.add(filesMaybe.subscribe(listener::onFilesSelected, ErrorHandler.EMPTY::invoke))
            }
        }
    }

    private fun startImagePicker() {
        isPicking = true
        val intent = Intent()
        intent.type = IMAGE_SELECTION
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_picture)), CROP_CHOOSER)
    }

    private fun startMultipleMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        startActivityForResult(intent, MULTIPLE_MEDIA_CHOOSER)
    }

    interface CropListener {
        fun onImageCropped(uri: Uri)
    }

    interface MediaListener {
        fun onFilesSelected(uris: List<Uri>)
    }

    interface DownloadRequester {
        fun requestedTeam(): Team

        fun startedDownLoad(started: Boolean)
    }

    interface ImagePickerListener : InteractiveAdapter.AdapterListener {
        fun onImageClick()
    }

    internal class MediaQuery(private val data: Intent) : MaybeOnSubscribe<List<Uri>> {

        override fun subscribe(emitter: MaybeEmitter<List<Uri>>) = emitter.onSuccess(onData())

        private fun onData(): List<Uri> {
            val uri = data.data
            val uris = ArrayList<Uri>()
            val clip = data.clipData
            val count = clip?.itemCount ?: 0


            if (count != 0) for (i in 0 until count) uris.add(clip!!.getItemAt(i).uri)
            else if (uri != null) uris.add(uri)

            if (data.hasExtra("uris")) uris.addAll(data.getParcelableArrayListExtra("uris"))

            // For Xiaomi Phones
            if (uris.isEmpty() && data.hasExtra("pick-result-data"))
                uris.addAll(data.getParcelableArrayListExtra("pick-result-data"))

            return uris
        }
    }

    companion object {

        private const val CROP_CHOOSER = 1
        private const val MULTIPLE_MEDIA_CHOOSER = 2
        private const val MEDIA_DOWNLOAD_CHOOSER = 3

        private const val TAG = "ImageWorkerFragment"
        private const val IMAGE_SELECTION = "image/*"
        private val MIME_TYPES = arrayOf("image/*", "video/*")
        private val STORAGE_PERMISSIONS = arrayOf(WRITE_EXTERNAL_STORAGE)

        fun newInstance(): ImageWorkerFragment = ImageWorkerFragment()

        fun attach(host: BaseFragment) {
            if (getInstance(host) != null) return

            val instance = newInstance()

            host.childFragmentManager.beginTransaction()
                    .add(instance, makeTag(host))
                    .commit()
        }

        fun requestCrop(host: BaseFragment) {
            requireInstanceWithActivity(host) { instance, activity ->
                val noPermit = noStoragePermission(activity)

                if (noPermit) instance.requestPermissions(STORAGE_PERMISSIONS, CROP_CHOOSER)
                else instance.startImagePicker()
            }
        }

        fun isPicking(host: BaseFragment): Boolean {
            val instance = getInstance(host)

            return instance != null && instance.isPicking
        }

        fun requestMultipleMedia(host: BaseFragment) {
            requireInstanceWithActivity(host) { instance, activity ->
                val noPermit = noStoragePermission(activity)

                if (noPermit)
                    instance.requestPermissions(STORAGE_PERMISSIONS, MULTIPLE_MEDIA_CHOOSER)
                else
                    instance.startMultipleMediaPicker()
            }
        }

        fun requestDownload(host: BaseFragment, team: Team): Boolean {
            return requireInstanceWithActivity(host, { instance, activity ->
                val noPermit = noStoragePermission(activity)
                var started = false

                if (noPermit)
                    instance.requestPermissions(STORAGE_PERMISSIONS, MEDIA_DOWNLOAD_CHOOSER)
                else
                    started = instance.mediaViewModel.downloadMedia(team)

                if (started) instance.showSnackbar(activity.getString(R.string.media_download_started))

                started
            }, false)
        }

        private fun noStoragePermission(activity: Activity): Boolean {
            return SDK_INT >= M && ContextCompat.checkSelfPermission(activity,
                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        }

        private fun makeTag(host: BaseFragment): String = TAG + "-" + host.stableTag

        private fun getInstance(host: BaseFragment): ImageWorkerFragment? =
                host.childFragmentManager.findFragmentByTag(makeTag(host)) as ImageWorkerFragment?

        private fun requireInstanceWithActivity(host: BaseFragment, biConsumer: (ImageWorkerFragment, Activity) -> Unit) {
            requireInstanceWithActivity(host, { instance, activity ->
                try {
                    biConsumer.invoke(instance, activity)
                } catch (e: Exception) {
                    log(TAG, "Could not get Instance", e)
                }
            }, Unit)
        }

        private fun <T> requireInstanceWithActivity(host: BaseFragment, biFunction: (ImageWorkerFragment, Activity) -> T, defaultValue: T): T {
            val instance = getInstance(host)

            if (instance == null) {
                attach(host)
                return defaultValue
            }

            val activity = host.activity ?: return defaultValue

            try {
                return biFunction.invoke(instance, activity)
            } catch (e: Exception) {
                log(TAG, "Could not get Instance", e)
            }

            return defaultValue
        }
    }
}
