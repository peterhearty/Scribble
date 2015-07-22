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
import uk.org.platitudes.scribble.drawitem.freehand.oldstuff.FreehandDrawItem;
import uk.org.platitudes.scribble.drawitem.text.TextItem;

/**
 */
public class ItemList {

    private ArrayList<DrawItem> mList;

    private DrawItem mLastSelected;

    public ItemList () {
        mList = new ArrayList<>();
    }

    public void add (DrawItem item) {
        mList.add(item);
    }

    public void onDraw (Canvas c, ScribbleView v) {
        for (DrawItem d : mList) {
            d.draw(c, v);
        }
    }

    public ItemList (DataInputStream dis, int version) throws IOException {
        int numItems = dis.readInt();
        mList = new ArrayList<>(numItems+20);
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
                case DrawItem.TEXT:
                    item = new TextItem(dis, version);
                    break;
                case DrawItem.COMPRESSED_FREEHAND:
                    item = new FreehandCompressedDrawItem(dis, version);
                    break;
                default:
                    ScribbleMainActivity.makeToast("Error reading data file");
            }
            if (item != null)
                mList.add(item);
        }
    }

    public void write (DataOutputStream dos, int version) throws IOException {
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


    public int toggleSelected(PointF selectionPoint, boolean allowMultiple) {
        int changeCount = 0;
        // We scan the list from the end backwards.
        // Most likely to want to adjust the most recently added item.
        for (int i=mList.size()-1; i >= 0; i--) {
            DrawItem d = mList.get(i);
            boolean selectionChanged = d.toggleSelected(selectionPoint);
            if (selectionChanged) {
                if (d.isSelected()) {
                    mLastSelected = d;
                }
                changeCount++;
                if (!allowMultiple)
                    break;
            }
        }
        return changeCount;
    }

    public DrawItem getmLastSelected() {return mLastSelected;}

}
