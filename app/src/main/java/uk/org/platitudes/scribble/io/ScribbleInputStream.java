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

    private String readLine () {
        StringBuilder sb = new StringBuilder();
        do {
            try {
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
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "readLine", e);
            }
        } while (true);
        String result = sb.toString();
        return result;
    }

    public ScribbleInputStream (InputStream is) {
        firstLongRead = false;
        asText = false;
        dis = new DataInputStream(is);
        try {
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
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleInputStream", "ScribbleInputStream", e);
        }
    }

    public long readLong () {
        long result = 0;
        if (!firstLongRead) {
            firstLongRead = true;
            result = firstLong;
        } else {
            if (asText) {
                String s = readLine();
                result = Long.valueOf(s);
            } else {
                try {
                    result = dis.readLong();
                } catch (IOException e) {
                    ScribbleMainActivity.log("ScribbleInputStream", "readLong", e);
                }
            }
        }
        return result;
    }

    public int readInt () {
        int result = 0;
        if (asText) {
            result = (int) readLong();
        } else {
            try {
                result = dis.readInt();
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "readInt", e);
            }
        }
        return result;
    }

    public byte readByte () {
        byte result = 0;
        if (asText) {
            result = (byte) readLong();
        } else {
            try {
                result = dis.readByte();
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "readByte", e);
            }
        }
        return result;
    }

    public short readShort () {
        short result = 0;
        if (asText) {
            result = (short) readLong();
        } else {
            try {
                result = dis.readShort();
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "readShort", e);
            }
        }
        return result;
    }

    public float readFloat () {
        float result = 0;
        if (asText) {
            String s = readLine();
            result = Float.valueOf(s);
        } else {
            try {
                result = dis.readFloat();
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "readFloat", e);
            }
        }
        return result;
    }

    public String readUTF () {
        String result = "";
        if (asText) {
            String s = readLine();
            result = s;
        } else {
            try {
                result = dis.readUTF();
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "readUTF", e);
            }
        }
        return result;
    }

    public void read (byte[] buffer, int start, int count) {
        if (asText) {
            String s = readLine();
            StringTokenizer tokens = new StringTokenizer(s, " ");
            for (int i=0; i<count; i++) {
                String nextToken = tokens.nextToken();
                byte b = Byte.valueOf(nextToken);
                buffer[i]=b;
            }
        } else {
            try {
                dis.read(buffer, start, count);
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleInputStream", "read", e);
            }
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
