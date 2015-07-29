/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.googledrive;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.file.FileSaver;

/**
 */
public class GoogleDriveFile extends File implements ResultCallback<DriveApi.DriveContentsResult> {

    private String mName;
    private GoogleDriveFolder mParentFolder;
    private Metadata metaData;
    private DriveContents mDriveContents;
    private long mSize;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mDriveId;
    private DriveFile mDriveFile;
    private byte[] mFileContents;
    private ByteArrayOutputStream mOutputStream;

    /**
     * Constructor for existing files.
     */
    public GoogleDriveFile(GoogleDriveFolder parent, Metadata m) {
        super("GoogleDriveFile:noname");
        mName = m.getTitle();
        metaData = m;
        mParentFolder = parent;
        mSize = m.getFileSize();
        mGoogleApiClient = mParentFolder.getmGoogleApiClient();
        mDriveId = m.getDriveId();
        mDriveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);

        mDriveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(this);
    }

    /**
     * Constructor for new file.
     */
    public GoogleDriveFile(GoogleDriveFolder parent, String name) {
        super("GoogleDriveFile:noname");
        mName = name;
        mParentFolder = parent;
        mGoogleApiClient = mParentFolder.getmGoogleApiClient();


        final ResultCallback<DriveFolder.DriveFileResult> writeCallback
                = new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(DriveFolder.DriveFileResult result) {
                Status s = result.getStatus();
                if (!s.isSuccess()) {
                    // error
                    return;
                }

                mDriveFile = result.getDriveFile();
                mDriveId = mDriveFile.getDriveId();
            }
        };

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(mName)
                .build();
        PendingResult<DriveFolder.DriveFileResult> pendingResult
                = mParentFolder.getmDriveFolder().createFile(mGoogleApiClient, changeSet, null);
        pendingResult.setResultCallback(writeCallback);

    }

    public boolean isDirectory () {return false;}
    public boolean canRead () {return true;}
    public boolean exists () {return true;}

    @Override
    public long length() {
        return mSize;
    }

    public InputStream getInputStream () {
        if (mFileContents == null && mDriveContents != null) {
            // file contents have not been fetched.
            int size = (int) mSize;
            mFileContents = new byte[size];
            try {
                InputStream is = mDriveContents.getInputStream();
                is.read(mFileContents, 0, size);
                is.close();
                mDriveContents.discard(mGoogleApiClient);
                mDriveContents = null;
            } catch (Exception e) {
                ScribbleMainActivity.log("GoogleDriveFile", "getInputStream", e);
            }
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(mFileContents);
        return bais;
    }

    public OutputStream getOutputStream () {
        mOutputStream = new DriveOutputStream(2048);
        if (mDriveContents != null) {
            mDriveContents.discard(mGoogleApiClient);
        }
        mDriveContents = null;
        return mOutputStream;
    }



    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
        Status s = driveContentsResult.getStatus();
        if (!s.isSuccess()) {
            // error
            return;
        }
        mDriveContents = driveContentsResult.getDriveContents();
    }

    class DriveOutputStream extends ByteArrayOutputStream implements ResultCallback<DriveApi.DriveContentsResult> {

        private boolean closed;

        public DriveOutputStream(int i) {
            super (i);
            closed = false;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            mFileContents = toByteArray();
            mSize = mFileContents.length;
            super.close();
            closed = true;
            // Initiate write to google drive
            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
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

            mDriveContents = driveContentsResult.getDriveContents();
            OutputStream os = mDriveContents.getOutputStream();
            try {
                os.write(mFileContents, 0, mFileContents.length);
            } catch (Exception e) {
                ScribbleMainActivity.log("DriveOutputStream", "onResult", e);
            }
            mDriveContents.commit(mGoogleApiClient, null);

        }
    }
}
