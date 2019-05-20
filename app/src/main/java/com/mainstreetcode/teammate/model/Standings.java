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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

/**
 * Event events
 */

public class Standings {

    private String id;
    private String tournamentId;

    private Row titleRow = Row.empty();
    private List<Row> table = new ArrayList<>();

    public static Standings forTournament(Tournament tournament) {
        return new Standings("", tournament.getId());
    }

    private Standings(String id, String tournamentId) {
        this.id = id;
        this.tournamentId = tournamentId;
    }

    public List<String> getColumnNames() { return titleRow.getColumns(); }

    public Standings update(Standings other) {
        this.id = other.id;
        this.tournamentId = other.tournamentId;
        table.clear();
        table.addAll(other.table);
        titleRow.update(other.titleRow);
        return this;
    }

    public List<Row> getTable() { return table; }

    public static class GsonAdapter
            implements
            JsonDeserializer<Standings> {

        private static final String ID = "_id";
        private static final String TOURNAMENT = "tournament";
        private static final String TABLE = "table";
        private static final String COLUMNS = "columns";

        @Override
        @SuppressLint("CheckResult")
        public Standings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID, body);
            String tournament = ModelUtils.asString(TOURNAMENT, body);
            Standings standings = new Standings(id, tournament);

            JsonArray table = body.get(TABLE).getAsJsonArray();
            if (table.size() == 0) return standings;

            ModelUtils.deserializeList(context, table, standings.table, Row.class);
            JsonObject columnObject = table.get(0).getAsJsonObject().get(COLUMNS).getAsJsonObject();

            Flowable.fromIterable(columnObject.entrySet())
                    .filter(entry -> !entry.getKey().equals("competitor"))
                    .map(Map.Entry::getKey).map(Object::toString)
                    .subscribe(standings.titleRow::add, ErrorHandler.EMPTY);

            return standings;
        }
    }
}
