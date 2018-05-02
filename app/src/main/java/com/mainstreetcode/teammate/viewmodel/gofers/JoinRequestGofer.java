package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.repository.TeamMemberRepository;
import com.mainstreetcode.teammate.repository.UserRepository;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class JoinRequestGofer implements Gofer<JoinRequest> {

    @Retention(SOURCE)
    @IntDef({INVITING, JOINING, APPROVING, ACCEPTING, WAITING})
    public @interface JoinRequestState {}

    public static final int INVITING = 0;
    public static final int JOINING = 1;
    public static final int APPROVING = 2;
    public static final int ACCEPTING = 3;
    public static final int WAITING = 4;

    private int state;

    private int index;
    private final JoinRequest request;
    private final UserRepository userRepository;
    private final List<Item<JoinRequest>> items;

    private final BiFunction<JoinRequest, Boolean, Single<JoinRequest>> joinCompleter;

    public JoinRequestGofer(JoinRequest request, BiFunction<JoinRequest, Boolean, Single<JoinRequest>> joinCompleter) {
        this.request = request;
        this.joinCompleter = joinCompleter;
        userRepository = UserRepository.getInstance();
        items = new ArrayList<>();
        index = Flowable.range(0, request.asItems().size() - 1)
                .filter(index -> request.asItems().get(index).getItemType() == Item.ROLE)
                .first(0)
                .blockingGet();
        refresh();
    }

    public void refresh() {
        boolean isEmpty = request.isEmpty();
        boolean isRequestOwner = isRequestOwner();
        boolean isUserEmpty = request.getUser().isEmpty();
        boolean isUserApproved = request.isUserApproved();
        boolean isTeamApproved = request.isTeamApproved();

        state = isEmpty && isUserEmpty && isTeamApproved
                ? INVITING
                : isEmpty && isUserApproved && isRequestOwner
                ? JOINING
                : (!isEmpty && isUserApproved && isRequestOwner) || (!isEmpty && isTeamApproved && !isRequestOwner)
                ? WAITING
                : isTeamApproved && isRequestOwner ? ACCEPTING : APPROVING;

        items.clear();
        Flowable.fromIterable(request.asItems())
                .filter(this::filter)
                .collectInto(items, List::add)
                .subscribe();
    }

    public boolean showsFab(boolean hasPrivilegedRole) {
        switch (state) {
            case INVITING:
            case JOINING:
                return true;
            case APPROVING:
                return hasPrivilegedRole;
            case ACCEPTING:
                return isRequestOwner();
            default:
                return false;
        }
    }

    public boolean canEditFields() {
        return state == INVITING;
    }

    public boolean canEditRole() {
        return state == INVITING || state == JOINING;
    }

    public boolean isRequestOwner() {
        return userRepository.getCurrentUser().equals(request.getUser());
    }

    @JoinRequestState
    public int getState() {
        return state;
    }

    public String getToolbarTitle(Fragment fragment) {
        return fragment.getString(state == JOINING
                ? R.string.join_team
                : state == INVITING
                ? R.string.invite_user
                : state == WAITING
                ? R.string.pending_request
                : state == APPROVING ? R.string.approve_request : R.string.accept_request);
    }

    public Single<JoinRequest> joinTeam() {
        TeamMember<JoinRequest> member = TeamMember.fromModel(request);
        TeamMemberRepository<JoinRequest> repository = TeamMemberRepository.getInstance();

        return repository.createOrUpdate(member).map(ignored -> request).observeOn(mainThread());
    }

    public Single<JoinRequest> processJoinRequest(boolean approved) {
        return Single.defer(() -> joinCompleter.apply(request, approved));
    }

    public List<Item<JoinRequest>> getItems() {
        return items;
    }

    private boolean filter(Item<JoinRequest> item) {
        boolean isEmpty = request.isEmpty();
        int sortPosition = item.getSortPosition();

        if (item.getItemType() == Item.ROLE) return true;

        // Joining a team
        boolean joining = state == JOINING || state == ACCEPTING || (state == WAITING && isRequestOwner());
        if (joining) return sortPosition > index;

        // Inviting a user
        boolean ignoreTeam = sortPosition <= index;

        int stringRes = item.getStringRes();

        // About field should not show when inviting a user, email field should not show when trying
        // to join a team.
        return isEmpty
                ? ignoreTeam && stringRes != R.string.user_about
                : ignoreTeam && stringRes != R.string.email;
    }
}
