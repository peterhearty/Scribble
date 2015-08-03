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
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class GoogleDriveFile extends File {

    private String mName;

    private GoogleDriveFolder mParentFolder;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mDriveId;
    private byte[] mFileContents;
    private DriveOutputStream pendingWrite;
    private boolean dummyFile;
    private AsyncRead readRequest;
    private CheckMetadata metadataCheck;
    private long lastModifiedDate;
    public boolean fileHasChanged;

    /**
     * Constructor for existing files.
     */
    public GoogleDriveFile(GoogleDriveFolder parent, Metadata m) {
        super("GoogleDriveFile:noname");
        mName = m.getTitle();
        mParentFolder = parent;
        mGoogleApiClient = mParentFolder.getmGoogleApiClient();
        mDriveId = m.getDriveId();
        lastModifiedDate = m.getModifiedDate().getTime();
        // We need all the files cached so that we can test for a Scribble file when a  file is being selected
        forceReRead();
        ScribbleMainActivity.mainActivity.getmGoogleStuff().checkFileLoadPending(this);
    }

    public void forceReRead() {
        if (readRequest == null) {
            readRequest = new AsyncRead(this, mGoogleApiClient, mDriveId);
        }
    }

    public void checkForChange () {
        metadataCheck = new CheckMetadata(this, mGoogleApiClient, mDriveId);
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
        DriveOutputStream dos = new DriveOutputStream(2048, mGoogleApiClient, this);
        return dos;
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

    public void createFileOnDrive () {
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

    public boolean isDummyFile() {return dummyFile;}
    public void setDummyFile(boolean dummyFile) {this.dummyFile = dummyFile;}
    public void setmFileContents(byte[] mFileContents) {
        this.mFileContents = mFileContents;
    }
    public String toString () {return mParentFolder.toString()+"/"+mName;}
    public boolean isDirectory () {return false;}
    public boolean canRead () {return true;}
    public File getParentFile() {return mParentFolder;}
    public long length() {
        if (mFileContents==null) return 0;
        return mFileContents.length;
    }
    public String getName() {return mName;}
    public AsyncRead getReadRequest() {return readRequest;}
    public void setReadRequest(AsyncRead readRequest) {this.readRequest = readRequest;}
    public byte[] getmFileContents() {return mFileContents;}
    public GoogleDriveFolder getmParentFolder() {return mParentFolder;}
    public void setmParentFolder(GoogleDriveFolder mParentFolder) {this.mParentFolder = mParentFolder;}
    public void setPendingWrite(DriveOutputStream pendingWrite) {this.pendingWrite = pendingWrite;}
    public DriveId getmDriveId() {return mDriveId;}
    public long getLastModifiedDate() {return lastModifiedDate;}
    public void setLastModifiedDate(long lastModifiedDate) {this.lastModifiedDate = lastModifiedDate;}



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
        mFileContents = f.mFileContents;
        mDriveId = f.mDriveId;
        mGoogleApiClient = f.mGoogleApiClient;
        return true;
    }

    @NonNull
    @Override
    public String getCanonicalPath() throws IOException {
        String s = mParentFolder.getCanonicalPath() + File.separator + mName;
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


}
