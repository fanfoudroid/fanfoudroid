package com.ch_linghu.fanfoudroid.data2;

public class Photo {
    private String thumburl;
    private String imageurl;
    private String largeurl;
    
    public Photo() {}

    public String getThumburl() {
        return thumburl;
    }

    public void setThumburl(String thumburl) {
        this.thumburl = thumburl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getLargeurl() {
        return largeurl;
    }

    public void setLargeurl(String largeurl) {
        this.largeurl = largeurl;
    }

    @Override
    public String toString() {
        return "Photo [thumburl=" + thumburl + ", imageurl=" + imageurl
                + ", largeurl=" + largeurl + "]";
    }
}
