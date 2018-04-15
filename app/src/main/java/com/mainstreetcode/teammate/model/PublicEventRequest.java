package com.mainstreetcode.teammate.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mainstreetcode.teammate.util.ModelUtils.parse;
import static com.mainstreetcode.teammate.util.ModelUtils.prettyPrinter;

public class PublicEventRequest implements ItemListableBean<PublicEventRequest> {

    private int distance;
    private Sport sport;
    private Date endDate;
    private LatLng location;

    private final List<Item<PublicEventRequest>> items;

    private PublicEventRequest(int distance, Sport sport, Date endDate, LatLng location) {
        this.distance = distance;
        this.endDate = endDate;
        this.location = location;
        this.sport = sport;
        items = buildItems();
    }

    public static PublicEventRequest empty() {
        return new PublicEventRequest(5, Sport.empty(), new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)), null);
    }

    public void setDistance(String distance) {
        this.distance = parse(distance);
        items.get(0).setValue(getDistance());
    }

    public void setSport(String sport) { this.sport = Config.sportFromCode(sport); }

    private void setEndDate(String endDate) {
        this.endDate = ModelUtils.parseDate(endDate, prettyPrinter);
    }

    public void setLocation(LatLng location) { this.location = location; }

    private CharSequence getDistance() {
        return App.getInstance().getString(R.string.event_public_distance, distance);
    }

    private CharSequence getSport() { return sport.getName(); }

    private CharSequence getEndDate() { return ModelUtils.prettyPrinter.format(endDate); }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<PublicEventRequest>> buildItems() {
        return Arrays.asList(
                Item.text(Item.INFO, R.string.event_distance, this::getDistance, ignored -> {}, this),
                Item.text(Item.SPORT, R.string.team_sport, this::getSport, this::setSport, this),
                Item.text(Item.DATE, R.string.start_date, this::getEndDate, this::setEndDate, this)
        );
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Item get(int position) {
        return items.get(position);
    }

    public static class GsonAdapter implements JsonSerializer<PublicEventRequest> {

        private static final String DISTANCE_KEY = "maxDistance";
        private static final String SPORT_KEY = "sport";

        private static final String LOCATION_KEY = "location";

        @Override
        public JsonElement serialize(PublicEventRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(DISTANCE_KEY, src.distance);

            if (src.sport != null) {
                serialized.addProperty(SPORT_KEY, src.sport.getCode());
            }

            if (src.location != null) {
                JsonArray coordinates = new JsonArray();
                coordinates.add(src.location.longitude);
                coordinates.add(src.location.latitude);
                serialized.add(LOCATION_KEY, coordinates);
            }

            return serialized;
        }

    }
}
