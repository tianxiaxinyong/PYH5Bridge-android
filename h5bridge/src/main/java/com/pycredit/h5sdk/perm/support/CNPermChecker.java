package com.pycredit.h5sdk.perm.support;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

/**
 * @author huangx
 * @date 2018/1/24
 */

public class CNPermChecker {
    private static final String TAG = "CNPermChecker";
    private static final String TAG_NUMBER = "1";
    private static boolean granted = false;

    /**
     * ensure whether permission granted
     *
     * @param context
     * @param permission
     * @return true if granted else denied
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        try {
            switch (permission) {
                case Manifest.permission.READ_CONTACTS:
                    return checkReadContacts(context);
                case Manifest.permission.WRITE_CONTACTS:
                    return checkWriteContacts(context);
                case Manifest.permission.GET_ACCOUNTS:
                    return true;

                case Manifest.permission.READ_CALL_LOG:
                    return checkReadCallLog(context);
                case Manifest.permission.READ_PHONE_STATE:
                    return true;
                case Manifest.permission.CALL_PHONE:
                    return true;
                case Manifest.permission.WRITE_CALL_LOG:
                    return checkWriteCallLog(context);
                case Manifest.permission.USE_SIP:
                    return true;
                case Manifest.permission.PROCESS_OUTGOING_CALLS:
                    return true;
                case Manifest.permission.ADD_VOICEMAIL:
                    return true;

                case Manifest.permission.READ_CALENDAR:
                    return checkReadCalendar(context);
                case Manifest.permission.WRITE_CALENDAR:
                    return true;

                case Manifest.permission.BODY_SENSORS:
                    return checkBodySensors(context);

                case Manifest.permission.CAMERA:
                    return checkCamera(context);

                case Manifest.permission.ACCESS_COARSE_LOCATION:
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    return true;

                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    return checkReadStorage(context);
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    return checkWriteStorage(context);

                case Manifest.permission.RECORD_AUDIO:
                    return checkRecordAudio(context);

                case Manifest.permission.READ_SMS:
                    return checkReadSms(context);
                case Manifest.permission.SEND_SMS:
                case Manifest.permission.RECEIVE_WAP_PUSH:
                case Manifest.permission.RECEIVE_MMS:
                case Manifest.permission.RECEIVE_SMS:
                    return true;
                default:
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "throwing exception in PermissionChecker:  ", e);
            return false;
        }
    }

    /**
     * {@link android.Manifest.permission#CAMERA}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkCamera(Context context) throws Exception {
        boolean canUse = true;
        Camera camera = null;
        try {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            camera.setParameters(parameters);
        } catch (Exception e) {
            canUse = false;
            e.printStackTrace();
        }
        if (camera != null) {
            camera.release();
        }
        return canUse;
    }

    /**
     * record audio, {@link android.Manifest.permission#RECORD_AUDIO},
     * it will consume some resource!!
     *
     * @param context
     * @return true if success
     */
    private static boolean checkRecordAudio(Context context) throws Exception {
        AudioRecordManager recordManager = new AudioRecordManager();

        recordManager.startRecord(context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES) + "/" +
                TAG + ".3gp");
        recordManager.stopRecord();

        return recordManager.getSuccess();
    }

    /**
     * read calendar, {@link android.Manifest.permission#READ_CALENDAR}
     *
     * @param context
     * @return true if success
     */
    private static boolean checkReadCalendar(Context context) throws Exception {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://com" +
                ".android.calendar/calendars"), null, null, null, null);
        if (cursor != null) {
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * write or delete call log, {@link android.Manifest.permission#WRITE_CALL_LOG}
     *
     * @param context
     * @return true if success
     */
    private static boolean checkWriteCallLog(Context context) throws Exception {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues content = new ContentValues();
        content.put(CallLog.Calls.TYPE, CallLog.Calls.INCOMING_TYPE);
        content.put(CallLog.Calls.NUMBER, TAG_NUMBER);
        content.put(CallLog.Calls.DATE, 20140808);
        content.put(CallLog.Calls.NEW, "0");
        contentResolver.insert(Uri.parse("content://call_log/calls"), content);

        contentResolver.delete(Uri.parse("content://call_log/calls"), "number = ?", new
                String[]{TAG_NUMBER});

        return true;
    }

    /**
     * read sms, {@link android.Manifest.permission#READ_SMS}
     * in MEIZU 5.0~6.0, just according normal phone request
     * in XIAOMI 6.0~, need force judge
     * in XIAOMI 5.0~6.0, not test!!!
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkReadSms(Context context) throws Exception {
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/"), null, null,
                null, null);
        if (cursor != null) {
            if (ManufacturerSupportUtil.isForceManufacturer()) {
                if (isNumberIndexInfoIsNull(cursor, cursor.getColumnIndex(Telephony.Sms.DATE))) {
                    cursor.close();
                    return false;
                }
            }
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * write storage, {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkWriteStorage(Context context) throws Exception {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath(), TAG);
        if (!file.exists()) {
            boolean newFile;
            try {
                newFile = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return newFile;
        } else {
            return file.delete();
        }
    }

    /**
     * read storage, {@link android.Manifest.permission#READ_EXTERNAL_STORAGE}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkReadStorage(Context context) throws Exception {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath());
        File[] files = file.listFiles();
        return files != null;
    }

    /**
     * use location, {@link android.Manifest.permission#ACCESS_FINE_LOCATION},
     * {@link android.Manifest.permission#ACCESS_COARSE_LOCATION}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkLocation(Context context) throws Exception {
        granted = false;
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context
                .LOCATION_SERVICE);
        List<String> list = locationManager.getProviders(true);

        if (list.contains(LocationManager.GPS_PROVIDER)) {
            return true;
        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            return true;
        } else {
            if (!locationManager.isProviderEnabled("gps")) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, new
                        LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                locationManager.removeUpdates(this);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                locationManager.removeUpdates(this);
                                granted = true;
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                                locationManager.removeUpdates(this);
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                locationManager.removeUpdates(this);
                            }
                        });
            }
            return granted;
        }
    }

    /**
     * use sensors, {@link android.Manifest.permission#BODY_SENSORS}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkBodySensors(Context context) throws Exception {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.unregisterListener(listener, sensor);

        return true;
    }

    /**
     * read phone state, {@link android.Manifest.permission#READ_PHONE_STATE}
     * <p>
     * in {@link com.pycredit.h5sdk.perm.support.manufacturer.XIAOMI} or
     * {@link com.pycredit.h5sdk.perm.support.manufacturer.OPPO}          :
     * -> {@link TelephonyManager#getDeviceId()} will be null if deny permission
     * <p>
     * in {@link com.pycredit.h5sdk.perm.support.manufacturer.MEIZU}      :
     * -> {@link TelephonyManager#getSubscriberId()} will be null if deny permission
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    @SuppressLint("HardwareIds")
    private static boolean checkReadPhoneState(Context context) throws Exception {
        TelephonyManager service = (TelephonyManager) context.getSystemService
                (TELEPHONY_SERVICE);
        if (PermissionsPageManager.isMEIZU()) {
            return !TextUtils.isEmpty(service.getSubscriberId());
        } else if (PermissionsPageManager.isXIAOMI() || PermissionsPageManager.isOPPO()) {
            return !TextUtils.isEmpty(service.getDeviceId());
        } else {
            return !TextUtils.isEmpty(service.getDeviceId()) || !TextUtils.isEmpty(service
                    .getSubscriberId());
        }
    }

    /**
     * read call log, {@link android.Manifest.permission#READ_CALL_LOG}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkReadCallLog(Context context) throws Exception {
        Cursor cursor = context.getContentResolver().query(Uri.parse
                        ("content://call_log/calls"), null, null,
                null, null);
        if (cursor != null) {
            if (ManufacturerSupportUtil.isForceManufacturer()) {
                if (isNumberIndexInfoIsNull(cursor, cursor.getColumnIndex(CallLog.Calls.NUMBER))) {
                    cursor.close();
                    return false;
                }
            }
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * write and delete contacts info, {@link android.Manifest.permission#WRITE_CONTACTS}
     * and we should get read contacts permission first.
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkWriteContacts(Context context) throws Exception {
        if (checkReadContacts(context)) {
            // write some info
            ContentValues values = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            Uri rawContactUri = contentResolver.insert(ContactsContract.RawContacts
                    .CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);
            values.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds
                    .StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, TAG);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, TAG_NUMBER);
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);

            // delete info
            Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID},
                    "display_name=?", new String[]{TAG}, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(0);
                    resolver.delete(uri, "display_name=?", new String[]{TAG});
                    uri = Uri.parse("content://com.android.contacts/data");
                    resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
                }
                cursor.close();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * read contacts, {@link android.Manifest.permission#READ_CONTACTS}
     *
     * @param context
     * @return true if success
     * @throws Exception
     */
    private static boolean checkReadContacts(Context context) throws Exception {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone
                .CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (ManufacturerSupportUtil.isForceManufacturer()) {
                if (isNumberIndexInfoIsNull(cursor, cursor.getColumnIndex(ContactsContract.CommonDataKinds
                        .Phone.NUMBER))) {
                    cursor.close();
                    return false;
                }
            }
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * in {@link com.pycredit.h5sdk.perm.support.manufacturer.XIAOMI}
     * 1.denied {@link android.Manifest.permission#READ_CONTACTS} permission
     * ---->cursor.getCount == 0
     * 2.granted {@link android.Manifest.permission#READ_CONTACTS} permission
     * ---->cursor.getCount return real count in contacts
     * <p>
     * so when there are no user or permission denied, it will return 0
     *
     * @param cursor
     * @param numberIndex
     * @return true if can not get info
     */
    private static boolean isNumberIndexInfoIsNull(Cursor cursor, int numberIndex) {
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                return TextUtils.isEmpty(cursor.getString(numberIndex));
            }
            return false;
        } else {
            return true;
        }
    }
}
