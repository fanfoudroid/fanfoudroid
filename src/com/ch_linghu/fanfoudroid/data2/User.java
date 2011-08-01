package com.ch_linghu.fanfoudroid.data2;

import java.util.Date;

public class User {
	private String id;
	private String name;
	private String screen_name;
	private String location;
	private String desription;
	private String profile_image_url;
	private String url;
	private boolean isProtected;
	private int friends_count;
	private int followers_count;
	private int favourites_count;
	private Date created_at;
	private boolean following;
	private boolean notifications;
	private int utc_offset;

	private Status status; // null

	public User() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screen_name;
	}

	public void setScreenName(String screen_name) {
		this.screen_name = screen_name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDesription() {
		return desription;
	}

	public void setDesription(String desription) {
		this.desription = desription;
	}

	public String getProfileImageUrl() {
		return profile_image_url;
	}

	public void setProfileImageUrl(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	public int getFriendsCount() {
		return friends_count;
	}

	public void setFriendsCount(int friends_count) {
		this.friends_count = friends_count;
	}

	public int getFollowersCount() {
		return followers_count;
	}

	public void setFollowersCount(int followers_count) {
		this.followers_count = followers_count;
	}

	public int getFavouritesCount() {
		return favourites_count;
	}

	public void setFavouritesCount(int favourites_count) {
		this.favourites_count = favourites_count;
	}

	public Date getCreatedAt() {
		return created_at;
	}

	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}

	public boolean isFollowing() {
		return following;
	}

	public void setFollowing(boolean following) {
		this.following = following;
	}

	public boolean isNotifications() {
		return notifications;
	}

	public void setNotifications(boolean notifications) {
		this.notifications = notifications;
	}

	public int getUtcOffset() {
		return utc_offset;
	}

	public void setUtcOffset(int utc_offset) {
		this.utc_offset = utc_offset;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", screen_name="
				+ screen_name + ", location=" + location + ", desription="
				+ desription + ", profile_image_url=" + profile_image_url
				+ ", url=" + url + ", isProtected=" + isProtected
				+ ", friends_count=" + friends_count + ", followers_count="
				+ followers_count + ", favourites_count=" + favourites_count
				+ ", created_at=" + created_at + ", following=" + following
				+ ", notifications=" + notifications + ", utc_offset="
				+ utc_offset + ", status=" + status + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
