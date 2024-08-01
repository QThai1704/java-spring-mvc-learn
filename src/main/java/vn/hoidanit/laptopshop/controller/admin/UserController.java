package vn.hoidanit.laptopshop.controller.admin;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UploadService;
import vn.hoidanit.laptopshop.service.UserService;
import java.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import java.io.*;

@Controller
public class UserController {

    // DI: Dependency injection
    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/")
    public String getHomePage(Model model) {
        List<User> arrUsers = this.userService.getAllUserByEmail("1@gmail.com");
        model.addAttribute("arrUsers", arrUsers);
        return "hello";
    }

    @RequestMapping("/admin/user")
    public String getListUser(Model model) {
        List<User> users = this.userService.getAllUsers();
        model.addAttribute("users", users);
        return "/admin/user/show";
    }

    // Create
    @GetMapping("/admin/user/create")
    public String getUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "/admin/user/create";
    }

    @PostMapping("/admin/user/create")
    // @ModelAttribute("newUser") User hoidanit: Lấy ra các giá trị của form của
    // giao diện
    public String createUserPage(Model model, @ModelAttribute("newUser") User hoidanit,
            @RequestParam("hoidanitFile") MultipartFile file) {
        String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(hoidanit.getPassword());
        hoidanit.setAvatar(avatar);
        hoidanit.setPassword(hashPassword);
        hoidanit.setRole(this.userService.getRoleByName(hoidanit.getRole().getName()));
        this.userService.handleSaveUser(hoidanit);
        return "redirect:/admin/user";
    }

    // Show
    @RequestMapping(value = "/admin/user/{id}")
    // @PathVariable long hoidanit: Lấy ra giá trị của hoidanit trên URL
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getAllUserById(id);
        model.addAttribute("user", user);
        return "/admin/user/detail";
    }

    // Update
    @RequestMapping(value = "/admin/user/update/{id}")
    // @PathVariable long hoidanit: Lấy ra giá trị của hoidanit trên URL
    public String getUpdateUserPage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getAllUserById(id);
        model.addAttribute("newUser", currentUser);
        return "/admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(Model model, @ModelAttribute("newUser") User hoidanit) {
        User currentUser = this.userService.getAllUserById(hoidanit.getId());
        if (currentUser != null) {
            currentUser.setFullName(hoidanit.getFullName());
            currentUser.setAddress(hoidanit.getAddress());
            currentUser.setPhone(hoidanit.getPhone());
            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user";
    }

    // Delete
    @GetMapping("/admin/user/delete/{id}")
    public String getDeleteUserPage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getAllUserById(id);
        model.addAttribute("newUser", currentUser);
        return "/admin/user/delete";
    }

    @PostMapping(value = "/admin/user/delete")
    public String postDeleteUser(Model model, @ModelAttribute("newUser") User hoidanit) {
        this.userService.deleteUser(hoidanit.getId());
        return "redirect:/admin/user";
    }

}