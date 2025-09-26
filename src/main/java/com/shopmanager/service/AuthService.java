package com.shopmanager.service;

import com.shopmanager.model.Role;
import com.shopmanager.model.User;
import com.shopmanager.repository.UserRepository;
import com.shopmanager.repository.RoleRepository;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserRepository userRepo = new UserRepository();
    private final RoleRepository roleRepo = new RoleRepository();

    public User login(String email, String password) {
        User user = userRepo.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public void ensureDefaultUsers() {
        // ADMIN role & user
        Role adminRole = roleRepo.findByName("ADMIN");
        if (adminRole == null) { adminRole = new Role("ADMIN"); roleRepo.save(adminRole); }
        User admin = userRepo.findByEmail("admin@shop.com");
        if (admin == null) {
            admin = new User();
            admin.setEmail("admin@shop.com");
            admin.setFullName("Administrateur");
            admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setRole(adminRole);
            userRepo.save(admin);
        } else {
            // ensure known password and role
            admin.setRole(adminRole);
            admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            userRepo.save(admin);
        }

        // CASHIER role & user
        Role cashierRole = roleRepo.findByName("CASHIER");
        if (cashierRole == null) { cashierRole = new Role("CASHIER"); roleRepo.save(cashierRole); }
        User cashier = userRepo.findByEmail("cashier@shop.com");
        if (cashier == null) {
            cashier = new User();
            cashier.setEmail("cashier@shop.com");
            cashier.setFullName("Caissier");
            cashier.setPasswordHash(BCrypt.hashpw("cashier123", BCrypt.gensalt()));
            cashier.setRole(cashierRole);
            userRepo.save(cashier);
        } else {
            cashier.setRole(cashierRole);
            cashier.setPasswordHash(BCrypt.hashpw("cashier123", BCrypt.gensalt()));
            userRepo.save(cashier);
        }
    }
}
