package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;

public class CompetitorTypeConverter {

    private final UserTypeConverter userTypeConverter = new UserTypeConverter();
    private final TeamTypeConverter teamTypeConverter = new TeamTypeConverter();

    @TypeConverter
    public String toDbValue(Competitor competitor) {
        return competitor == null ? "" : competitor.getType() + "," + competitor.getId();
    }

    @TypeConverter
    public Competitor fromId(String source) {
        if (TextUtils.isEmpty(source)) return Competitor.Util.empty();
        String[] split = source.split(",");
        String type = split[0];
        String id = split[1];

        switch (type) {
            case User.COMPETITOR_TYPE:
                return userTypeConverter.fromId(id);
            case Team.COMPETITOR_TYPE:
                return teamTypeConverter.fromId(id);
            default:
                return Competitor.Util.empty();
        }
    }
}
