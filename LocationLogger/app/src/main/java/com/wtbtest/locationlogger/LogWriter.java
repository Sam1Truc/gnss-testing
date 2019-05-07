package com.wtbtest.locationlogger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogWriter {
    private static final String TAG = "LogWriter";
    private static final String FILE_PREFIX = "gnss_log";
    private static final char RECORD_DELIMITER = ',';

    private final Object mFileLock = new Object();
    private File mFile;
    private Context mContext;


    public LogWriter(Context context, LogType logType) {
        if (isExternalStorageWritable()) {
            synchronized (mFileLock) {
                mContext = context;
                File baseDirectory = new File(Environment.getExternalStorageDirectory(), FILE_PREFIX);
                baseDirectory.mkdirs();

                File currentDirectory = new File(baseDirectory.getPath(), logType.toString());
                currentDirectory.mkdirs();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                Date now = new Date();
                String fileName = String.format("%s.txt", dateFormat.format(now));
                File currentFile = new File(currentDirectory, fileName);
                if (!currentFile.exists()) {
                    try {
                        currentFile.createNewFile();
                        Toast.makeText(context, "File created: " + currentFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                mFile = currentFile;
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeLog(String[] logs) {
        synchronized (mFileLock) {
            if (mFile != null) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(mFile, true);
                    OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);

                    StringBuilder logBuilder = new StringBuilder();

                    if (logs != null && logs.length > 0) {

                        for (String log : logs) {
                            logBuilder.append(log);
                            logBuilder.append(RECORD_DELIMITER);
                        }

                        writer.append(logBuilder.toString());
                        writer.close();
                        fileOutputStream.close();
                    }
                } catch(FileNotFoundException e){
                    Log.e(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
}
