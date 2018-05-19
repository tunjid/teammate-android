package com.mainstreetcode.teammate.repository;


import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;

abstract class TeamQueryRepository<T extends Model<T>> extends QueryRepository<T, Team> {

    TeamQueryRepository() {}
}
