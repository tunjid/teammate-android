package com.mainstreetcode.teammate.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Objects;

@SuppressLint("ParcelCreator")
public class TeamMember<S extends Model<S>> implements Model<TeamMember<S>> {

    private final S wrappedModel;

    private TeamMember(S wrappedModel) {this.wrappedModel = wrappedModel;}

    public S getWrappedModel() {
        return wrappedModel;
    }

    public static <S extends Model<S>> TeamMember<S> fromModel(final S wrappedModel) {
        return new TeamMember<>(wrappedModel);
    }

    @SuppressWarnings("unchecked")
    public static <S extends Model<S>> TeamMember<S> unsafeCast(final TeamMember teamMember) {
        return (TeamMember<S>) teamMember;
    }

    @Override
    public boolean isEmpty() { return wrappedModel.isEmpty(); }

    @Override
    public String getImageUrl() { return wrappedModel.getImageUrl(); }

    @Override
    public String getId() { return wrappedModel.getId(); }

    @Override
    public int compareTo(@NonNull TeamMember o) { return Identifiable.COMPARATOR.compare(wrappedModel, o); }

    @Override
    public void reset() {wrappedModel.reset();}

    @Override
    @SuppressWarnings("unchecked")
    public void update(TeamMember updated) {
        Model<?> wrapped = updated.getWrappedModel();
        if (wrappedModel.getClass().equals(wrapped.getClass()))
            wrappedModel.update((S) wrapped);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
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
        return Objects.hash(wrappedModel);
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
