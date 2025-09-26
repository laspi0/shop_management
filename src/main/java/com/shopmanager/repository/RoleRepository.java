package com.shopmanager.repository;

import com.shopmanager.model.Role;

public class RoleRepository extends BaseRepository<Role> {
    public Role findByName(String name) {
        return get(s -> s.createQuery("from Role r where r.name = :n", Role.class)
                .setParameter("n", name)
                .uniqueResult());
    }
    public void save(Role r) { runInTransaction(s -> s.persist(r)); }
}
