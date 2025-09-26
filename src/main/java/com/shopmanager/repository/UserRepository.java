package com.shopmanager.repository;

import com.shopmanager.model.User;
import org.hibernate.Session;

public class UserRepository extends BaseRepository<User> {
    public User findByEmail(String email) {
        return get((Session s) -> s.createQuery("from User u where u.email = :e", User.class)
                .setParameter("e", email)
                .uniqueResult());
    }

    public void save(User user) {
        runInTransaction(s -> s.merge(user));
    }
}
