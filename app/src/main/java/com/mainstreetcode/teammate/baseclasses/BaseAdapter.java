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

package com.mainstreetcode.teammate.baseclasses;

import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

public abstract class BaseAdapter<VH extends BaseViewHolder, T extends InteractiveAdapter.AdapterListener>
        extends InteractiveAdapter<VH, T> {

    public BaseAdapter(T adapterListener) {
        super(adapterListener);
    }

    @SuppressWarnings("WeakerAccess")
    protected BaseAdapter() { super();}

    protected abstract <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder);

    @SuppressWarnings("unchecked")
    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.updateAdapterListener(updateListener(holder));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override public void onViewRecycled(@NonNull VH holder) {
        holder.clear();
        super.onViewRecycled(holder);
    }

    @Override public void onViewDetachedFromWindow(@NonNull VH holder) {
        holder.onDetached();
        super.onViewDetachedFromWindow(holder);
    }

    @Override public boolean onFailedToRecycleView(@NonNull VH holder) {
        holder.clear();
        return super.onFailedToRecycleView(holder);
    }
}
