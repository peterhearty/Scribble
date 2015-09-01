/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.SortedMap;

import uk.org.platitudes.scribble.ScribbleMainActivity;

/**
 * Used to alter IO format, e.g. binary/text.
 */
public class ScribbleOutputStream {

    private boolean asText;
    private DataOutputStream dos;
    private static String charsetName;

    /**
     */
    public ScribbleOutputStream(OutputStream out, boolean writeAsText) {
        dos = new DataOutputStream(out);
        asText = writeAsText;
    }

    /**
     * Utility routine to return a ScribbleOutputStream that has had any necessary
     * header setup correctly.
     */
    public static ScribbleOutputStream newScribbleOutputStream (OutputStream out, boolean writeAsText) {
        ScribbleOutputStream sos = new ScribbleOutputStream(out, writeAsText);
        sos.writeLong(ScribbleReader.MAGIC_NUMBER);
        return sos;
    }

    // These are all supposed to be supported
    // http://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html
    private static final String[] possibleCharsets = {"US-ASCII", "US_ASCII", "ISO-8859-1", "ISO-LATIN-1", "UTF-8"};

    private void checkCharset () {
        if (charsetName == null) {
            for (String name : possibleCharsets) {
                if (Charset.isSupported(name)) {
                    charsetName = name;
                    break;
                }
            }
        }
        if (charsetName == null) {
            SortedMap<String,Charset> charsetMap = Charset.availableCharsets();
            for (String entryName : charsetMap.keySet()) {
                // Some use US_ASCII instead of US-ASCII
                if (entryName.indexOf("ASCII") != -1) {
                    charsetName = entryName;
                    break;
                }
            }
        }
        if (charsetName == null) {
            ScribbleMainActivity.log ("ScribbleOutputStream", "Could not find a supported charset", null);
        }
//        charsetName = "US_ASCII";
    }

    private void writeString (String s) throws IOException {
        if (charsetName == null) {
            checkCharset();
        }
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
        if (asText) {
            writeLong(l);
        } else {
            try {
                dos.writeInt(l);
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleOutputStream", "writeInt", e);
            }
        }
    }

    public void writeByte (int l) {
        if (asText) {
            writeLong(l);
        } else {
            try {
                dos.writeByte(l);
            } catch (IOException e) {
                ScribbleMainActivity.log("ScribbleOutputStream", "writeInt", e);
            }
        }
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
                dos.writeUTF(s);
//                writeString(s);
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
