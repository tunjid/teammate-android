package com.mainstreetcode.teammate.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Event events
 */

public class Standings  {

    private String id;
    private String tournamentId;
    private List<Row> table = new ArrayList<>();

    private Standings(String id, String tournamentId) {
        this.id = id;
        this.tournamentId = tournamentId;
    }

    public static class GsonAdapter
            implements
            JsonDeserializer<Standings> {

        private static final String ID = "_id";
        private static final String TOURNAMENT = "tournament";
        private static final String TABLE = "table";

        @Override
        public Standings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID, body);
            String tournament = ModelUtils.asString(TOURNAMENT, body);
            Standings standings = new Standings(id, tournament);

            ModelUtils.deserializeList(context, body.get(TABLE),standings.table,Row.class);

            return standings;
        }
    }
}
