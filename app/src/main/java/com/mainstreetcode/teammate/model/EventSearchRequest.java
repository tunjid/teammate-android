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

import androidx.room.Ignore;
import android.location.Address;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.IdCache;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mainstreetcode.teammate.util.ModelUtils.parse;
import static com.mainstreetcode.teammate.util.ModelUtils.prettyPrinter;

public class EventSearchRequest implements ListableModel<EventSearchRequest> {

    @Ignore private static final IdCache holder = IdCache.cache(5);

    private int distance;
    private Address address;
    private Sport sport;
    private Date startDate;
    private Date endDate;
    private LatLng location;

    private final List<Item<EventSearchRequest>> items;

    private EventSearchRequest(int distance, Sport sport, LatLng location, Date startDate, Date endDate) {
        this.distance = distance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.sport = sport;
        items = buildItems();
    }

    public static EventSearchRequest empty() {
        return new EventSearchRequest(5, Sport.empty(), null, new Date(), new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
    }

    public void setAddress(Address address) {
        this.address = address;
        items.get(0).setValue(getAddress());
    }

    public void setDistance(String distance) {
        this.distance = parse(distance);
        items.get(1).setValue(getDistance());
    }

    public void setSport(String sport) { this.sport = Config.sportFromCode(sport); }

    private void setStartDate(String startDate) {
        this.startDate = ModelUtils.parseDate(startDate, prettyPrinter);
    }

    private void setEndDate(String endDate) {
        this.endDate = ModelUtils.parseDate(endDate, prettyPrinter);
    }

    public void setLocation(LatLng location) { this.location = location; }

    @Nullable
    public LatLng getLocation() { return location; }

    public Sport getSport() { return sport; }

    private CharSequence getAddress() { return address == null ? "": address.getLocality() + ", " + address.getAdminArea(); }

    private CharSequence getSportName() { return sport.getName(); }

    private CharSequence getDistance() { return App.getInstance().getString(R.string.event_public_distance, distance); }

    private CharSequence getStartDate() { return ModelUtils.prettyPrinter.format(startDate); }

    private CharSequence getEndDate() { return ModelUtils.prettyPrinter.format(endDate); }

    @SuppressWarnings("unchecked")
    private List<Item<EventSearchRequest>> buildItems() {
        return Arrays.asList(
                Item.text(holder.get(0), 0, Item.LOCATION, R.string.location, this::getAddress, ignored -> {}, this),
                Item.text(holder.get(1),1, Item.INFO, R.string.event_distance, this::getDistance, ignored -> {}, this),
                Item.text(holder.get(2),2, Item.SPORT, R.string.team_sport, this::getSportName, this::setSport, this)
                        .textTransformer(value -> Config.sportFromCode(value.toString()).getName()),
                Item.text(holder.get(3),3, Item.DATE, R.string.start_date, this::getStartDate, this::setStartDate, this),
                Item.text(holder.get(4),4, Item.DATE, R.string.end_date, this::getEndDate, this::setEndDate, this)
        );
    }

    @Override
    public List<Item<EventSearchRequest>> asItems() { return items; }

    public static class GsonAdapter implements JsonSerializer<EventSearchRequest> {

        private static final String DISTANCE_KEY = "maxDistance";
        private static final String SPORT_KEY = "sport";
        private static final String LOCATION_KEY = "location";
        private static final String START_DATE_KEY = "startDate";
        private static final String END_DATE_KEY = "endDate";

        @Override
        public JsonElement serialize(EventSearchRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject serialized = new JsonObject();

            serialized.addProperty(DISTANCE_KEY, src.distance);

            if (src.sport != null) {
                serialized.addProperty(SPORT_KEY, src.sport.getCode());
            }
            if (src.startDate != null) {
                serialized.addProperty(START_DATE_KEY, ModelUtils.dateFormatter.format(src.startDate));
                if (src.endDate != null) {
                    serialized.addProperty(END_DATE_KEY, ModelUtils.dateFormatter.format(src.endDate));
                }
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
