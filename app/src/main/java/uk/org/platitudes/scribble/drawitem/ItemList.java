/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;

/**
 */
public class ItemList {

    private ArrayList<DrawItem> list;

    public ItemList () {
        list = new ArrayList<>();
    }

    public void add (DrawItem item) {
        list.add(item);
    }

    public void onDraw (Canvas c, ScribbleView v) {
        for (DrawItem d : list) {
            d.draw(c, v);
        }
    }

    public ItemList (DataInputStream dis, int version) throws IOException {
        int numItems = dis.readInt();
        list = new ArrayList<>(numItems+20);
        for (int i=0; i<numItems; i++) {
            DrawItem item = null;
            byte itemType = dis.readByte();
            switch (itemType) {
                case DrawItem.FREEHAND:
                    item = new FreehandDrawItem(dis, version);
                    break;
                case DrawItem.LINE:
                    item = new LineDrawItem(dis, version);
                    break;
                case DrawItem.COMPRESSED_FREEHAND:
                    item = new FreehandCompressedDrawItem(dis, version);
                    break;
                default:
                    ScribbleMainActivity.makeToast("Error reading data file");
            }
            if (item != null)
                list.add(item);
        }
    }

    public void write (DataOutputStream dos, int version) throws IOException {
        dos.writeInt(list.size());
        for (int i=0; i<list.size(); i++) {
            DrawItem di = list.get(i);
            di.saveToFile(dos, version);
        }
    }

    public DrawItem moveLastTo (ItemList dest) {
        if (list.size() == 0) return null;

        DrawItem result = list.remove(list.size() - 1);
        dest.list.add(result);

        return result;
    }


}
