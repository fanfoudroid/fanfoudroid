package com.ch_linghu.fanfoudroid.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

	public String id;
	public String name;
	public String screenName;
	public String location;
	public String description;
	public String profileImageUrl;
	public String url;
	public boolean isProtected;
	public int followersCount;
	public String lastStatus;

	public int friendsCount;
	public int favoritesCount;
	public int statusesCount;
	public Date createdAt;
	public boolean isFollowing;

	// public boolean notifications;
	// public utc_offset
	
	public User() {}
	
	public static User create(com.ch_linghu.fanfoudroid.fanfou.User u) {
		User user = new User();

		user.id = u.getId();
		user.name = u.getName();
		user.screenName = u.getScreenName();
		user.location = u.getLocation();
		user.description = u.getDescription();
		user.profileImageUrl = u.getProfileImageURL().toString();
		if (u.getURL() != null) {
			user.url = u.getURL().toString();
		}
		user.isProtected = u.isProtected();
		user.followersCount = u.getFollowersCount();
		user.lastStatus = u.getStatusText();

		user.friendsCount = u.getFriendsCount();
		user.favoritesCount = u.getFavouritesCount();
		user.statusesCount = u.getStatusesCount();
		user.createdAt = u.getCreatedAt();
		user.isFollowing = u.isFollowing();

		return user;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		boolean[] boolArray = new boolean[] { isProtected, isFollowing };
		out.writeString(id);
		out.writeString(name);
		out.writeString(screenName);
		out.writeString(location);
		out.writeString(description);
		out.writeString(profileImageUrl);
		out.writeString(url);
		out.writeBooleanArray(boolArray);
		out.writeInt(friendsCount);
		out.writeInt(followersCount);
		out.writeInt(statusesCount);
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			// return new User[size];
			throw new UnsupportedOperationException();
		}
	};
	
	public User(Parcel in){
		boolean[] boolArray =  new boolean[]{isProtected, isFollowing};
		id = in.readString();
		name = in.readString();
		screenName = in.readString();
		location = in.readString();
		description = in.readString();
		profileImageUrl = in.readString();
		url = in.readString();
		in.readBooleanArray(boolArray);
		friendsCount = in.readInt();
		followersCount = in.readInt();
		statusesCount = in.readInt();
		
		isProtected = boolArray[0];
		isFollowing = boolArray[1];
	}
}
