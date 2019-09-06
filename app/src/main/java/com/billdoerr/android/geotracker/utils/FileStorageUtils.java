package com.billdoerr.android.geotracker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import com.billdoerr.android.geotracker.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *  File storage utilities.
 */
public class FileStorageUtils {

    private static final String TAG = "FileStorageUtils";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String TABS = "\t\t";

    private Context mContext;
    private static String mFilename;

    /**
     * Generate date/time stamp that will be used to for system log entries.
     * @return String:  date/time in format:  "dd MMM yyyy HH:mm:ss".
     */
    public static String getDateTime() {
        String dateFormat = "dd MMM yyyy HH:mm:ss";
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        return df.format(c.getTime());
    }

    /**
     *
     * @param context Context:  Application context.
     * @param data String:  Data to be written to file.
     * @param filename String: The name of the file to open; can not contain path separators.
     * @param mode int: File creation mode
     */
    public static void writeToFile(Context context, String data, String filename, int mode) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, mode));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, context.getString(R.string.exception_file_write_failed) + " " + e.toString());
        }
    }

    /**
     * Read data from file.
     * @param context Context:  Application context.
     * @param filename String: The name of the file to open; can not contain path separators.
     * @return String:  Contents of file being read.
     */
    public static String readFromFile(Context context, String filename) {

        final String lineSeparator = System.getProperty("line.separator");
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append(lineSeparator);
                }

                inputStream.close();
                ret = stringBuilder.toString();

            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, context.getString(R.string.exception_file_not_found) + " " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, context.getString(R.string.exception_cannot_read_file) + " " + e.toString());
        }

        return ret;
    }

    /**
     * Writes to the system log.  Each entry is preceded with a date/time stamp.
     * @param context Context:  Application context.
     * @param filename String:  System log filename.
     * @param entry String:  Data to be written to log file.
     */
    public static void writeSystemLog(Context context, String filename, String entry) {
        final int mode = Context.MODE_PRIVATE | Context.MODE_APPEND;
        String output = getDateTime() + TABS + entry + LINE_SEPARATOR + LINE_SEPARATOR;
        writeToFile(context, output, filename, mode);
    }

    /**
     * Read the system log.
     * @return String:  Contents of system log.
     */
    public static String readSystemLog(Context context, String filename) {
        return readFromFile(context, filename);
    }

    /**
     * Clears the contents of the system log.
     */
    public static void clearSystemLog(Context context, String filename) {
        final int mode = Context.MODE_PRIVATE;
        writeToFile(context, "", filename, mode);
    }

}

