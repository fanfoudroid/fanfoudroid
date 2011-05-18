package com.ch_linghu.fanfoudroid.debug;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class ProfileFileWriter extends BaseWriter {
    private String mFile;
    private String mContent;
    
    public ProfileFileWriter() {
    }

    public ProfileFileWriter(String file, String content) {
        setFile(file);
        setContent(content);
    }
    
    public void setFile(String file) {
        mFile = file;
    }
    
    public void setContent(String content) {
        mContent = content;
    }

    public void write(String content) {
        setContent(content);
        try {
            write();
        } catch (ProfileWriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public String formatContent(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append(new Date());
        sb.append("\n---------------------------------\n");
        sb.append(content);
        sb.append("\n");
        return sb.toString();
    }

    public void write() throws ProfileWriterException {
        if (null == mFile) {
            throw new ProfileWriterException("file is null");
        }
        if (null == mContent) {
            throw new ProfileWriterException("content is null");
        }
        
        String content = formatContent(mContent);
        
        OutputStream os = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            os = new FileOutputStream(mFile, true);
            byte[] buffer = new byte[1024];
            int l;
            while ((l = is.read(buffer)) != -1)
            {
                os.write(buffer, 0, l);
            }
        } catch (IOException e) {
            throw new ProfileWriterException(
                    "Cann't write content to the file.", e);
        } finally {
            try {
                os.flush();
                os.close();
                is.close();
            } catch (IOException ioe) {
                throw new ProfileWriterException("Cann't close file.", ioe);
            }
        }
    }
}