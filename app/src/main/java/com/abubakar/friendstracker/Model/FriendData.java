package com.abubakar.friendstracker.Model;

import android.database.Cursor;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FriendData {

    private static final String TAG = "FriendData";
    private static final FriendData ourInstance = new FriendData();
    private ArrayList<Friend> friendArrayList = new ArrayList<>();

    public static FriendData getInstance() {
        return ourInstance;
    }

    private FriendData() {
    }

    public ArrayList<Friend> getFriendArrayList() {
        return friendArrayList;
    }

    public void addNewFriend(Friend friend) {
        friendArrayList.add(friend);
    }

    public void populateFriendsList(DatabaseHelper db) {
        Cursor cursor = db.getAllFriendData();
        friendArrayList.clear();
        if (cursor.getCount() == 0) {
            Log.d(TAG, "populateFriendsList: NO DATA IN DB");
            return;
        }
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            String email = cursor.getString(2);
            String dob = cursor.getString(3);
            Date dateOfBirth = null;
            if (dob != null) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("MMM, dd, yyyy");
                    dateOfBirth = format.parse(dob);
                } catch (ParseException e) {
                    Log.d(TAG, "populateFriendsList: DOB FROM DB = " + dob);
                    e.printStackTrace();
                    dateOfBirth = null;
                }
            }
            Double lat = null;
            Double lon = null;
            if (cursor.getString(4) != null && cursor.getString(5) != null) {
                lat = Double.valueOf(cursor.getString(4));
                lon = Double.valueOf(cursor.getString(5));
            }
            String timestamp = cursor.getString(6);

            Friend friend = new Friend(id, name, email, dateOfBirth, lat, lon, timestamp);
            friendArrayList.add(friend);
        }
    }

    public void addSampleFriends() {
        friendArrayList.add(new Friend("Jane Doe1", "janedoe1@somedomain.com", new Date(1989 - 1900, 11, 1), -37.784220, 144.951847, null));
        friendArrayList.add(new Friend("Jane Doe2", "janedoe2@somedomain.com", new Date(1988 - 1900, 5, 1), -37.786191, 144.962983, null));
        friendArrayList.add(new Friend("John Doe1", "johndoe1@somedomain.com", new Date(1987 - 1900, 5, 1), -37.812579, 144.966054, null));
        friendArrayList.add(new Friend("John Doe2", "johndoe2@somedomain.com", new Date(1995 - 1900, 5, 1), -37.840403, 144.966835, null));
        friendArrayList.add(new Friend("Sansa Stark", "arya@email.com", new Date(1992 - 1900, 2, 12), -37.784220, 144.951847, null));
        friendArrayList.add(new Friend("Jamie Lannister", "jamie@email.com", new Date(1989 - 1900, 11, 1), -37.786191, 144.962983, null));

        Log.d(TAG, "addSampleFriends: ADDED");
    }

    public void saveFriendDatabase(DatabaseHelper db) {
        db.clearFriendsTable();
        for (Friend f : friendArrayList) {
            DateFormat dateFormat = new SimpleDateFormat("MMM, dd, yyyy");
            Date birthday = f.getBirthday();
            String dob = null;
            if (birthday != null) {
                dob = dateFormat.format(birthday);
            }
            db.insertFriendData(f.getID(), f.getName(), dob, f.getEmail(), f.getLat(), f.getLon(), f.getTimestamp());
        }
    }
    public Friend getFriendByID(String ID){
        for (Friend friend: friendArrayList){
            if (friend.getID().equals(ID)){
                return friend;
            }
        }
        return null;
    }
    public int getFriendListIndex(String ID){
        for (Friend friend: friendArrayList){
            if (friend.getID().equals(ID)){
                return friendArrayList.indexOf(friend);
            }
        }
        return -1;
    }
}
