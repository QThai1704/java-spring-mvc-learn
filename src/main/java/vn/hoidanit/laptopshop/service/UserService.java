package vn.hoidanit.laptopshop.service;

import org.springframework.stereotype.Service;
import java.util.*;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    // public User getDeleteUser(long id) {
    // return this.getDeleteUser(id);
    // }

    public User findById(long id) {
        return this.userRepository.findById(id);
    }
}
