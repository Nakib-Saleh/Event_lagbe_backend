package com.eventlagbe.backend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "participants")
public class Participant {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String name;
    private String firebaseUid;

    @Indexed(unique = true)
    private String username;
    
    private String institution = "N/A";

    private String profilePictureUrl = "https://res.cloudinary.com/dfvwazcdk/image/upload/v1753161431/generalProfilePicture_inxppe.png";
    private String bannerUrl = "https://res.cloudinary.com/dfvwazcdk/image/upload/v1753513555/banner_z0sar4.png";
    private List<String> idDocumentUrls;
    private boolean isVerified = false;
    private List<String> interestedSkills = new ArrayList<>();

    private List<String> bookmarkedEventIds = new ArrayList<>();
    private List<String> registeredEventIds = new ArrayList<>();
    private List<String> pastEventIds = new ArrayList<>();


    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getBookmarkedEventIds() { return bookmarkedEventIds; }
    public void setBookmarkedEventIds(List<String> bookmarkedEventIds) { this.bookmarkedEventIds = bookmarkedEventIds; }



    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public List<String> getIdDocumentUrls() { return idDocumentUrls; }
    public void setIdDocumentUrls(List<String> idDocumentUrls) { this.idDocumentUrls = idDocumentUrls; }

    public boolean getIsVerified() { return isVerified; }
    public void setIsVerified(boolean isVerified) { this.isVerified = isVerified; }

    public List<String> getInterestedSkills() { return interestedSkills; }
    public void setInterestedSkills(List<String> interestedSkills) { this.interestedSkills = interestedSkills; }

    public List<String> getRegisteredEventIds() { return registeredEventIds; }
    public void setRegisteredEventIds(List<String> registeredEventIds) { this.registeredEventIds = registeredEventIds; }

    public List<String> getPastEventIds() { return pastEventIds; }
    public void setPastEventIds(List<String> pastEventIds) { this.pastEventIds = pastEventIds; }


    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> following) { this.following = following; }
}
