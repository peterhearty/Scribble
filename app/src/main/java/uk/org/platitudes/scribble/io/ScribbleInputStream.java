/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 */
public class ScribbleInputStream {

    private boolean asText;
    private DataInputStream dis;
    private long firstLong;
    private boolean firstLongRead;
    private int lineNumber;

    private byte[] buffer;

    private String readLine () throws IOException {
        StringBuilder sb = new StringBuilder();
        do {
            if (dis.available()==0) {
                throw new EOFException();
            }
            int nextByte = dis.read();
            if (nextByte == '\n') {
                break;
            }
            if (nextByte != 0) {
                // Zeroes got appended when the line terminator was written
                // using writeChar (a 16 bit unicode value) rather than writeByte.
                sb.append((char)nextByte);
            }
        } while (true);
        lineNumber++;
        String result = sb.toString();
        return result;
    }

    /**
     * We copy the input stream to an internal byte[] buffer. This guarantees that we can
     * do mark() and reset() operations.
     */
    private InputStream readWholeInputStream (InputStream is) throws IOException {
        int totalSize = 0;
        int available = is.available();
        while (available > 0) {
            // read in the available bytes
            byte[] availableBytes = new byte[available];
            is.read(availableBytes);
            totalSize += available;

            // add them to any bytes read in a previous loop
            if (buffer == null) {
                // first bytes read
                buffer = availableBytes;
            } else {
                // add them to previous bytes
                byte[] newBuffer = new byte[totalSize];
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                System.arraycopy(availableBytes, 0, newBuffer, buffer.length, availableBytes.length);
            }

            available = is.available();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        return bais;
    }

    public ScribbleInputStream (InputStream is) throws IOException {
        firstLongRead = false;
        asText = false;

        is = readWholeInputStream(is);

        dis = new DataInputStream(is);
        firstLong = dis.readLong();
        if (firstLong != ScribbleReader.MAGIC_NUMBER) {
            // Not a scribble binary file, try as text. We've already read the first 4 bytes
            // so we need MAGIC_NUMBER as a string minus the first 4 chars.
            String magicString = Long.toString(ScribbleReader.MAGIC_NUMBER);
            String magicStringTruncated = magicString.substring(8);
            String nextLine = readLine();
            if (nextLine.equals(magicStringTruncated)) {
                asText = true;
                firstLong = ScribbleReader.MAGIC_NUMBER;
            }
        }
    }

    public void mark () {
        if (dis.markSupported()) {
            dis.mark(1024);
        }
    }

    public void reset () throws IOException {
        if (dis.markSupported()) {
            dis.reset();
        }
    }

    public long readLong () throws IOException {
        long result = 0;
        if (!firstLongRead) {
            firstLongRead = true;
            result = firstLong;
        } else {
            if (asText) {
                String s = readLine();
                try {
                    result = Long.valueOf(s);
                } catch (NumberFormatException e) {
                    // An error wil result in zero being returned
                    // Eventually this will cause a DrawItem or ItemList error
                    // Item list should rewind and try to skip past the error
                    ScribbleMainActivity.log("ScribbleInputStream", "Error reading number: "+s+" at line "+lineNumber, null);

                    // A UTF string can be followed immediately by a new DrawItem code. Try decoding
                    // this by reading from the end of the current line.
                    if (s.length() > 0 ) {
                        int i = s.length()-1;
                        char c = s.charAt(i);
                        while (c >= '0' && c <='9' && i > 0) {
                            c = s.charAt(--i);
                        }
                        if (i < s.length()-1) {
                            String numberString = s.substring(i+1);
                            ScribbleMainActivity.log("ScribbleInputStream", "Trying: "+numberString, null);
                            result = Long.valueOf(numberString);
                        }
                    }
                }
            } else {
                result = dis.readLong();
            }
        }
        return result;
    }

    public int readInt () throws IOException {
        int result = 0;
        if (asText) {
            result = (int) readLong();
        } else {
            result = dis.readInt();
        }
        return result;
    }

    public byte readByte () throws IOException {
        byte result = 0;
        if (asText) {
            result = (byte) readLong();
        } else {
            result = dis.readByte();
        }
        return result;
    }

    public short readShort () throws IOException {
        short result = 0;
        if (asText) {
            result = (short) readLong();
        } else {
            result = dis.readShort();
        }
        return result;
    }

    public float readFloat () throws IOException {
        float result = 0;
        if (asText) {
            String s = readLine();
            result = Float.valueOf(s);
        } else {
            result = dis.readFloat();
        }
        return result;
    }

    public String readUTF () throws IOException {
        String result = "";
        if (asText) {
            result = dis.readUTF();
//            String s = readLine();
//            result = s;
        } else {
            result = dis.readUTF();
        }
        return result;
    }

    public void read (byte[] buffer, int start, int count) throws IOException {
        if (asText) {
            String s = readLine();
            StringTokenizer tokens = new StringTokenizer(s, " ");
            for (int i=0; i<count; i++) {
                String nextToken = tokens.nextToken();
                byte b = Byte.valueOf(nextToken);
                buffer[i]=b;
            }
        } else {
            dis.read(buffer, start, count);
        }

    }

    public void close () {
        try {
            dis.close();
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleInputStream", "close", e);
        }
    }
}
