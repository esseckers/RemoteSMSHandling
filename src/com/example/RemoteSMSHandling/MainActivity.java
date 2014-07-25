package com.example.RemoteSMSHandling;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {

    EditText txtPhoneNo;
    EditText txtMessage;
    Button date;
    Button time;

    private DateModel dateModel;

    private static String emptyField = "Please fill in all required fields";
    private static String incorrectTime = "You can't send sms in past";

    private PendingIntent pendingIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dateModel = new DateModel();

        txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        date = (Button) findViewById(R.id.calendar);
        time = (Button) findViewById(R.id.time);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Calendar currentTime = Calendar.getInstance();
                final int hour = dateModel.getHour() == 0 ? currentTime.get(Calendar.HOUR_OF_DAY) : dateModel.getHour();
                final int minute = dateModel.getMin() == 0 ? currentTime.get(Calendar.MINUTE) : dateModel.getMin();
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        if (selectedHour < hour || selectedMinute < minute) {
                            showDialogError(incorrectTime);
                        } else {
                            dateModel.setHour(selectedHour);
                            dateModel.setMin(selectedMinute);
                            ((Button) view).setText(selectedHour + ":" + (selectedMinute <= 9 ? "0" + selectedMinute : selectedMinute));
                        }
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");

                mTimePicker.show();
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Calendar currentTime = Calendar.getInstance();
                int year = dateModel.getYear() == 0 ? currentTime.get(Calendar.YEAR) : dateModel.getYear();
                int month = dateModel.getMonth() == 0 ? currentTime.get(Calendar.MONTH) : dateModel.getMonth();
                int day = dateModel.getYear() == 0 ? currentTime.get(Calendar.DAY_OF_MONTH) : dateModel.getDay();

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, day);
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String date = df.format(new Date(c.getTimeInMillis()));
                        ((Button) view).setText(date);
                        dateModel.setYear(year);
                        dateModel.setMonth(month);
                        dateModel.setDay(day);
                    }
                }, year, month, day);
                datePickerDialog.setCanceledOnTouchOutside(true);
                datePickerDialog.setTitle("Select Date");
                if (datePickerDialog.getDatePicker() != null) {
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    datePickerDialog.getDatePicker().setCalendarViewShown(false);
                }
                datePickerDialog.show();
            }
        });


        findViewById(R.id.btnSendSMS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields(txtPhoneNo, txtMessage, date, time)) {
                    startCheck();
                } else {
                    showDialogError(emptyField);
                }

            }
        });


    } //end onCreate

    private Intent createNewIntent(long date) {
        Intent intent = new Intent(this, AlarmService.class);
        intent.putExtra("time", date);
        return intent;
    }

    private long setDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, dateModel.getMonth());
        calendar.set(Calendar.YEAR, dateModel.getYear());
        calendar.set(Calendar.DAY_OF_MONTH, dateModel.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, dateModel.getHour());
        calendar.set(Calendar.MINUTE, dateModel.getMin());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.PM);

        return calendar.getTimeInMillis();
    }

    public void startCheck() {
        startService(createNewIntent(setDate()));
    }

    private boolean validateFields(View... view) {
        boolean result = true;
        for (View text : view) {
            if (text instanceof EditText) {
                if (((EditText) text).getText().toString().isEmpty()) {
                    result = false;
                    break;
                }
            } else {
                if (((Button) text).getText().toString().isEmpty()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }


    private void showDialogError(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create();
        builder.show();
    }


//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
//        txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
//        txtMessage = (EditText) findViewById(R.id.txtMessage);
//
//        btnSendSMS.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                String phoneNo = txtPhoneNo.getText().toString();
////                String message = txtMessage.getText().toString();
////                if (phoneNo.length() > 0 && message.length() > 0)
////                    sendSMSCustome(phoneNo, message);
////                else
////                    Toast.makeText(getBaseContext(),
////                            "Please enter both phone number and message.",
////                            Toast.LENGTH_SHORT).show();
//
//                TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                String getSimNumber = telemamanger.getLine1Number();
//                String getNameOperator = telemamanger.getSimOperatorName();
//                Toast.makeText(getBaseContext(),
//                        getSimNumber,
//                          Toast.LENGTH_LONG).show();
//               // printContactList();
//               startCheck();
//            }
//        });
//
////        Intent openNewAlarm = new Intent(AlarmClock.ACTION_SET_ALARM);
////        openNewAlarm.putExtra(AlarmClock.EXTRA_HOUR, 13);
////        openNewAlarm.putExtra(AlarmClock.EXTRA_MINUTES, 31);
////        startActivity(openNewAlarm);
//
//    }


    //---sends an SMS message to another device---
    private void sendSMSCustome(String phoneNumber, String message) {
        try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {

        }
    }

    private void printContactList() {
        Cursor cursor = getContacts();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Log.d("Display_Name", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
            Log.d("Contact_id", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)));
            Log.d("Account_Type", cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)));
            Log.d("number", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            cursor.moveToNext();
        }
        cursor.close();
    }

    /**
     * Obtains the contact list for the currently selected account.
     *
     * @return A cursor for for accessing the contact list.
     */
    private Cursor getContacts() {
        // Run query
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.RawContacts._ID,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.RawContacts.CONTACT_ID

        };
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return getContentResolver().query(uri, projection, null, selectionArgs, sortOrder);
    }

    private void initView(View view) {


    }
}
