/* 
 * Copyright (C) 2011 Robert Neild
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pquery;

import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import android.view.GestureDetector;

public class MapsActivity extends MapActivity
{    
    MapView mapView; 
    MapController mc;

    /**
     * Stores user selected location
     */
    private GeoPoint point;

    class MapOverlay extends com.google.android.maps.Overlay implements GestureDetector.OnGestureListener, OnDoubleTapListener {

        private GestureDetector gesturedetector;

        public MapOverlay() {
            gesturedetector = new GestureDetector(MapsActivity.this, this);
            gesturedetector.setOnDoubleTapListener(this);
        }

        /**
         * Detects a clean touch (not a drag) and moves location
         */
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
        {   
            return gesturedetector.onTouchEvent(event);

            //			if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //				touchEventInProgress = true;
            //			}
            //
            //			if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //				touchEventInProgress = false;  // user is dragging so cancel detecting location on up
            //			}
            //
            //			if (event.getAction() == MotionEvent.ACTION_UP) {
            //
            //				if (touchEventInProgress) {
            //					// store location
            //
            //					point = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
            //
            //					//                Toast.makeText(getBaseContext(), 
            //					//                        p.getLatitudeE6() / 1E6 + "," + 
            //					//                        p.getLongitudeE6() /1E6 , 
            //					//                        Toast.LENGTH_SHORT).show();
            //
            //					Toast.makeText(getBaseContext(), "Location stored", Toast.LENGTH_SHORT);
            //
            //					Intent data = new Intent();
            //
            //					data.putExtra("lat", point.getLatitudeE6() / 1E6);
            //					data.putExtra("lon", point.getLongitudeE6() /1E6);
            //
            //					setResult(RESULT_OK, data) ;
            //
            //				}
            //
            //				touchEventInProgress = false;
            //			}         
            //
            //			return false;
        }        

        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) 
        {
            super.draw(canvas, mapView, shadow);                   

            if (point!=null) {
                //---translate the GeoPoint to screen pixels---
                Point screenPts = new Point();
                mapView.getProjection().toPixels(point, screenPts);

                //---add the marker---
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pushpin);            
                canvas.drawBitmap(bmp, screenPts.x, screenPts.y - bmp.getHeight(), null);
            }

            return true;
        }

        public boolean onDown(MotionEvent arg0) {
            return false;
        }

        public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {

            return false;
        }

        public void onLongPress(MotionEvent motion) {

            point = mapView.getProjection().fromPixels((int) motion.getX(), (int) motion.getY());

            Toast.makeText(getBaseContext(), "Location stored", Toast.LENGTH_SHORT);

            Intent data = new Intent();

            data.putExtra("lat", point.getLatitudeE6() / 1E6);
            data.putExtra("lon", point.getLongitudeE6() /1E6);

            setResult(RESULT_OK, data) ;
        }

        public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,	float arg3) {
            return false;
        }

        public void onShowPress(MotionEvent arg0) {
        }

        public boolean onSingleTapUp(MotionEvent arg0) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent motion) {
            mapView.getController().zoomInFixing((int) motion.getX(), (int) motion.getY());
            return true;
        }

        public boolean onDoubleTapEvent(MotionEvent motion) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }




    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);

        mapView = (MapView) findViewById(R.id.mapView);

        mapView.setBuiltInZoomControls(true);

        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);  

        mc = mapView.getController();

        double lat = getIntent().getDoubleExtra("lat",Double.MAX_VALUE);
        double lng = getIntent().getDoubleExtra("lon", Double.MAX_VALUE);

        if (lat == Double.MAX_VALUE) {
            lat = 46.4;
            lng = -35;
            mc.setZoom(3); 
        } else {
            mc.setZoom(15);
        }

        mc.setCenter(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
        mapView.invalidate();

    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
}