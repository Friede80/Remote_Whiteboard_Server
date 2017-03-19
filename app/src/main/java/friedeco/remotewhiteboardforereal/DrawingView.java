package friedeco.remotewhiteboardforereal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Friede on 3/16/2017.
 */

public class DrawingView extends View
{
    private Path  drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF00FF00;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    public DrawingView( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        setupDrawing();
    }

    private void setupDrawing()
    {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor( paintColor );
        drawPaint.setAntiAlias( true );
        drawPaint.setStrokeWidth( 20 );
        drawPaint.setStyle( Paint.Style.STROKE );
        drawPaint.setStrokeJoin( Paint.Join.ROUND );
        drawPaint.setStrokeCap( Paint.Cap.ROUND );
        canvasPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
    }

    @Override
    protected void onSizeChanged( int w, int h, int oldw, int oldh )
    {
        super.onSizeChanged( w, h, oldw, oldh );
        canvasBitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );
        drawCanvas = new Canvas( canvasBitmap );
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        canvas.drawBitmap( canvasBitmap, 0, 0, canvasPaint );
        canvas.drawPath( drawPath, drawPaint );
    }

    public void pointerPressed( float x, float y )
    {
        drawPath.moveTo( x, y );
        invalidate();
    }

    public void pointerMoved( float x, float y )
    {
        drawPath.lineTo( x, y );
        invalidate();
    }

    public void pointerLifted()
    {
        drawCanvas.drawPath( drawPath, drawPaint );
        drawPath.reset();
        invalidate();
    }

    public void setColor( int aColor )
    {
        invalidate();
        paintColor = aColor;
        drawPaint.setColor( paintColor );
    }

    public void clear()
    {
        drawCanvas.drawColor( 0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
