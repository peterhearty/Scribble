/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem.freehand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Stores a sequence of floats as a base value plus subsequent deltas (differences). Neighbouring
 * floating point numbers are assumed to be nearby - within a few % of each other. The differences
 * are held as byte values with ranges of [-126,-10] and [10,126] (inclusive). so there is always
 * at least two decimal digits of precision in the deltas.
 *
 * The sequence keeps track of the decimal point relative to the delta values. The delta values
 * -127 and +127 are reserved. The current exponent (mCurrentExponent) is multiplied by 10 every time a delta of
 * +127 is encountered and divided by 10 every time a -127 is encountered. Multiple modifications
 * of mCurrentExponent are allowed.
 *
 * Once built, the sequence is assumed to be read only. i.e. the sequence of calls is always:
 *
 * floatAndDeltas()             constructor
 * addPoint(float f)            multiple calls to add points to the sequence
 * read(DataInputStream dis)    alternative to addPoint(float f), creates the whole sequence
 *
 * firstfloat()                 reads from the start of the sequence
 * nextFloat()                  recovers the next float in the sequence
 *
 */
public class floatAndDeltas {

    /**
     * The base value from which all subsequent values are calculated.
     */
    private float mStart;

    /**
     * true until mStart is valid
     */
    private boolean firstPointAdded;

    /**
     * The sequence of delta values that are added to mStart to get the floating point values.
     * Multiplier modification values (-127 and +127) are embedded in this sequence.
     */
    private byte[] mDeltas;

    /**
     * The next free entry in mDeltas. Same as the number of valid deltas.
     */
    private int mNextFreeDelta;

    /**
     * The last value in the sequence that has been calculated. When the sequence is being read
     * it takes the values:
     *
     *   mLastCalculated = mStart
     *   mLastCalculated = mLastCalculated + byteDelta * mCurrentMultiplier;
     *
     * When values are being added, mLastCalculated should be close to the new value (g) being
     * added. When a subsequent value is added (f) the delta is calculated as
     *
     *   dx = f - mLastCalculated   NOT   dx = f - g
     *
     * The reason is that mLastCalculated is the value that will appear when the sequence is
     * reconstructed and it is dx based on this value that is needed to recover the best
     * approximation to f. In this way, rounding errors do not accumulate but are automatically
     * compensated for.
     */
    private float mLastCalculated;

    /**
     * The current value that deltas should be multiplied by before being added to the
     * mLastCalculated in order to get the next value in the sequence.
     *
     * Assumed to start at 1.0 at the start of the sequence.
     * Gets divided by 10 when -127 is found in the deltas.
     * Gets multiplied by 10 when +127 is found in the deltas.
     */
    private float mCurrentMultiplier;

    /**
     * The integer base 10 Log of mCurrentMultiplier. Only used when sequence is being constructed.
     * It's easier to compare the integer mCurrentExponent than the float mCurrentMultiplier.
     */
    private int mCurrentExponent;

    /**
     * Used when reading the sequence. Starts at zero in firstFloat(), incremented as each delta
     * is read.
     */
    private int mPointer;

    /**
     * Used to store the min and max values. Needed when being selected.
     */
    public float min, max;


    public floatAndDeltas() {
        mDeltas = new byte[100];
        mCurrentExponent = 0;
        mCurrentMultiplier = 1;
        firstPointAdded = false;
    }

    private void checkMinMax () {
        if (mLastCalculated < min) {
            min = mLastCalculated;
        }
        if (mLastCalculated > max) {
            max = mLastCalculated;
        }
    }

    public void moveStart (float delta) {
        mStart += delta;
        min = max = mStart;
        // min and max get adjusted when next draw takes place and nextFloat
        // gets repeatedly called
    }

    public void addPoint(float f) {
        if (!firstPointAdded) {
            min = max = mStart = mLastCalculated = f;
            firstPointAdded = true;
            return;
        }

        float dx = f - mLastCalculated;
        if (f != 0 && Math.abs(dx / f) < 0.0001) {
            // less than 0.01% change, ignore
            dx = 0;
        }
        int exp = 0;
        if (dx != 0) {
            // get in range [-126,126]
            while (Math.abs(dx) > 126) {
                exp++;
                dx /= 10;
            }
            while (Math.abs(dx) < 10) {
                exp--;
                dx *= 10;
            }
            while (exp > mCurrentExponent) {
                addByte((byte) 127);
                mCurrentExponent++;
                mCurrentMultiplier *= 10;
            }
            while (exp < mCurrentExponent) {
                addByte((byte) -127);
                mCurrentExponent--;
                mCurrentMultiplier /= 10;
            }
        }
        byte byteDelta = (byte) Math.round(dx);// ROUND
        addByte(byteDelta);
        mLastCalculated = mLastCalculated + byteDelta * mCurrentMultiplier;
        checkMinMax ();
    }

    private void checkArraySize() {
        if (mNextFreeDelta < mDeltas.length) return;

        byte[] newArray = new byte[mNextFreeDelta * 2];
        System.arraycopy(mDeltas, 0, newArray, 0, mNextFreeDelta);
        mDeltas = newArray;
    }

    private void addByte(byte b) {
        checkArraySize();
        mDeltas[mNextFreeDelta++] = b;
    }


    public void write(DataOutputStream dos) throws IOException {
        dos.writeFloat(mStart);
        dos.writeInt(mNextFreeDelta);
        dos.write(mDeltas, 0, mNextFreeDelta);
    }

    public void read(DataInputStream dis) throws IOException {
        mStart = dis.readFloat();
        mNextFreeDelta = dis.readInt();
        mDeltas = new byte[mNextFreeDelta];
        //noinspection ResultOfMethodCallIgnored
        dis.read(mDeltas, 0, mNextFreeDelta);
        firstPointAdded = true;

        // Calc min, max
        min = max = firstFloat();
    }

    public float firstFloat() {
        mPointer = 0;
        mCurrentMultiplier = 1;
        mLastCalculated = mStart;
        return mStart;
    }

    public float nextFloat() {
        byte b = mDeltas[mPointer++];
        while (b == 127) {
            mCurrentMultiplier *= 10;
            b = mDeltas[mPointer++];
        }
        while (b == -127) {
            mCurrentMultiplier /= 10;
            b = mDeltas[mPointer++];
        }
        float result = mLastCalculated + b * mCurrentMultiplier;
        mLastCalculated = result;
        // Every draw causes min max to recalculate - handles movements and reads from disk
        checkMinMax();
        return result;
    }

}
