package vn.hoidanit.laptopshop.service;

import org.springframework.stereotype.Service;
import java.util.*;

import vn.hoidanit.laptopshop.domain.Role;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.RoleRepository;
import vn.hoidanit.laptopshop.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public List<User> getAllUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User getAllUserById(long id) {
        return this.userRepository.findById(id);
    }

    public User handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public void deleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User findById(long id) {
        return this.userRepository.findById(id);
    }

    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name);
    }
}
