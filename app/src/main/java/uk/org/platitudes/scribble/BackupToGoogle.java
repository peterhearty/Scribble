package uk.org.platitudes.scribble;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.FullBackupDataOutput;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;

import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.file.FileSaver;

/**
 */
public class BackupToGoogle extends BackupAgentHelper {
    @Override
    public void onCreate() {
        try {
            synchronized (FileSaver.sDataLock) {
                FileBackupHelper helper = new FileBackupHelper(this, FileSaver.DATAFILE);
                addHelper("google_backup_key", helper);
                ScribbleMainActivity.log("Backup agent created", "", null);
            }
        } catch (Exception e) {
            ScribbleMainActivity.log("BackupToGoogle", "file path", e);
        }
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs backup
        synchronized (FileSaver.sDataLock) {
            ScribbleMainActivity.log("Backup started", "", null);
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file
        synchronized (FileSaver.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }

    @Override
    public void onFullBackup(FullBackupDataOutput data) throws IOException {
        super.onFullBackup(data);
    }

    @Override
    public void onRestoreFile(ParcelFileDescriptor data, long size, File destination, int type, long mode, long mtime) throws IOException {
        super.onRestoreFile(data, size, destination, type, mode, mtime);
    }


}
