/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.PointF;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Compresses Freehand drawings coordinates before saving them and expands them after reading them.
 *
 * FreehandDrawItem holds (x,y) coords as a pair of floating point numbers in a PointF. Each coord
 * takes up 2x4 = 8 bytes. The compressed data consists of a start value, followed by a series of
 * one byte differences from the previous value. The one byte difference is always held as a
 * signed number between 10 and 100 so that a full 2 digits of significance are stored.
 *
 * To get each one byte difference into the range (10,100), it will often have to be multipled by
 * a power of ten. The number of powers of ten is stored as 114 +/- 13. Byte values 101 to 127 are
 * reserved for this purpose. When an exponent byte is detected (a value in range [101,127]), 114 is
 * subtracted and this becomes the new exponent until another exponent byte is found.
 *
 * This algorithm is fairly lossy and errors could accumulate over difference values. To mitigate
 * against this it recalibrates after ever difference as follows.
 *
 * dx2 = x2 - x1  (dx2 is approximate)
 * c = x1 + dx2   (c is the value that will be calculated when values are reconstructed)
 *
 * dx3 = x3 - c   (dx3 is the difference needed to get from the previous CALCULATED value to the next real value).
 */
public class FreeCompressContext {

    /**
     * The current exponent. starts at 114 (10**0) and can take values from
     * 101 (10**-13) to 127 (10^13).
     */
    private byte mCurExponent;

    /**
     * The value at the start of the sequence that all differences are added to.
     */
    private float mStart;

    /**
     * To prevent the accumulation of rounding errors, each float value is recalcuated
     * from the previous value and the difference. The next value is then differenced from the
     * recalculated value.
     */
    private float mLastRecalculated;

    /**
     * Where the compressed data is held.
     */
    private byte[] mCompressedData;

    /**
     * The next free position in mCompressedData;
     */
    private int mDataPosn;

    /**
     * Constructor used when compressing data before writing.
     */
    public FreeCompressContext(float start, int numBytes) {
        mCurExponent = 114;
        mLastRecalculated = start;
        mDataPosn = 0;
        mStart = start;
        mCompressedData = new byte[numBytes];
    }

    /**
     * Write the conpressed data to the given output stream.
     * Should only be called after the last call to writeDelta.
     */
    public void writeData (DataOutputStream dos) throws IOException {
        dos.writeFloat(mStart);
        dos.writeShort(mDataPosn);
        dos.write(mCompressedData, 0, mDataPosn);
    }

    /**
     * Writes the difference between the supplied value and the previous value to the
     * compressed data stream.
     */
    public void writeDelta (float newX) {
        // Note that the diff is calculated from the last calculated value, not the last real
        // value. See class header for details.
        float delta = newX - mLastRecalculated;
        int exponent = 0;
        if (delta != 0) {
            // zero diff gets saved as byte value zero with any exponent.

            if (mLastRecalculated != 0 && Math.abs(delta/mLastRecalculated) < 0.0001) {
                // delta < 0.01%
                delta = 0;
            } else {
                // Get diff in range (10,100)
                while (Math.abs(delta) > 100.0f) {
                    delta /= 10.0f;
                    exponent++;
                }
                while (Math.abs(delta) < 10.0f) {
                    delta *= 10.0f;
                    exponent--;
                }
                int byteExponent = 114 + exponent;
                if (byteExponent > 127 || byteExponent < 101) {
                    // Diff outside range 10**-13 to 10**13 pixels
                    ScribbleMainActivity.makeToast("Exponent out of range " + byteExponent);
                }
                if (byteExponent != mCurExponent) {
                    // The exponent is different from last time, so save in compressed stream.
                    mCurExponent = (byte) byteExponent;
                    mCompressedData[mDataPosn++] = mCurExponent;
                }
            }
        }
        delta = Math.round(delta);
        byte byteDelta = (byte) delta;
        mCompressedData[mDataPosn++] = byteDelta;

        // We dont use newX as the value to take differences from. Instead, we calculate the value
        // that will be recreated when this data is uncompressed.
        float f = floatFromDelta(mLastRecalculated, mCurExponent, byteDelta);
        mLastRecalculated = f;
    }

    /**
     * Calculates result = start + byteDelta * 10**(exponent-114)
     */
    private float floatFromDelta(float start, int exponent, byte byteDelta) {
        float result = start;
        float delta = byteDelta;
        if (exponent != 114) {
            int powersofTen = exponent-114;
            if (powersofTen > 0) {
                for (int i=0; i<powersofTen; i++) {
                    delta *= 10;
                }
            } else {
                powersofTen = -1*powersofTen;
                for (int i=0; i<powersofTen; i++) {
                    delta /= 10;
                }
            }
        }
        result += delta;
        return result;
    }

    /*
     * ********************* READ DATA ************************
     */

    public FreeCompressContext(DataInputStream dis, ArrayList<PointF> points, boolean useYs) throws IOException {
        readData(dis);
        uncompressData(points, useYs);
    }

    public void readData (DataInputStream dis) throws IOException {
        mStart = dis.readFloat();
        mDataPosn = dis.readShort();
        mCurExponent = 114;
        mLastRecalculated = mStart;
        mCompressedData = new byte[mDataPosn];
        dis.read(mCompressedData, 0, mDataPosn);
    }


    private int setValue (ArrayList<PointF>  points, int uncompressedPosn, boolean useYs) {
        PointF p = points.get(uncompressedPosn);
        if (useYs) {
            p.y = mLastRecalculated;
        } else {
            p.x = mLastRecalculated;
        }
        uncompressedPosn++;
        return uncompressedPosn;
    }

    private void uncompressData (ArrayList<PointF>  points, boolean useYs) {
        int uncompressedPosn = 0;
        int compressedPosn = 0;

        uncompressedPosn = setValue(points, uncompressedPosn, useYs);

        while (compressedPosn < mDataPosn) {
            byte b = mCompressedData[compressedPosn++];
            if (b > 100) {
                // new exponent
                mCurExponent = b;
                b = mCompressedData[compressedPosn++];
            }
            if (b != 0) {
                mLastRecalculated = floatFromDelta(mLastRecalculated, mCurExponent, b);
            }

            uncompressedPosn = setValue(points, uncompressedPosn, useYs);
        }
    }

    public byte[] getmCompressedData() {return mCompressedData;}
    public int getmDataPosn() {return mDataPosn;}

}
