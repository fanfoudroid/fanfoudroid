package com.ch_linghu.fanfoudroid.debug;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugProfile  {
    private List<Record> mRecords = new ArrayList<Record>();
    
    public DebugProfile() {
    }
    
    public void writeRow(String tag, long inc, double incPercent) {
        mRecords.add(new Record(tag, inc, incPercent));
    }
    
    public List<Record> getRecords() {
        Collections.sort(mRecords);
        return mRecords;
    }
    
    public static class Record implements Comparable<Record> {
        private static NumberFormat percent = NumberFormat.getPercentInstance();
        
        public String tag;
        public long inc;
        public String incPercent;
        
        public Record(String tag, long inc, double incPercent) {
            this.tag = tag;
            this.inc = inc;
            
            percent = new DecimalFormat("0.00#%");
            this.incPercent = percent.format(incPercent);
        }
        
        @Override
        public int compareTo(Record o) {
            // TODO Auto-generated method stub
            return (int) (o.inc - this.inc);
        }
    }
}