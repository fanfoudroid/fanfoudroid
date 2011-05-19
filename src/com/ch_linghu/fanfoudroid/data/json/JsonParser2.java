package com.ch_linghu.fanfoudroid.data.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ch_linghu.fanfoudroid.data2.Photo;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.User;
import com.ch_linghu.fanfoudroid.data2.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

/**
 * @deprecated use JsonParser
 */
public class JsonParser2 {
    private static final String TAG = "JsonParser";
    private GsonBuilder mBuilder; // static ?

    public JsonParser2() {
        mBuilder = new GsonBuilder();
        // for status
        mBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
        mBuilder.registerTypeAdapter(User.class, new UserInstanceCreator());
        mBuilder.registerTypeAdapter(Photo.class, new PhotoInstanceCreator());
    }

    public List<Status> parseToStatuses(InputStream is) throws JsonParserException {
        List<Status> statuses = new ArrayList<Status>();
        JsonReader reader = null;
        try {
            reader = getReader(is);
            Gson gson = mBuilder.create();
            reader.beginArray();
            while (reader.hasNext()) {
                statuses.add((Status) gson.fromJson(reader, Status.class));
            }
            reader.endArray();
        } catch (IOException ioe) {
            throw new JsonParserException(ioe.getMessage(), ioe);
        } finally {
            try {
                is.close();
                reader.close();
            } catch (IOException ioe) {
                throw new JsonParserException(ioe.getMessage(), ioe);
            }
        }
        return statuses;
    }

    private JsonReader getReader(InputStream is)
            throws UnsupportedEncodingException {
        return new JsonReader(new InputStreamReader(is, "UTF-8"));
    }
    
    class DateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type type,
                JsonDeserializationContext context) throws JsonParseException {
            try {
                return Utils.parseDate(json.getAsJsonPrimitive().getAsString(),
                        "EEE MMM dd HH:mm:ss z yyyy");
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }
    
    class UserInstanceCreator implements InstanceCreator<User> {
        public User createInstance(Type type) {
            return new User();
        }
    }

    class StatusInstanceCreator implements InstanceCreator<Status> {
        public Status createInstance(Type type) {
            return new Status();
        }
    }
    
    class PhotoInstanceCreator implements InstanceCreator<Photo> {
        public Photo createInstance(Type type) {
            return new Photo();
        }
    }
    
    ///////////////////// TEST ////////////////////////////////////////
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(new File("status.json"));

        JsonParser2 jsonParser = new JsonParser2();
        try {
            List<Status> statuses = jsonParser.parseToStatuses(fis);
            for (Status s : statuses) {
                System.out.println(s);
            }
        } catch (JsonParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}