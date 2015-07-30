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
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class GoogleDriveFile extends File {

    private String mName;
    private GoogleDriveFolder mParentFolder;
    private long mSize;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mDriveId;
    private byte[] mFileContents;
    private DriveOutputStream pendingWrite;
    private boolean dummyFile;

    /**
     * Constructor for existing files.
     */
    public GoogleDriveFile(GoogleDriveFolder parent, Metadata m) {
        super("GoogleDriveFile:noname");
        mName = m.getTitle();
        mParentFolder = parent;
        mSize = m.getFileSize();
        mGoogleApiClient = mParentFolder.getmGoogleApiClient();
        mDriveId = m.getDriveId();
        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);

        ResultCallback<DriveApi.DriveContentsResult> callback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {

                Status s = driveContentsResult.getStatus();
                if (!s.isSuccess()) {
                    // error
                    return;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();

                int size = (int) mSize;
                mFileContents = new byte[size];
                try {
                    InputStream is = driveContents.getInputStream();
                    is.read(mFileContents, 0, size);
                    is.close();
                    driveContents.discard(mGoogleApiClient);
                } catch (Exception e) {
                    ScribbleMainActivity.log("GoogleDriveFile", "getInputStream", e);
                }

            }
        };

        driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(callback);
    }

    /**
     * Constructor for new file. The createFile flag allows the caller to specify
     * whether the file should actually be created on the Google Drive. Ssometimes we
     * just want a dummy file for testing.
     *
     * Caller should add the file to the parent's contents.
     */
    public GoogleDriveFile(GoogleDriveFolder parent, String name, boolean createFile) {
        super("GoogleDriveFile:noname");
        mName = name;
        mParentFolder = parent;
        mGoogleApiClient = mParentFolder.getmGoogleApiClient();


        if (createFile) {
            createFileOnDrive();
        } else {
            dummyFile = true;
        }
    }

    private void createFileOnDrive () {
        final ResultCallback<DriveFolder.DriveFileResult> writeCallback
                = new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(DriveFolder.DriveFileResult result) {
                Status s = result.getStatus();
                if (!s.isSuccess()) {
                    // error
                    return;
                }

                DriveFile driveFile = result.getDriveFile();
                mDriveId = driveFile.getDriveId();

                if (pendingWrite != null) {
                    pendingWrite.writeFile();
                    pendingWrite = null;
                }
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
    public File getParentFile() {return mParentFolder;}
    public long length() {return mSize;}
    public String getName() {return mName;}

    /**
     * Tests to see if this file is known to the parent folder.
     *
     * SIDE EFFECT - copies the file details and file contents.
     */
    public boolean exists () {
        GoogleDriveFile f = mParentFolder.getFile(mName);
        if (f == null)
            return false;

        // File exists, copy file details
        mSize = f.mSize;
        mFileContents = f.mFileContents;
        mDriveId = f.mDriveId;
        mGoogleApiClient = f.mGoogleApiClient;
        return true;
    }

    @NonNull
    @Override
    public String getCanonicalPath() throws IOException {
        String s = mParentFolder.getCanonicalPath() + mName;
        return s;
    }

    @Override
    public boolean delete() {
        if (mDriveId != null) {

            ResultCallback<Status> callback = new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (!status.isSuccess()) {
                        ScribbleMainActivity.log("Problem", "deleting "+mName, null);
                    }
                }
            };

            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
            PendingResult<Status> result = driveFile.delete(mGoogleApiClient);
            result.setResultCallback(callback);

            // We assume it's going to work
            mParentFolder.deleteFile(this);
            return true;

        }
        return false;
    }

    /**
     * We always rename to the same directory.
     */
    @Override
    public boolean renameTo(File newPath) {
        String newName = newPath.getName();
        if (mDriveId != null) {

            ResultCallback<DriveResource.MetadataResult> callback = new ResultCallback<DriveResource.MetadataResult>() {
                @Override
                public void onResult(DriveResource.MetadataResult metadataResult) {
                    Status status = metadataResult.getStatus();
                    if (!status.isSuccess()) {
                        ScribbleMainActivity.log("Problem", "renaming "+mName, null);
                    }
                }
            };

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(newName)
                    .build();

            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
            PendingResult<DriveResource.MetadataResult>  result = driveFile.updateMetadata(mGoogleApiClient, changeSet);
            result.setResultCallback(callback);

            // We assume it's going to work
            mName = newName;
            return true;

        }
        return false;
    }

    public InputStream getInputStream () {
        if (mFileContents == null) {
            // file contents have not been fetched.
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(mFileContents);
        return bais;
    }

    public OutputStream getOutputStream () {
        DriveOutputStream dos = new DriveOutputStream(2048);
        return dos;
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

            writeFile();
        }

        private void writeFile () {
            if (mDriveId == null) {
                // File has just been created. Callback in constructor hasn't been called yet.
                // do the write when the constructor call back has completed.
                if (dummyFile) {
                    // The file was created to test for existence but has now been written to.
                    // convert it into a real file.
                    createFileOnDrive();
                    mParentFolder.addFileToList(GoogleDriveFile.this);
                    dummyFile = false;
                }
                pendingWrite = this;
                return;
            }

            // File already exists and we are overwriting
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

            DriveContents driveContents = driveContentsResult.getDriveContents();
            OutputStream os = driveContents.getOutputStream();
            try {
                os.write(mFileContents, 0, mFileContents.length);
            } catch (Exception e) {
                ScribbleMainActivity.log("DriveOutputStream", "onResult", e);
            }
            driveContents.commit(mGoogleApiClient, null);

        }
    }
}
