/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
class DriveOutputStream extends ByteArrayOutputStream implements ResultCallback<DriveApi.DriveContentsResult> {

    private final GoogleDriveFile mGoogleDriveFile;
    private boolean closed;
    private GoogleApiClient mGoogleApiClient;

    public DriveOutputStream(int i, GoogleApiClient api, GoogleDriveFile file) {
        super (i);
        mGoogleApiClient = api;
        mGoogleDriveFile = file;
        closed = false;
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        byte[] contents = toByteArray();
        mGoogleDriveFile.setmFileContents(contents);
        super.close();
        closed = true;

        writeFile();
    }

    public void writeFile () {
        DriveId id = mGoogleDriveFile.getmDriveId();
        if (id == null) {
            // File has just been created. Callback in constructor hasn't been called yet.
            // do the write when the constructor call back has completed.
            if (mGoogleDriveFile.isDummyFile()) {
                // The file was created to test for existence but has now been written to.
                // convert it into a real file.
                mGoogleDriveFile.createFileOnDrive();
                mGoogleDriveFile.getmParentFolder().addFileToList(mGoogleDriveFile);
                mGoogleDriveFile.setDummyFile(false);
            }
            mGoogleDriveFile.setPendingWrite(this);
            return;
        }

        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, id);
        driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                .setResultCallback(this);
    }

    @Override
    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
        Status s = driveContentsResult.getStatus();
        if (!s.isSuccess()) {
            // error
            return;
        }

        DriveContents driveContents = driveContentsResult.getDriveContents();
        OutputStream os = driveContents.getOutputStream();
        try {
            byte[] contents = mGoogleDriveFile.getmFileContents();
            os.write(contents, 0, contents.length);
        } catch (Exception e) {
            ScribbleMainActivity.log("DriveOutputStream", "onResult", e);
        }
        driveContents.commit(mGoogleApiClient, null);

    }
}

