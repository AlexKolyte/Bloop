package com.luv2code.springboot.demo.controller;

import com.luv2code.springboot.demo.db.DBManager;
import com.luv2code.springboot.demo.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("loginPage/api")
public class UserRestController {
    public static ArrayList<User> users = new ArrayList<>();

    @PostConstruct
    public void fillUsers()
    {
        DBManager.getAllUsers();
    }

    @GetMapping("/users")
    public ArrayList<User> getUsers(){
        return users;
    }

    @GetMapping("/exists/{username}")
    public boolean userExists(@PathVariable String username) {
        boolean exists = DBManager.isUser(username);
        System.out.println("Checking if user exists: " + username + " = " + exists);
        return exists;
    }
    @GetMapping("/users/{userId}")
    public User getStudentById(@PathVariable Integer userId){
        User[] users = UserRestController.users.toArray(new User[UserRestController.users.size()]);
        return users[userId];
    }
}
