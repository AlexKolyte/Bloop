package com.luv2code.springboot.demo.entity;

public class InstagramDTO {
    private String username;
    private String fullName;
    private String biography;
    private int    followers;
    private int    following;
    private int    mediaCount;
    private String profilePicUrl;
    private String latestMediaUrl;



    // getters + setters
    public String getUsername()        { return username; }
    public void   setUsername(String u){ this.username = u; }
    public String getFullName()        { return fullName; }
    public void   setFullName(String f){ this.fullName = f; }
    public String getBiography()       { return biography; }
    public void   setBiography(String b){ this.biography = b; }
    public int    getFollowers()       { return followers; }
    public void   setFollowers(int f)  { this.followers = f; }
    public int    getFollowing()       { return following; }
    public void   setFollowing(int f)  { this.following = f; }
    public int    getMediaCount()      { return mediaCount; }
    public void   setMediaCount(int m) { this.mediaCount = m; }
    public String getProfilePicUrl()   { return profilePicUrl; }
    public void   setProfilePicUrl(String u){ this.profilePicUrl = u; }
    public String getLatestMediaUrl()  { return latestMediaUrl; }
    public void   setLatestMediaUrl(String u){ this.latestMediaUrl = u; }
}

