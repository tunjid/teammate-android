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

import androidx.annotation.Nullable;

import android.text.TextUtils;

import java.util.Objects;

public class TeamSearchRequest {

    private String name;
    private String screenName;
    private String sport;

    public static TeamSearchRequest from(@Nullable String sportCode) {
        TeamSearchRequest request = new TeamSearchRequest();
        if (!TextUtils.isEmpty(sportCode)) request.sport = sportCode;
        return request;
    }

    private TeamSearchRequest() {}

    public TeamSearchRequest query(String query) {
        TeamSearchRequest request = new TeamSearchRequest();
        request.name = query.startsWith("@") ? "" : query;
        request.screenName = query.startsWith("@") ? query.replaceFirst("@", "") : "";
        request.sport = this.sport;
        return request;
    }

    public String getName() { return name; }

    public String getScreenName() {
        return screenName;
    }

    @Nullable
    public String getSport() { return sport; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamSearchRequest)) return false;
        TeamSearchRequest that = (TeamSearchRequest) o;
        return Objects.equals(name, that.name)
                && Objects.equals(screenName, that.screenName)
                && Objects.equals(sport, that.sport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, screenName, sport);
    }
}
