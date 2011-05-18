package com.ch_linghu.fanfoudroid.debug;

public class PlainTextFormater implements ProfileFormater {
    
    public PlainTextFormater() {}
    public PlainTextFormater(String format) {}
    
    public String format(DebugProfile profile) {
        StringBuilder sb = new StringBuilder();
        for (DebugProfile.Record record : profile.getRecords()) {
            sb.append("TAG: ");
            sb.append(record.tag);
            sb.append("\t INC: ");
            sb.append(record.inc);
            sb.append("\t INCP: ");
            sb.append(record.incPercent);
            sb.append("\n");
        }
        return sb.toString();
    }
}