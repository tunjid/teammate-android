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

package com.mainstreetcode.teammate.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.util.Logger;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;

@SuppressLint("ParcelCreator")
public class TeamMember<S extends Model<S> & TeamHost & UserHost> implements
        UserHost,
        TeamHost,
        Model<TeamMember<S>> {

    private final S wrappedModel;

    private TeamMember(S wrappedModel) {this.wrappedModel = wrappedModel;}

    public S getWrappedModel() { return wrappedModel; }

    public User getUser() { return wrappedModel.getUser(); }

    public Team getTeam() { return wrappedModel.getTeam(); }

    public Date getCreated() {
        return wrappedModel instanceof Role
                ? ((Role) wrappedModel).getCreated()
                : wrappedModel instanceof JoinRequest
                ? ((JoinRequest) wrappedModel).getCreated()
                : new Date();
    }

    public static <S extends Model<S> & TeamHost & UserHost> TeamMember<S> fromModel(final S wrappedModel) {
        return new TeamMember<>(wrappedModel);
    }

    @SuppressWarnings("unchecked")
    public static <S extends Model<S> & TeamHost & UserHost> TeamMember<S> unsafeCast(final TeamMember teamMember) {
        return (TeamMember<S>) teamMember;
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public static void split(List<TeamMember> members,
                             BiConsumer<List<Role>, List<JoinRequest>> listBiConsumer) {
        Map<Class, List> classListMap = new HashMap<>();

        Flowable.fromIterable(members).subscribe(model -> {
            Model wrapped = model.getWrappedModel();
            Class modelClass = wrapped.getClass();
            List items = classListMap.get(modelClass);

            if (items == null) classListMap.put(modelClass, items = new ArrayList<>());
            items.add(wrapped);
        });

        List<Role> roles = classListMap.get(Role.class);
        List<JoinRequest> requests = classListMap.get(JoinRequest.class);

        if (roles == null) roles = new ArrayList<>();
        if (requests == null) requests = new ArrayList<>();

        try { listBiConsumer.accept(roles, requests); }
        catch (Exception e) {Logger.log("TeamMember", "Error splitting", e);}
    }

    @Override
    public boolean isEmpty() { return wrappedModel.isEmpty(); }

    @Override
    public String getImageUrl() { return wrappedModel.getImageUrl(); }

    @Override
    public String getId() { return wrappedModel.getId(); }

    @Override
    public int compareTo(@NonNull TeamMember o) { return FunctionalDiff.COMPARATOR.compare(wrappedModel, o); }

    @Override
    @SuppressWarnings("unchecked")
    public void update(TeamMember updated) {}

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof TeamMember)) return getId().equals(other.getId());
        TeamMember casted = (TeamMember) other;
        return getWrappedModel().areContentsTheSame(casted.getWrappedModel());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamMember)) return false;
        TeamMember<?> that = (TeamMember<?>) o;
        return Objects.equals(wrappedModel, that.wrappedModel);
    }

    @Override
    public int hashCode() {
        return wrappedModel.hashCode();
    }

    @Override
    public int describeContents() {
        throw new IllegalArgumentException("TeamMember instances are not Parcelable");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new IllegalArgumentException("TeamMember instances are not Parcelable");
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<TeamMember> {

        private static final String NAME_KEY = "roleName";

        @Override
        public TeamMember deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            boolean isJoinRequest = jsonObject.has(NAME_KEY) && jsonObject.get(NAME_KEY).isJsonPrimitive();
            return TeamMember.fromModel(context.deserialize(jsonObject, isJoinRequest ? JoinRequest.class : Role.class));
        }
    }
}
