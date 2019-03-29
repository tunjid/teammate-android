package com.mainstreetcode.teammate.repository;


import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;

import java.util.Date;

abstract class TeamQueryRepo<T extends Model<T>> extends QueryRepo<T, Team, Date> {

    TeamQueryRepo() {}
}
