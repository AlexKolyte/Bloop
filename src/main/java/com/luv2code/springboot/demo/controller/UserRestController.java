package com.luv2code.springboot.demo.controller;

import com.luv2code.springboot.demo.config.WebSocketListener;
import com.luv2code.springboot.demo.db.DBManager;
import com.luv2code.springboot.demo.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("loginPage/api")
public class UserRestController {
    public static ArrayList<User> users = new ArrayList<>();

    public static HashSet<User> onlineUsers = new HashSet<>();
    @PostConstruct
    public void fillUsers()
    {
        DBManager.getAllUsers();
    }

    @GetMapping("/users")
    public ArrayList<User> getUsers(){
        return users;
    }

    @GetMapping("/onlineUsers")
    public HashSet<User> getOnlineUsers()
    {
        for (int i= 0; i<users.size(); i++)
        {
            for (int j = 0; j<WebSocketListener.onlineUsers.size(); j++){
                if(users.get(i).getUsername().equals(WebSocketListener.onlineUsers.get(j)))
                {
                    onlineUsers.add(users.get(i));
                }
            }
        }
        return onlineUsers;
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
