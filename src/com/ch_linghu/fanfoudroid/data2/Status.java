package com.ch_linghu.fanfoudroid.data2;

import java.util.Date;

public class Status {

    private String statusId;
    private User author;
    private String text;
    private String source;
    private Date createdAt;
    private boolean truncated;
    private boolean favorited;
    private Photo photo;
    private String inReplyToStatusId;
    private String inReplyToUserId;
    private String inReplyToScreenName;
    private User owner;
    private int type;
    
    public static int TYPE_PUBLIC = 1;
    public static int TYPE_FRIENDS = 2;
    public static int TYPE_USER = 3;
    public static int TYPE_FAVERITES = 4;
    public static int TYPE_PHOTO = 5;
    public static int TYPE_SEARCHRESULTS = 6;

    public Status() {
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(String inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    public void setInReplyToUserId(String inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public void setInReplyToScreenName(String inReplyToScreenName) {
        this.inReplyToScreenName = inReplyToScreenName;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Status other = (Status) obj;
        if (statusId == null) {
            if (other.statusId != null)
                return false;
        } else if (!statusId.equals(other.statusId))
            return false;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (createdAt == null) {
            if (other.createdAt != null)
                return false;
        } else if (!createdAt.equals(other.createdAt))
            return false;
        if (truncated != other.truncated)
            return false;
        if (favorited != other.favorited)
            return false;
        if (photo == null) {
            if (other.photo != null)
                return false;
        } else if (!photo.equals(other.photo))
            return false;
        if (inReplyToStatusId == null) {
            if (other.inReplyToStatusId != null)
                return false;
        } else if (!inReplyToStatusId.equals(other.inReplyToStatusId))
            return false;
        if (inReplyToUserId == null) {
            if (other.inReplyToUserId != null)
                return false;
        } else if (!inReplyToUserId.equals(other.inReplyToUserId))
            return false;
        if (inReplyToScreenName == null) {
            if (other.inReplyToScreenName != null)
                return false;
        } else if (!inReplyToScreenName.equals(other.inReplyToScreenName))
            return false;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Status [statusId=" + statusId + ", author=" + author.toString()
                + ", text=" + text + ", source=" + source + ", createdAt="
                + createdAt.toString() + ", truncated=" + truncated
                + ", favorited=" + favorited + ", photo=" + photo.toString()
                + ", inReplyToStatusId=" + inReplyToStatusId
                + ", inReplyToUserId=" + inReplyToUserId
                + ", inReplyToScreenName=" + inReplyToScreenName + ", owner="
                + owner.toString() + ", type" + type + "]";
    }
}
