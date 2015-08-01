/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Used to alter IO format, e.g. binary/text.
 */
public class ScribbleOutputStream {

    private boolean asText;
    private DataOutputStream dos;

    /**
     */
    public ScribbleOutputStream(OutputStream out, boolean writeAsText) {
        dos = new DataOutputStream(out);
        asText = writeAsText;
    }

    private void writeString (String s) throws IOException {
        String charsetName = "US_ASCII";
        byte[] bytes = s.getBytes(charsetName);
        dos.write(bytes);
        dos.writeByte('\n');
    }

    public void writeLong (long l) {
        try {
            if (asText) {
                String s = Long.toString(l);
                writeString(s);
            } else {
                dos.writeLong(l);
            }
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleOutputStream", "writeLong", e);
        }
    }

    public void writeInt (int l) {
        writeLong(l);
    }

    public void writeByte (int l) {
        writeLong(l);
    }

    public void writeFloat (float f) {
        try {
            if (asText) {
                String s = Float.toString(f);
                writeString(s);
            } else {
                dos.writeFloat(f);
            }
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleOutputStream", "writeFloat", e);
        }
    }

    public void write (byte[] buffer, int offset, int count) {
        try {
            if (asText) {
                StringBuilder sb = new StringBuilder(count*5);
                for (int i=0; i<count; i++) {
                    if (i>0) {
                        sb.append(' ');
                    }
                    String byteString = Byte.toString(buffer[i]);
                    sb.append(byteString);
                }
                writeString(sb.toString());
            } else {
                dos.write(buffer, offset, count);
            }
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleOutputStream", "write", e);
        }
    }

    public void writeUTF (String s) {
        try {
            if (asText) {
                writeString(s);
            } else {
                dos.writeUTF(s);
            }
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleOutputStream", "writeUTF", e);
        }

    }


    public void close () {
        try {
            dos.close();
        } catch (IOException e) {
            ScribbleMainActivity.log("ScribbleOutputStream", "close", e);
        }
    }

}
