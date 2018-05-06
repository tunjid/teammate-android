package com.mainstreetcode.teammate.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdCache {

    private final List<String> ids;

    public static IdCache cache(int count) {return new IdCache(count);}

    private IdCache(int count) {
        ids = Collections.unmodifiableList(buildUniqueIds(count));
    }

    public String get(int index){return ids.get(index);}

    private List<String> buildUniqueIds(int count) {
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) result.add(new ObjectId().toHexString());
        return result;
    }
}
