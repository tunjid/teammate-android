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
import java.util.Map;

/**
 * Event events
 */

public class Row implements Identifiable {

    private final String id;
    private final Competitor competitor;
    private final List<String> tableValues = new ArrayList<>();

    public static Row empty() {
        return new Row("", Competitor.empty());
    }

    private Row(String id, Competitor competitor) {
        this.id = id;
        this.competitor = competitor;
    }

    public Competitor getCompetitor() { return competitor; }

    public void add(String column) { tableValues.add(column); }

    public void update(Row updated) {
        tableValues.clear();
        tableValues.addAll(updated.tableValues);
    }

    public String getId() { return id; }

    public String getImageUrl() { return competitor.getImageUrl(); }

    public CharSequence getName() { return competitor.getName(); }

    public List<String> getColumns() { return tableValues; }

    public static class GsonAdapter
            implements
            JsonDeserializer<Row> {

        private static final String ID_KEY = "_id";
        private static final String COMPETITOR = "competitor";
        private static final String COLUMNS = "columns";

        @Override
        public Row deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject body = json.getAsJsonObject();

            String id = ModelUtils.asString(ID_KEY, body);
            Competitor competitor = context.deserialize(body.get(COMPETITOR), Competitor.class);

            Row row = new Row(id, competitor);

            JsonElement columnElement = body.get(COLUMNS);
            if (columnElement == null || !columnElement.isJsonObject()) return row;

            JsonObject columnObject = columnElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : columnObject.entrySet())
                row.tableValues.add(entry.getValue().toString());

            return row;
        }
    }
}
