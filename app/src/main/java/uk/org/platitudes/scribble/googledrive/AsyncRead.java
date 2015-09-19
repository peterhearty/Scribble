/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class AsyncRead implements ResultCallback<DriveApi.DriveContentsResult> {

    private GoogleDriveFile mFile;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mDriveId;
    private DriveFile mDriveFile;

    public AsyncRead (GoogleDriveFile gdf, GoogleApiClient apiClient, DriveId id) {
        mFile = gdf;
        mGoogleApiClient = apiClient;
        mDriveId = id;

        ScribbleMainActivity.log("AsyncRead", "read requested " + gdf.toString(), null);
        mDriveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
        mDriveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(this);
    }

    private void readContents (DriveContents driveContents) {
        int size = (int) mFile.length();
        byte[] contents = new byte[size];
        try {
            InputStream is = driveContents.getInputStream();
//            ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
//            FileDescriptor fd = parcelFileDescriptor.getFileDescriptor();
//            InputStream is = new FileInputStream(fd);
            int available = is.available();
            if (available != size) {
                if (available > size) {
                    contents = new byte[available];
                    size = available;
                }
            }
            is.read(contents, 0, size);
            is.close();
            driveContents.discard(mGoogleApiClient);

            // Check for contents change. Only modify Drawing if contents have changed.
            // This is to prevent the view abrubtly changing in the
            // middle of an extended move, pan, or zoom operation.
            byte[] currentContents = mFile.getmFileContents();
            boolean updateContents = false;
            if (currentContents == null) {
                updateContents = true;
                ScribbleMainActivity.log("AsyncRead", "File changed, contents previously empty "+mFile.toString(), null);
            } else {
                if (currentContents.length != contents.length) {
                    updateContents = true;
                    ScribbleMainActivity.log("AsyncRead", "File changed, length has changed "+mFile.toString(), null);
                } else {
                    // old and new same length, check contents
                    // Scribble files have a changeByte near the start that changes on each write.
                    // This should prevent the need for comparing the full contents every time
                    // for a change.
                    for (int i=0; i<contents.length; i++) {
                        if (currentContents[i] != contents[i]) {
                            // contents have changed
                            ScribbleMainActivity.log("AsyncRead", "File changed at byte "+i+" "+mFile.toString(), null);
                            updateContents = true;
                            break;
                        }
                        if (i > 100) {
                            // The file header consists of:
                            // MAGIC_NUMBER long    8 bytes (15 + n/l .txt)
                            // version      int     4 bytes ( 4 + n/l .txt)
                            // changeByte   byte    1 byte  ( 3 + n/l .txt)
                            // draw count   int     4 bytes (4? + n/l .txt)
                            // After about 30 bytes unchanged there should be no more changes
                            break;
                        }
                    }
                }
            }

            if (updateContents) {
                mFile.setmFileContents(contents);
                mFile.fileHasChanged = true;
            } else {
                ScribbleMainActivity.log("AsyncRead", "file has not changed "+mFile.toString(), null);
            }

            mFile.setReadRequest(null);
        } catch (Exception e) {
            ScribbleMainActivity.log("GoogleDriveFile", "getInputStream", e);
        }

    }

    @Override
    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
        Status s = driveContentsResult.getStatus();
        ScribbleMainActivity.log("AsyncRead", "read result "+mFile.toString()+" status = "+s.toString(), null);
        if (!s.isSuccess()) {
            // error
            mFile.setReadRequest(null);
            return;
        }

        DriveContents  driveContents = driveContentsResult.getDriveContents();
        readContents(driveContents);
    }

}
