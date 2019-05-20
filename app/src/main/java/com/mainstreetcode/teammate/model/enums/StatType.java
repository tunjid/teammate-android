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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

import java.util.Objects;

public class StatType extends MetaData {

    private String emoji;
    private String sportCode;
    private StatAttributes attributes = new StatAttributes();

    private StatType(String code, String name, String emoji, String sportCode) {
        super(code, name);
        this.emoji = emoji;
        this.sportCode = sportCode;
    }

    public static StatType empty() {
        return new StatType("", "", "", "");
    }

    public CharSequence getEmoji() { return ModelUtils.processString(emoji); }

    public CharSequence getEmojiAndName() {
        return SpanBuilder.of(getEmoji()).append("   ").append(name).build();
    }

    public StatAttributes getAttributes() {
        return attributes;
    }

    public StatAttribute fromCode(String code) {
        for (StatAttribute attr : attributes) if (attr.code.equals(code)) return attr;
        return StatAttribute.empty();
    }

    @Override
    public String toString() {
        return name;
    }

    public void update(StatType updated) {
        super.update(updated);
        this.emoji = updated.emoji;
        this.sportCode = updated.sportCode;
        ModelUtils.replaceList(attributes, updated.attributes);
    }

    @Override
    public boolean areContentsTheSame(Differentiable other) {
        boolean result = super.areContentsTheSame(other);
        if (!(other instanceof StatType)) return result;
        return result && sportCode.equals(((StatType) other).sportCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatType)) return false;
        StatType variant = (StatType) o;
        return Objects.equals(code, variant.code) && Objects.equals(name, variant.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public static class GsonAdapter extends MetaData.GsonAdapter<StatType> {

        private static final String EMOJI_KEY = "emoji";
        private static final String SPORT = "sport";
        private static final String ATTRIBUTES = "attributes";

        @Override
        StatType fromJson(String code, String name, JsonObject body, JsonDeserializationContext context) {
            String emoji = ModelUtils.asString(EMOJI_KEY, body);
            String sportCode = ModelUtils.asString(SPORT, body);

            StatType type = new StatType(code, name, emoji, sportCode);
            ModelUtils.deserializeList(context, body.get(ATTRIBUTES), type.attributes, StatAttribute.class);
            return type;
        }

        @Override
        JsonObject toJson(JsonObject serialized, StatType src, JsonSerializationContext context) {
            serialized.addProperty(EMOJI_KEY, src.emoji);
            serialized.addProperty(SPORT, src.sportCode);
            serialized.add(ATTRIBUTES, context.serialize(src.attributes));

            return serialized;
        }
    }
}
