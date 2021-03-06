package com.abubakar.friendstracker.View;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.abubakar.friendstracker.Controller.ManageMeeting;
import com.abubakar.friendstracker.Controller.MeetingNotificationReceiver;
import com.abubakar.friendstracker.Model.DatabaseHelper;
import com.abubakar.friendstracker.Model.Meeting;
import com.abubakar.friendstracker.Model.MeetingData;
import com.abubakar.friendstracker.R;

import java.util.Calendar;
import java.util.Date;

public class MeetingActivity extends AppCompatActivity {

    private static final String TAG = "MeetingActivity";
    private DatabaseHelper myDB;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private MeetingListAdapter adapter;
    private BottomNavigationView navigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_contacts:
                    finish();
                    return true;
                case R.id.navigation_meetings:
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        //Database
        myDB = new DatabaseHelper(this);
        //Preferences
        preferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor = preferences.edit();
        //Link UI
        Button addNewMeeting = (Button) findViewById(R.id.addNewMeetingBtn);
        navigation = (BottomNavigationView) findViewById(R.id.navigation_meetings);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_meetings);
        ListView list = (ListView) findViewById(R.id.meetingListView);
        adapter = new MeetingListAdapter(getApplicationContext(), MeetingData.getInstance().getMeetingArrayList());
        list.setAdapter(adapter);

        // Button Testing toast
        addNewMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddMeetingActivity.class);
                startActivity(intent);
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MeetingActivity.this);
                mBuilder.setTitle("Manage Meetings");
                final int positionToRemove = i;
                ManageMeeting.getInstance().populateMeetingDialog(positionToRemove,mBuilder);
                mBuilder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ManageMeeting.getInstance().editMeeting(getApplicationContext(),positionToRemove,MeetingActivity.this,dialogInterface);
                    }
                });
                mBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MeetingData.getInstance().getMeetingArrayList().remove(positionToRemove);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(),"Meeting Deleted", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();
            }
        });
    }

    public void generateMeetingNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int requestCode = 1;
        for (Meeting meeting : MeetingData.getInstance().getMeetingArrayList()) {
            Intent intent = new Intent(getApplicationContext(), MeetingNotificationReceiver.class);
            intent.putExtra("requestCode", requestCode);
            intent.putExtra("meetingId", meeting.getMeetingID());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Date startTime = meeting.getStartTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            calendar.add(Calendar.MINUTE, -9);
            calendar.set(Calendar.SECOND, 1);
            Log.d(TAG, "generateMeetingNotifications: ===============" + calendar.getTime().toString());
            long ONE_MINUTE_IN_MILLIS = 60000;
            if (System.currentTimeMillis() > startTime.getTime() - (ONE_MINUTE_IN_MILLIS * 9)) {
                Date date = new Date();
                date.setTime(System.currentTimeMillis());
                Log.d(TAG, "generateMeetingNotifications: " + date.toString() + "         " + System.currentTimeMillis());
                Log.d(TAG, "generateMeetingNotifications: " + startTime.toString() + "       " + startTime.getTime());
                continue;
            }
            Date date = new Date();
            date.setTime(System.currentTimeMillis());
            Log.d(TAG, "generateMeetingNotifications: " + date.toString() + "         " + System.currentTimeMillis());
            Log.d(TAG, "generateMeetingNotifications: " + startTime.toString() + "       " + startTime.getTime());
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d(TAG, "generateMeetingNotifications: " + requestCode);
            requestCode++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.meeting_page, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_sortDateDescending:
                MeetingData.getInstance().sortMeetingsByDateDesc();
                adapter.notifyDataSetChanged();
                return true;
            case R.id.btn_sortDateAscending:
                MeetingData.getInstance().sortMeetingsByDateAscending();
                adapter.notifyDataSetChanged();
                return true;
            case R.id.action_snooze:
                getSnoozeTimerDialog();
                return true;
            case R.id.action_location:
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_meetings);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MeetingData.getInstance().saveMeetingDatabase(myDB);
        generateMeetingNotifications();
    }

    public void getSnoozeTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon_snooze_o);
        builder.setTitle("Please choose snooze duration");
        builder.setItems(R.array.minutes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.putInt("snoozeTime", i + 1);
                editor.apply();
                Toast.makeText(getApplicationContext(), "Snoozed duration changed to " + (i + 1) + " minutes", Toast.LENGTH_LONG).show();
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void startPlacePickerActivity() {

    }
}
