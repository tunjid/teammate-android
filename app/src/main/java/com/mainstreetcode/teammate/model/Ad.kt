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

package com.mainstreetcode.teammate.model


import com.mainstreetcode.teammate.util.ObjectId
import com.tunjid.androidx.recyclerview.diff.Differentiable

abstract class Ad<T> : Differentiable {

    override val diffId: String = ObjectId().toHexString()

    private val nativeAd: T? = null

    //    Ad(T nativeAd) {
    //        this.nativeAd = nativeAd;
    //    }
    //
    abstract val type: Int

    //
    //    public T getNativeAd() {
    //        return nativeAd;
    //    }
    //
    //        NativeAd.Image image = getImage();
    //        if (image == null) return null;
    //
    //        Drawable drawable = image.getDrawable();
    //        if (drawable == null) return null;
    //
    //        return "H," + drawable.getIntrinsicWidth() + ":" + drawable.getIntrinsicHeight();
    val imageAspectRatio: String?
        get() = "H,1:1"

    //
    //    @Nullable
    //    public NativeAd.Image getImage() {
    //        List<NativeAd.Image> images = null;
    //        if (nativeAd instanceof NativeContentAd) images = ((NativeContentAd) nativeAd).getImages();
    //        else if (nativeAd instanceof NativeAppInstallAd)
    //            images = ((NativeAppInstallAd) nativeAd).getImages();
    //
    //        if (images == null || images.isEmpty()) return null;
    //        return images.get(0);
    //    }
}
