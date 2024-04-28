package com.example.fimae.models;

public class Follows {
    private String follower;
    private String following;
    private String id;
    private String message; // Thêm trường message

    public Follows(String follower, String following, String id, String message) { // Cập nhật constructor
        this.follower = follower;
        this.following = following;
        this.id = id;
        this.message = message;
    }

    public Follows(){
    }

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() { // Thêm phương thức getter cho trường message
        return message;
    }

    public void setMessage(String message) { // Thêm phương thức setter cho trường message
        this.message = message;
    }
}
