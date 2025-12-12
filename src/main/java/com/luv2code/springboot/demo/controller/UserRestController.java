package com.luv2code.springboot.demo.controller;

import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.config.WebSocketListener;
import com.luv2code.springboot.demo.db.DBManager;
import com.luv2code.springboot.demo.entity.User;
import com.luv2code.springboot.demo.entity.UserDescription;
import com.luv2code.springboot.demo.entity.UserPosition;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/loginPage/api")
public class UserRestController {
    public static ArrayList<User> users = new ArrayList<>();
    public static HashSet<User> onlineUsers = new HashSet<>();

    public static HashSet<UserPosition> userPositions = new HashSet<>();

    public static HashSet<UserDescription> userDescriptions = new HashSet<>();
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
    @GetMapping("/saveDescription/{username}/{description}")
    public void saveDescription(@PathVariable String username, @PathVariable String description)
    {
        int pointer = 0;
        boolean check = false;
        int counter = -1;
        List<UserDescription> userDescriptions1 = userDescriptions.stream().toList();
        if(userDescriptions.size() == 0)
        {
            counter = 0;
        }
            for (int i = 0; i < userDescriptions1.size(); i++) {
                if (userDescriptions1.get(i).getUsername().equals(username) && userDescriptions1.get(i).getDescription().equals(description)) {
                    break;
                } else if (userDescriptions1.get(i).getUsername().equals(username) && !userDescriptions1.get(i).getDescription().equals(description)) {
                    check = true;
                    pointer  = i;
                    counter = 0;
                } else {
                    counter = 0;
                }
            }
            if(counter == 0 && check)
            {
                userDescriptions1.get(pointer).setDescription(description);
                DBManager.updateUserDescription(username, description);
                userDescriptions.clear();
                userDescriptions.addAll(userDescriptions1);

            } else if (counter == 0 && !check) {
                if(!DBManager.getUserDescription(username).equals(description)) {
                    DBManager.saveDescription(username, description);
                    userDescriptions.add(new UserDescription(username, description));
                }
            }
        }



    @GetMapping("/findRandomFriend/{username}")
    public UserDescription findRandomFriend(@PathVariable String username)
    {
       return DBManager.findRandomFriend(username);
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

    @PostConstruct
    public void fillUserDescriptions()
    {
        DBManager.fillAllUserDescriptions();
    }

    @GetMapping("/getUserDescription/{username}")
    public String getUserDescription(@PathVariable String username)
    {
        return DBManager.getUserDescription(username);
    }

    @Nullable
    @GetMapping("/updateUserAndCoordinates/{new_username}/{lat}/{lon}/{old_username}")
    public void updateUserAndCoordinates(@PathVariable String new_username, @PathVariable String lat, @PathVariable String lon, @PathVariable String old_username) {
        DBManager.updateUserAndCoordinates(new_username, lat, lon, old_username);
    }
}
