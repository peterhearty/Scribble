package uk.org.platitudes.scribble;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;

/**
 */
public class BackupToGoogle extends BackupAgentHelper {
    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this, MoreButtonHandler.DATAFILE);
        addHelper("google_backup_key", helper);
    }
}
