package pl.kacpermajkowski.ChunkyPlots.user;

import java.util.Collection;
import java.util.Set;

public interface UserRepository {
    Set<User> loadAll();
    void save(User user);
    void saveAll(Collection<User> users);
}
