package com.temp.afan.data;

public class Photo implements java.io.Serializable {
    private static final long serialVersionUID = 8370731670836608535L;
    
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
