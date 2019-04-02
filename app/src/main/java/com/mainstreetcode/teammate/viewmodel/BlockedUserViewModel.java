package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.repository.BlockedUserRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class BlockedUserViewModel extends TeamMappedViewModel<BlockedUser> {

    private final BlockedUserRepo repository;

    public BlockedUserViewModel() {
        repository = RepoProvider.forRepo(BlockedUserRepo.class);
    }

    public BlockedUserGofer gofer(BlockedUser blockedUser) {
        return new BlockedUserGofer(blockedUser, onError(blockedUser), this::unblockUser);
    }

    @Override
    Class<BlockedUser> valueClass() { return BlockedUser.class; }

    @Override
    boolean hasNativeAds() {return false;}

    @Override
    Flowable<List<BlockedUser>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, BlockedUser::getCreated));
    }

    public Single<BlockedUser> blockUser(BlockedUser blockedUser) {
        return RepoProvider.forRepo(BlockedUserRepo.class).createOrUpdate(blockedUser)
                .doOnSuccess(blocked -> pushModelAlert(Alert.creation(blockedUser)));
    }

    private Single<BlockedUser> unblockUser(BlockedUser blockedUser) {
        return repository.delete(blockedUser).doOnSuccess(getModelList(blockedUser.getTeam())::remove);
    }
}
