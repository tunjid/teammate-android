package com.mainstreetcode.teammate.viewmodel.gofers;

import com.mainstreetcode.teammate.model.ListableModel;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.repository.UserRepository;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * Interface for liaisons between a ViewModel and a single instance of it's Model
 */
public abstract class TeamHostingGofer<T extends Model<T> & ListableModel<T> & TeamHost> extends Gofer<T> {

    private Role currentRole;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    TeamHostingGofer(T model, Consumer<Throwable> onError) {
        super(model, onError);
        currentRole = Role.empty();
        userRepository = UserRepository.getInstance();
        roleRepository = RoleRepository.getInstance();

        startPrep();
    }

    @Override
    Flowable<Boolean> changeEmitter() {
        return roleRepository.getRoleInTeam(userRepository.getCurrentUser().getId(), model.getTeam().getId())
                .map(this::onRoleFound).observeOn(mainThread());
    }

    public boolean hasRole() {return !currentRole.isEmpty();}

    public boolean hasPrivilegedRole() {
        return currentRole.isPrivilegedRole();
    }

    User getSignedInUser() {
        return userRepository.getCurrentUser();
    }

    private boolean onRoleFound(Role foundRole) {
        boolean changed = !currentRole.getPosition().equals(foundRole.getPosition());
        currentRole.update(foundRole);
        return changed;
    }
}
