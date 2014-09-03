package com.faltenreich.diaguard.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.database.DatabaseDataSource;
import com.faltenreich.diaguard.helpers.FileHelper;
import com.faltenreich.diaguard.helpers.Helper;
import com.faltenreich.diaguard.helpers.PreferenceHelper;
import com.faltenreich.diaguard.helpers.ViewHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Filip on 04.11.13.
 */
public class BackupPreference extends DialogPreference {

    private final int ACTION_CREATEBACKUP = 0;
    private final int ACTION_RESTOREBACKUP = 1;

    DatabaseDataSource dataSource;
    PreferenceHelper preferenceHelper;
    Activity activity;

    public BackupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;

        dataSource = new DatabaseDataSource(activity);
        preferenceHelper = new PreferenceHelper(activity);

        // Hide standard buttons
        setPositiveButtonText(null);
        setNegativeButtonText(null);
    }

    @Override
    protected void onPrepareDialogBuilder(android.app.AlertDialog.Builder builder) {
        builder.setTitle(activity.getResources().getString(R.string.backup))
                .setItems(R.array.backup_actions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case ACTION_CREATEBACKUP:
                                createBackup();
                                String path = FileHelper.PATH_EXTERNAL + "/backup" +
                                        DateTimeFormat.forPattern("yyyyMMddHHmmss").print(new DateTime()) + ".csv";
                                ViewHelper.showInfo(activity,
                                        activity.getResources().getString(R.string.pref_data_backup_finished) + ": " + path);
                                break;
                            case ACTION_RESTOREBACKUP:
                                dialog.dismiss();
                                showBackups();
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
    }

    private void showBackups() {
        File path = FileHelper.getExternalStorage();
        File[] files = path.listFiles();
        List<String> csvFiles = new ArrayList<String>();
        for (File file : files) {
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
            if(extension.equals("csv"))
                csvFiles.add(fileName);
        }

        if(csvFiles.size() <= 0) {
            Activity preferenceActivity = (Activity)getContext();
            ViewHelper.showAlert(preferenceActivity, preferenceActivity.getString(R.string.error_no_backups));
            return;
        }

        final String[] csvArray = csvFiles.toArray(new String[csvFiles.size()]);
        final String[] csvArrayDates = new String[csvArray.length];
        for(int position = 0; position < csvArray.length; position++) {
            String dateString = csvArray[position].substring(6, csvArray[position].lastIndexOf("."));
            DateTime date = DateTimeFormat.forPattern("yyyyMMddHHmmss").parseDateTime(dateString);
            csvArrayDates[position] = preferenceHelper.getDateFormat().print(date) + " " +
                    Helper.getTimeFormat().print(date);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.backup_title)
                .setItems(csvArrayDates, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FileHelper fileHelper = new FileHelper(getContext());
                        fileHelper.importCSV(csvArray[which]);

                        ViewHelper.showInfo(activity,
                                activity.getResources().getString(R.string.pref_data_backup_import));
                    }
                }); 
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void createBackup() {
        FileHelper fileHelper = new FileHelper(getContext());
        fileHelper.exportCSV();
    }
}