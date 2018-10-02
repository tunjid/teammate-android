package com.mainstreetcode.teammate.viewmodel;

import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.BlockedUserRepository;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static com.mainstreetcode.teammate.util.ModelUtils.findLast;

public class BlockedUserViewModel extends TeamMappedViewModel<BlockedUser> {

    private final BlockedUserRepository repository;

    public BlockedUserViewModel() {
        repository = BlockedUserRepository.getInstance();
    }

    public BlockedUserGofer gofer(BlockedUser blockedUser) {
        return new BlockedUserGofer(blockedUser, onError(blockedUser), this::unblockUser);
    }

    @Override
    boolean hasNativeAds() {return false;}

    @Override
    Flowable<List<BlockedUser>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest));
    }

    public Single<BlockedUser> blockUser(BlockedUser blockedUser) {
        return BlockedUserRepository.getInstance().createOrUpdate(blockedUser)
                .doOnSuccess(blocked -> pushModelAlert(Alert.userBlocked(blockedUser)));
    }

    private Single<BlockedUser> unblockUser(BlockedUser blockedUser) {
        return repository.delete(blockedUser).doOnSuccess(getModelList(blockedUser.getTeam())::remove);
    }

    @Nullable
    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;

        BlockedUser media = findLast(getModelList(team), BlockedUser.class);
        return media == null ? null : media.getCreated();
    }
}
