package ekg_animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.kardiamobile.R;

public class ekg_view extends View {

    private Bitmap ekgLineBitmap; // Bitmap für die EKG-Linie
    private Paint pointPaint;     // Paint für den schwarzen Punkt
    private float pointPositionX = 0; // Startposition des Punkts
    private float pointSpeed = 5; // Geschwindigkeit des Punkts

    public ekg_view(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // EKG-Bild laden
        ekgLineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ekg_000);

        // Farbe und Stil des schwarzen Punkts
        pointPaint = new Paint();
        pointPaint.setColor(0xFF000000); // Schwarz
        pointPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Zeichne die EKG-Linie (Bitmap)
        if (ekgLineBitmap != null) {
            canvas.drawBitmap(ekgLineBitmap, 0, 0, null);
        }

        // Zeichne den schwarzen Punkt
        float pointY = getHeight() / 2f; // Positioniere den Punkt in der Mitte der Höhe des Bildes
        canvas.drawCircle(pointPositionX, pointY, 10, pointPaint);

        // Bewege den Punkt nach rechts
        pointPositionX += pointSpeed;

        // Wenn der Punkt das Ende erreicht, starte ihn von vorne
        if (pointPositionX > getWidth()) {
            pointPositionX = 0;
        }

        // Wiederhole die Animation durch Neuzeichnen
        postInvalidateOnAnimation();
    }
}
