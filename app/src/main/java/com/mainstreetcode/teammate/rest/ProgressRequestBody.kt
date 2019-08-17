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

package com.mainstreetcode.teammate.rest


import android.net.Uri

import com.mainstreetcode.teammate.App

import java.io.IOException
import java.io.InputStream

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

class ProgressRequestBody(
        private val uri: Uri,
        private val numWriteToCallsToIgnore: Int,
        private val mediaType: MediaType
) : RequestBody() {

    private var numWriteToCalls: Int = 0
    private val processor = PublishProcessor.create<Int>()

    val progressFlowable: Flowable<Int>
        get() = processor

    override fun contentType(): MediaType? = mediaType

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        numWriteToCalls++

        // when using HttpLoggingInterceptor it calls writeTo and passes data into
        // a local buffer just for logging purposes.
        // the second call to write to is the progress we actually want to track
        val isUploading = numWriteToCalls > numWriteToCallsToIgnore

        val `in`: InputStream?

        try {
            `in` = App.instance.contentResolver.openInputStream(uri)
        } catch (exception: Exception) {
            processor.onError(exception)
            throw exception
        }

        if (`in` == null) {
            val exception = IOException("Unable to create stream from URI")
            processor.onError(exception)
            throw exception
        }

        var uploaded = 0f
        val fileLength = `in`.available().toFloat()
        val buffer = ByteArray(UPLOAD_BUFFER_SIZE)

        try {
            var overallProgress = 0f
            var read: Int
            read = `in`.read(buffer)

            while (read != -1) {
                uploaded += read.toFloat()
                sink.write(buffer, 0, read)
                read = `in`.read(buffer)

                if (!isUploading) continue
                val currentProgress = uploaded / fileLength * 100

                // prevent publishing too many updates, which slows upload,
                // by checking if the upload has progressed by at least 1 percent
                if (currentProgress - overallProgress <= 1) continue

                // publish progress
                processor.onNext(currentProgress.toInt())
                overallProgress = currentProgress
            }

            if (isUploading) processor.onComplete()
        } catch (exception: Exception) {
            processor.onError(exception)
            throw exception
        }

    }

    companion object {

        private const val UPLOAD_BUFFER_SIZE = 2048
    }
}

