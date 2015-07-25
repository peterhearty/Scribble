package uk.org.platitudes.scribble;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

import java.io.File;

import uk.org.platitudes.scribble.buttonhandler.MoreButtonHandler;
import uk.org.platitudes.scribble.file.FileSaver;

/**
 */
public class BackupToGoogle extends BackupAgentHelper {
    @Override
    public void onCreate() {
        File f = ScribbleMainActivity.mainActivity.getApplicationContext().getFilesDir();
        try {
            String path = f.getCanonicalPath()+File.separator+ FileSaver.DATAFILE;
            FileBackupHelper helper = new FileBackupHelper(this, path);
            addHelper("google_backup_key", helper);
        } catch (Exception e) {
            ScribbleMainActivity.log("BackupToGoogle", "file path", e);
        }
    }
}
