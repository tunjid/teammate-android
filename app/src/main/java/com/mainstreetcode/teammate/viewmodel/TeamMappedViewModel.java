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

package com.mainstreetcode.teammate.viewmodel;


import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public abstract class TeamMappedViewModel<V extends Differentiable & TeamHost> extends MappedViewModel<Team, V> {

    final Map<Team, List<Differentiable>> modelListMap = new HashMap<>();

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        //noinspection unchecked
        Alert.matches(alert, Alert.of(Alert.Deletion.class, Team.class, modelListMap::remove));
    }

    public List<Differentiable> getModelList(Team team) {
        return ModelUtils.get(team, modelListMap, ArrayList::new);
    }

    @Override
    void onErrorMessage(Message message, Team key, Differentiable invalid) {
        super.onErrorMessage(message, key, invalid);
        if (message.isIllegalTeamMember()) pushModelAlert(Alert.deletion(key));
    }

    Consumer<Throwable> onError(V model) {
        return throwable -> checkForInvalidObject(throwable, model, model.getTeam());
    }

    @Override
    void onInvalidKey(Team key) {
        super.onInvalidKey(key);
        pushModelAlert(Alert.deletion(key));
    }

    Flowable<Differentiable> getAllModels() {
        return Flowable.fromIterable(modelListMap.values()).flatMap(Flowable::fromIterable);
    }
}
