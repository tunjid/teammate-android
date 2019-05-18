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

package com.mainstreetcode.teammate.model.enums;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Objects;

public class MetaData implements Differentiable {

    String code;
    String name;

    MetaData(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean isInvalid() {
        return TextUtils.isEmpty(code);
    }

    @Override
    public String getId() { return code; }

    public CharSequence getName() { return name; }

    public String getCode() { return code; }

    public void update(MetaData updated) {
        this.code = updated.code;
        this.name = updated.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaData)) return false;
        MetaData metaData = (MetaData) o;
        return Objects.equals(code, metaData.code);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        if (!(other instanceof MetaData)) return getId().equals(other.getId());
        MetaData casted = (MetaData) other;
        return this.code.equals(casted.code) && this.name.equals(casted.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    public static abstract class GsonAdapter<T extends MetaData>
            implements
            JsonSerializer<T>,
            JsonDeserializer<T> {

        private static final String CODE_KEY = "code";
        private static final String NAME_KEY = "name";

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject baseEnumJson = json.getAsJsonObject();

            String code = ModelUtils.asString(CODE_KEY, baseEnumJson);
            String name = ModelUtils.asString(NAME_KEY, baseEnumJson);

            return fromJson(code, name, baseEnumJson, context);
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(CODE_KEY, src.code);
            serialized.addProperty(NAME_KEY, src.name);

            return toJson(serialized, src, context);
        }

        abstract T fromJson(String code, String name, JsonObject body, JsonDeserializationContext context);

        JsonObject toJson(JsonObject serialized, T src, JsonSerializationContext context) {
            return serialized;
        }
    }
}


