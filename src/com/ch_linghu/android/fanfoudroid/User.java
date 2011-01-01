package com.ch_linghu.android.fanfoudroid;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

	// TODO: private 
	public String id = "";
	public String name = "";
	public String screenName = "";
	public String location = "";
	public String description = "";
	public String profileImageUrl = "";
	public String url = "";

	public static User create(JSONObject jsonObject) throws JSONException {
		User user = new User();

		user.id = jsonObject.getString("id");
		user.name = Utils.decodeTwitterJson(jsonObject.getString("name"));
		user.screenName = Utils.decodeTwitterJson(jsonObject
				.getString("screen_name"));
		user.location = Utils.decodeTwitterJson(jsonObject
				.getString("location"));
		user.description = Utils.decodeTwitterJson(jsonObject
				.getString("description"));
		user.profileImageUrl = jsonObject.getString("profile_image_url");
		user.url = jsonObject.getString("url");

		return user;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", screenName="
				+ screenName + ", location=" + location + ", description="
				+ description + ", profileImageUrl=" + profileImageUrl
				+ ", url=" + url + "]";
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
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
