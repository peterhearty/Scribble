/**
 * This source code is not owned by anybody. You can can do what you like with it.
 */
package uk.org.platitudes.scribble.mock;

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
    public ArrayList<TextEntry> textHistory;
    public boolean hasFailed;
    public int lineCount;
    public int circleCount;
    public int textCount;

    public TestCanvas () {
        testReset();
    }

    @Override
    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        if (startX < -MAXIMUM || startY < -MAXIMUM || stopX > MAXIMUM || stopY > MAXIMUM) {
            hasFailed = true;
            // Note - we still record the out of bounds points below
        }
                            // left     top     right   bottom
        RectF line = new RectF(startX, startY, stopX, stopY);
        history.add(line);
        lineCount++;
    }

    @Override
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        if (cx < -MAXIMUM || cy < -MAXIMUM || radius < 0 || radius > MAXIMUM) {
            hasFailed = true;
            // Note - we still record the out of bounds points below
        }
        RectF line = new RectF(cx, cy, radius, 0);
        history.add(line);
        circleCount++;
    }

    @Override
    public void drawText(String text, float x, float y, Paint paint) {
        TextEntry textEntry = new TextEntry(x, y, text);
        textHistory.add(textEntry);
        textCount++;
    }

    class TextEntry {
        float x;
        float y;
        String text;

        TextEntry (float x, float y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }

    // Methods that are not part of android.graphics.Canvas are prefixed with the word "test".

    public void testReset () {
        history = new ArrayList<>();
        textHistory = new ArrayList<>();
        lineCount = circleCount = textCount = 0;
        hasFailed = false;
    }

    public boolean testDrawCount (int expected) {
        if (history.size() == expected) {
            return true;
        }
        return false;
    }

    public boolean testStartPosition (int drawNumber, float expectedX, float expectedY) {
        RectF singleDraw = history.get(drawNumber);
        boolean result = true;
        if (singleDraw.top != expectedY) {
            result = false;
        }
        if (singleDraw.left != expectedX) {
            result = false;
        }
        return result;
    }

    public boolean testEndPosition (int drawNumber, float expectedX, float expectedY) {
        RectF singleDraw = history.get(drawNumber);
        boolean result = true;
        if (singleDraw.bottom != expectedY) {
            result = false;
        }
        if (singleDraw.right != expectedX) {
            result = false;
        }
        return result;
    }
}
