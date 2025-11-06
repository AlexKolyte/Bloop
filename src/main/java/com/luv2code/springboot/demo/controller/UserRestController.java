package com.luv2code.springboot.demo.controller;

import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.config.WebSocketListener;
import com.luv2code.springboot.demo.db.DBManager;
import com.luv2code.springboot.demo.entity.User;
import com.luv2code.springboot.demo.entity.UserPosition;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/loginPage/api")
public class UserRestController {
    public static ArrayList<User> users = new ArrayList<>();
    public static HashSet<User> onlineUsers = new HashSet<>();

    public static HashSet<UserPosition> userPositions = new HashSet<>();
    @PostConstruct
    public void fillUsers()
    {
        DBManager.getAllUsers();
    }

    @PostConstruct
    @GetMapping("/fillUserPositions")
    public void fillUserPositions()
    {
        DBManager.fillUserPositions();
    }

    @GetMapping("/users")
    public synchronized ArrayList<User> getUsers(){
        return users;
    }

    @GetMapping("/onlineUsers")
    public synchronized HashSet<User> getOnlineUsers()
    {
        onlineUsers.clear();
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
    @GetMapping("/updateOnlineUser/{new_username}/{new_password}/{email}")
    public void updateOnlineUser(@PathVariable String new_username,@PathVariable String new_password, @PathVariable String email) {
//        onlineUsers.add(new User(new_username, new_password, email));
        WebSocketListener.onlineUsers.add(new_username);
    }
    @GetMapping("/position/{lat}/{lon}/{username}")
    public void savePosition(@PathVariable String lat, @PathVariable String lon, @PathVariable String username) {
        DBManager.saveUserPosition(lat, lon, username);
    }

    @GetMapping("/updateUser/{new_username}/{new_password}/{old_username}")
    public void updateUser(@PathVariable String new_username, @PathVariable String new_password, @PathVariable String old_username) {
        DBManager.updateUser(new_username, new_password, old_username);
        for(int i = 0; i<users.size(); i++)
        {
            if (users.get(i).getUsername().equals(old_username))
            {
                users.get(i).setUsername(new_username);
                users.get(i).setPassword(new_password);
            }
        }
    }
    @GetMapping("/updateUsername/{new_username}/{old_username}")
    public void updateUsername(@PathVariable String new_username, @PathVariable String old_username) {
        DBManager.updateUsername(new_username, old_username);
        for(int i = 0; i<users.size(); i++)
        {
            if (users.get(i).getUsername().equals(old_username))
            {
                users.get(i).setUsername(new_username);
            }
        }
    }

    @GetMapping("/updatePosition/{lat}/{lon}/{username}")
    public void updatePosition(@PathVariable String lat, @PathVariable String lon, @PathVariable String username) {
        DBManager.updateUserPosition(lat, lon, username);
    }

    @GetMapping("/userPositions")
    public HashSet<UserPosition> getUserPositions()
    {
        return userPositions;
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

    @GetMapping("/getUserId/{name}")
    public int getUserByName(@PathVariable String name)
    {
        for(int i = 0; i<users.size(); i++)
        {
            if(users.get(i).getUsername().equals(name))
            {
                return i+1;
            }
        }
        return -1;
    }


    @Nullable
    @GetMapping("/updateUserAndCoordinates/{new_username}/{lat}/{lon}/{old_username}")
    public void updateUserAndCoordinates(@PathVariable String new_username, @PathVariable String lat, @PathVariable String lon, @PathVariable String old_username) {
        DBManager.updateUserAndCoordinates(new_username, lat, lon, old_username);
    }
}
