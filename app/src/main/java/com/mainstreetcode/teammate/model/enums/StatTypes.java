package com.mainstreetcode.teammate.model.enums;

import java.util.ArrayList;

public class StatTypes extends ArrayList<StatType> {

    public StatType fromCodeOrFirst(String code) {
        if (isEmpty()) return StatType.empty();
        for (StatType type : this) if (type.getCode().equals(code)) return type;
        return get(0);
    }
}
