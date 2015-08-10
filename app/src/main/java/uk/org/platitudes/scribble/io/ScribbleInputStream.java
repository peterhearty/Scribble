/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.DataInputStream;
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

    private String readLine () throws IOException {
        StringBuilder sb = new StringBuilder();
        do {
            if (dis.available()==0) {
                break;
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
        String result = sb.toString();
        return result;
    }

    public ScribbleInputStream (InputStream is) throws IOException {
        firstLongRead = false;
        asText = false;
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

    public long readLong () throws IOException {
        long result = 0;
        if (!firstLongRead) {
            firstLongRead = true;
            result = firstLong;
        } else {
            if (asText) {
                String s = readLine();
                result = Long.valueOf(s);
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
