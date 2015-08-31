/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.drawitem;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.freehand.FreehandCompressedDrawItem;
import uk.org.platitudes.scribble.drawitem.text.TextItem;
import uk.org.platitudes.scribble.io.ScribbleInputStream;
import uk.org.platitudes.scribble.io.ScribbleOutputStream;

/**
 */
public class ItemList {

    private ArrayList<DrawItem> mList;

    /**
     * Used when a list is being read in to identify the presence of MoveItems. Each one has to
     * be matched up to a target DrawItem (not necessarily in the same ItemList).
     */
    public boolean mContainsMoveItems;

    public ItemList () {
        mList = new ArrayList<>();
    }

    public void add (DrawItem item) {
        mList.add(item);
    }

    public String toString () {
        return mList.size()+" items";
    }

    public void onDraw (Canvas c) {
        for (DrawItem d : mList) {
            if (!d.deleted) {
                d.draw(c);
            }
        }
    }

    public int getHashTag () {
        int result = 0;
        for (DrawItem d : mList) {
            result += d.getHashTag();
        }
        return result;
    }

    private byte scanForValidType (ScribbleInputStream dis) throws IOException {
        byte itemType = dis.readByte();
        while ((itemType < DrawItem.LOWEST_TYPE) || (itemType > DrawItem.HIGHEST_TYPE)) {
            // error reading file, try to find a valid object
            itemType = dis.readByte();
        }
        return itemType;
    }

    public ItemList (ScribbleInputStream dis, int version, ScribbleView scribbleView) throws IOException {
        int numItems = dis.readInt();
        if (numItems > 100000) {
            // assume this to be an error
            numItems = 10000;
        }
        mList = new ArrayList<>(numItems+20);
        for (int i=0; i<numItems; i++) {
            byte itemType = 0; // declare here to put in scope for "catch" below
            try {
                // note the point in the input stream where the DrawItem begins
                // if anything goes wrong we'll reset to here
                dis.mark();

                DrawItem item = null;
                itemType = scanForValidType(dis);
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
                    case DrawItem.MOVE:
                        item = new MoveItem(dis, version, scribbleView);
                        mContainsMoveItems = true;
                        break;
                    case DrawItem.GROUP:
                        item = new GroupItem(dis, version, scribbleView);
                    case DrawItem.DEFAULT_ITEM:
                        // do nothing
                        break;
                    default:
                        throw new Exception("Error reading data file");
                }
                if (item != null)
                    mList.add(item);
            } catch (EOFException eof) {
                ScribbleMainActivity.log("Read error", "EOF", eof);
                return;
            } catch (Exception e) {
                ScribbleMainActivity.log("Read error", "type=" + itemType, e);
                // go back to start of item that caused the error
                dis.reset();
                dis.readByte();  // read the original type byte, next loop iteration will scan forward
            }
        }
    }

    public void tieMoveItemsToTargets (ItemList itemList) {
        if (!mContainsMoveItems) {
            return;
        }
        for (DrawItem d : mList) {
            if (d instanceof  MoveItem) {
                MoveItem m = (MoveItem) d;
                m.matchDrawItem(itemList.mList);
            }
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

    public int clean () {
        // Have to collect items first
        ArrayList<DrawItem> cleanedItems = new ArrayList<>(mList.size());
        for (DrawItem d : mList) {
            if (d instanceof MoveItem) {
                cleanedItems.add(d);
            } else if (d.deleted) {
                cleanedItems.add(d);
            } else if (d instanceof GroupItem) {
                GroupItem g = (GroupItem) d;
                if (g.isEmpty()) {
                    cleanedItems.add(d);
                }
            }
        }
        for (DrawItem d : cleanedItems) {
            mList.remove(d);
        }
        return cleanedItems.size();
    }

    public boolean isEmpty () {
        boolean result = true;
        if (mList != null) {
            if (mList.size() > 0) {
                result = false;
            }
        }
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

    public ItemList findSelectedItems (PointF start, PointF end) {
        ItemList result = new ItemList();
        for (DrawItem d : mList) {
            boolean itemSelected = d.selectItem(start, end);
            if (itemSelected) {
                result.add(d);
                // return its paint color to black and mark as unselected now that it's been added
                d.deselectItem();
            }
        }
        return result;
    }

    public void removeItems (ItemList source) {
        for (DrawItem d : source.mList) {
            mList.remove(d);
        }
    }

    public void moveFromList (ItemList source) {
        for (DrawItem d : source.mList) {
            mList.add(d);
        }
        source.mList.clear();
    }

    public void remove (DrawItem d) {
        mList.remove(d);
    }

    public void move(float deltaX, float deltaY) {
        for (DrawItem d : mList) {
            d.move(deltaX, deltaY);
        }
    }

    public void selectedAll () {
        for (DrawItem d : mList) {
            d.selectItem();
        }
    }

    public void deSelectedAll () {
        for (DrawItem d : mList) {
            d.deselectItem();
        }
    }

    public int clear () {
        int numCleared = mList.size();
        mList.clear();
        return numCleared;
    }

    public RectF getBounds () {
        RectF result = null;
        for (DrawItem d : mList) {
            RectF itemBounds = d.getBounds();
            if (itemBounds == null)
                continue;
            if (result == null) {
                // first bounds
                result = itemBounds;
            } else {
                if (itemBounds.left < result.left)
                    result.left = itemBounds.left;
                if (itemBounds.top < result.top)
                    result.top = itemBounds.top;
                if (itemBounds.right > result.right)
                    result.right = itemBounds.right;
                if (itemBounds.bottom > result.bottom)
                    result.bottom = itemBounds.bottom;
            }
        }
        return result;
    }

}
