package uk.org.platitudes.scribble.drawitem.freehand.oldstuff;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import uk.org.platitudes.scribble.ScribbleMainActivity;
import uk.org.platitudes.scribble.ScribbleView;
import uk.org.platitudes.scribble.drawitem.DrawItem;

/**
 */
public class FreehandDrawItem extends DrawItem {

    private ArrayList<PointF> mPoints;

    public FreehandDrawItem(MotionEvent event, ScribbleView scribbleView) {
        super (event, scribbleView);
        mPoints = new ArrayList<>();
        handleMoveEvent(event);
    }

    @Override
    public void draw(Canvas c) {
        for (int i = 0; i < mPoints.size() - 1; i++) {
            PointF startPoint = mPoints.get(i);
            PointF endPoint = mPoints.get(i + 1);
            float startX = mScribbleView.storedXtoScreen(startPoint.x);
            float startY = mScribbleView.storedYtoScreen(startPoint.y);
            float endX = mScribbleView.storedXtoScreen(endPoint.x);
            float endY = mScribbleView.storedYtoScreen(endPoint.y);
            c.drawLine(startX, startY, endX, endY, mPaint);
        }
    }

    private void addPoint(float x, float y) {
        float storedX = mScribbleView.screenXtoStored(x);
        float storedY = mScribbleView.screenYtoStored(y);

        // Check to see if very close to last point, if so, don't add it.
        int size = mPoints.size();
        if (size > 0) {
            PointF lastPoint = mPoints.get(size - 1);
            float diffX = Math.abs(lastPoint.x - storedX);
            float diffY = Math.abs(lastPoint.y - storedY);
            if (diffX < 0.01 && diffY < 0.01) {
                //TODO get screen size to fiz min diff
                return;
            }
        }
        PointF p = new PointF(storedX, storedY);
        mPoints.add(p);
    }

    @Override
    public void handleMoveEvent(MotionEvent event) {
        final int historySize = event.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            addPoint(event.getHistoricalX(h), event.getHistoricalY(h));
        }
        addPoint(event.getX(), event.getY());

    }

    public void handleUpEvent(MotionEvent event, ScribbleView scribbleView) {
        scribbleView.addItem(this);
    }

    /**
     * Constructor to read data from file.
     */
    public FreehandDrawItem(DataInputStream dis, int version, ScribbleView sv) {
        super (null, sv);
        try {
            readFromFile(dis, version);
        } catch (IOException e) {
            ScribbleMainActivity.makeToast("Error reading FreehandDrawItem " + e);
        }
    }

    private void compressData(DataOutputStream dos, int version) throws IOException {
        int numPoints = mPoints.size();
        FreeCompressContext xs = new FreeCompressContext(mPoints.get(0).x, numPoints * 2);
        FreeCompressContext ys = new FreeCompressContext(mPoints.get(0).y, numPoints * 2);
        for (int i = 1; i < numPoints; i++) {
            PointF p = mPoints.get(i);
            xs.writeDelta(p.x);
            ys.writeDelta(p.y);
        }
        xs.writeData(dos);
        ys.writeData(dos);
    }

    private void compressedWrite() throws IOException {
        int num = mPoints.size();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(num * 8);
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < num; i++) {
            PointF p = mPoints.get(i);
            dos.writeFloat(p.x);
        }
        for (int i = 0; i < num; i++) {
            PointF p = mPoints.get(i);
            dos.writeFloat(p.y);
        }
        dos.close();

        byte[] data = baos.toByteArray();
        baos.close();

        ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream(data.length);
        DeflaterOutputStream def = new DeflaterOutputStream(compressedBaos);
        def.write(data, 0, data.length);
        def.close();

        byte[] compressedData = compressedBaos.toByteArray();
        // Size for a 479 point curve was 2812 for xs mized wwith ys,
        // 2741 for xs followed by ys. Uncompressed 479x8 = 3832.
        compressedBaos.close();

        ByteArrayInputStream compressedBais = new ByteArrayInputStream(compressedData);
        InflaterInputStream inf = new InflaterInputStream(compressedBais);
        byte[] inflatedData = new byte[num * 8];
        inf.read(inflatedData, 0, inflatedData.length);
        inf.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(inflatedData);
        DataInputStream dis = new DataInputStream(bais);
        PointF[] points = new PointF[num];
        for (int i = 0; i < num; i++) {
            PointF p = new PointF();
            p.x = dis.readFloat();
            points[i] = p;
        }
        for (int i = 0; i < num; i++) {
            PointF p = points[i];
            p.y = dis.readFloat();
        }
        dis.close();
        bais.close();

    }

    public void saveToFile(DataOutputStream dos, int version) throws IOException {
        dos.writeByte(FREEHAND);
        int num = mPoints.size();
        dos.writeInt(num);
        compressData(dos, version);
    }

    public DrawItem readFromFile(DataInputStream dis, int version) throws IOException {
        int numPoints = dis.readInt();
        mPoints = new ArrayList<>(numPoints);
        for (int i = 0; i < numPoints; i++) {
            PointF p = new PointF();
            mPoints.add(p);
        }
        FreeCompressContext xs = new FreeCompressContext(dis, mPoints, false);
        FreeCompressContext ys = new FreeCompressContext(dis, mPoints, true);
        return this;
    }

}
