package mockClasses; /**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

/**
 * A basic mock Canvas object.
 */
public class TestCanvas extends Canvas {

    public static int MAXIMUM = 10000;

    public ArrayList<RectF> history;
    public boolean hasFailed;

    public TestCanvas () {
        history = new ArrayList<>();
    }

    @Override
    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        if (startX < 0 || startY < 0 || stopX > MAXIMUM || stopY > MAXIMUM) {
            hasFailed = true;
            // Note - we still record the out of bounds points below
        }
        RectF line = new RectF(startX, startY, stopX, startY);
        history.add(line);
    }

    @Override
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        if (cx < 0 || cy < 0 || radius < 0 || radius > MAXIMUM) {
            hasFailed = true;
            // Note - we still record the out of bounds points below
        }
        RectF line = new RectF(cx, cy, radius, 0);
        history.add(line);
    }
}
