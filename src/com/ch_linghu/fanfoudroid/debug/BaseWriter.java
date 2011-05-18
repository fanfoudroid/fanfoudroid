package com.ch_linghu.fanfoudroid.debug;

abstract public class BaseWriter implements ProfileWriter {
    private ProfileFormater mFormater;
    
    public void store(DebugProfile profile) throws ProfileWriterException {
        write(mFormater.format(profile));
    }
    
    public ProfileFormater getFormater() {
        return mFormater;
    }

    public void setFormater(ProfileFormater formater) {
        this.mFormater = formater;
    }
    
    public abstract void write(String content) throws ProfileWriterException;
}
