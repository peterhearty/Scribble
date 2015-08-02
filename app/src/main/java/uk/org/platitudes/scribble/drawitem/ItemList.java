/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.PointF;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.freehand.FreehandCompressedDrawItem;
//import uk.org.platitudes.scribble.drawitem.freehand.oldstuff.FreehandDrawItem;
import uk.org.platitudes.scribble.drawitem.text.TextItem;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 */
public class ItemList {

    private ArrayList<DrawItem> mList;

    public ItemList () {
        mList = new ArrayList<>();
    }

    public void add (DrawItem item) {
        mList.add(item);
    }

    public void onDraw (Canvas c) {
        for (DrawItem d : mList) {
            d.draw(c);
        }
    }

    public ItemList (ScribbleInputStream dis, int version, ScribbleView scribbleView) throws IOException {
        int numItems = dis.readInt();
        mList = new ArrayList<>(numItems+20);
        for (int i=0; i<numItems; i++) {
            DrawItem item = null;
            byte itemType = dis.readByte();
            switch (itemType) {
                case DrawItem.FREEHAND:
//                    item = new FreehandDrawItem(dis, version, scribbleView);
                    break;
                case DrawItem.LINE:
                    item = new LineDrawItem(dis, version, scribbleView);
                    break;
                case DrawItem.TEXT:
                    item = new TextItem(dis, version, scribbleView);
                    break;
                case DrawItem.COMPRESSED_FREEHAND:
                    item = new FreehandCompressedDrawItem(dis, version, scribbleView);
                    break;
                case DrawItem.DEFAULT_ITEM:
                    // do nothing
                    break;
                default:
                    ScribbleMainActivity.log ("Error reading data file", "", null);
                    return;
            }
            if (item != null)
                mList.add(item);
        }
    }

    public void write (ScribbleOutputStream dos, int version) throws IOException {
        dos.writeInt(mList.size());
        for (int i=0; i< mList.size(); i++) {
            DrawItem di = mList.get(i);
            di.saveToFile(dos, version);
        }
    }

    public DrawItem moveLastTo (ItemList dest) {
        if (mList.size() == 0) return null;

        DrawItem result = mList.remove(mList.size() - 1);
        dest.mList.add(result);

        return result;
    }


    public DrawItem findFirstSelectedItem (PointF selectionPoint) {
        DrawItem result = null;
        // We scan the list from the end backwards.
        // Most likely to want to adjust the most recently added item.
        for (int i=mList.size()-1; i >= 0; i--) {
            DrawItem d = mList.get(i);
            boolean selected = d.selectItem(selectionPoint);
            if (selected) {
                result = d;
                break;
            }
        }
        return result;
    }

    public void clear () {
        mList.clear();
    }

}
