package com.example.googlemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class DemoOverlay extends Overlay {
	
	GeoPoint geoPoint = null;
	GeoPoint geoPoint1 = null;
	
	public DemoOverlay(GeoPoint g1, GeoPoint g2){
		geoPoint = g1;
		geoPoint1 = g2;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		Projection projection = mapView.getProjection();

		int latSpan = mapView.getLatitudeSpan();
		int lngSpan = mapView.getLongitudeSpan();
		GeoPoint mapCenter = mapView.getMapCenter();
		int mapLeftGeo = mapCenter.getLongitudeE6() - (lngSpan / 2);
		int mapRightGeo = mapCenter.getLongitudeE6() + (lngSpan / 2);

		int mapTopGeo = mapCenter.getLatitudeE6() - (latSpan / 2);
		int mapBottomGeo = mapCenter.getLatitudeE6() + (latSpan / 2);

		

		if ((geoPoint.getLatitudeE6() > mapTopGeo && geoPoint.getLatitudeE6() < mapBottomGeo)
				&& (geoPoint.getLongitudeE6() > mapLeftGeo && geoPoint.getLongitudeE6() < mapRightGeo) ) {

			Point myPoint = new Point();
			projection.toPixels(geoPoint, myPoint);

			Bitmap marker = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.markerblue);

			canvas.drawBitmap(marker, myPoint.x - 15, myPoint.y - 30, null);
		}
		
		//GeoPoint geoPoint1 = this.getSampleLocation1();

		if ((geoPoint1.getLatitudeE6() > mapTopGeo && geoPoint1.getLatitudeE6() < mapBottomGeo)
				&& (geoPoint1.getLongitudeE6() > mapLeftGeo && geoPoint1.getLongitudeE6() < mapRightGeo)) {

			Point myPoint = new Point();
			projection.toPixels(geoPoint1, myPoint);

			Bitmap marker = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.markerblue);

			canvas.drawBitmap(marker, myPoint.x - 15, myPoint.y - 30, null);
		}
        
        if (shadow == false) {

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Point point = new Point();
            projection.toPixels(geoPoint, point);
            //paint.setColor(Color.BLUE);
            Point point2 = new Point();
            projection.toPixels(geoPoint1, point2);
            paint.setStrokeWidth(2);
            canvas.drawLine((float) point.x, (float) point.y, (float) point2.x,
                    (float) point2.y, paint);
        }
		
		
		
		
		
	}



}
