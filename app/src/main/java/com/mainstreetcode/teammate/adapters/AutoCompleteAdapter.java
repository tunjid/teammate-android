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

package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AutoCompleteViewHolder;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

public class AutoCompleteAdapter extends BaseAdapter<AutoCompleteViewHolder, AutoCompleteAdapter.AdapterListener> {

    private final List<AutocompletePrediction> predictions;

    public AutoCompleteAdapter(List<AutocompletePrediction> predictions,
                               AutoCompleteAdapter.AdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.predictions = predictions;
    }

    @NonNull
    @Override
    public AutoCompleteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new AutoCompleteViewHolder(getItemView(R.layout.viewholder_auto_complete, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AutoCompleteViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        viewHolder.bind(predictions.get(position));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        return (S) adapterListener;
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    @Override
    public long getItemId(int position) {
        return predictions.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM;
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onPredictionClicked(AutocompletePrediction prediction);
    }

}
