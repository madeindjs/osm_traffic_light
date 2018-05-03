package com.rousseau_alexandre.testmap;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class Enemy extends Marker {

    private static int DELAY_MOVEMENT = 500;

    public int life = 5;

    private final MapView mapView;
    private final Context context;

    public Enemy(MapView mapView, Context context) {
        super(mapView);
        this.mapView = mapView;
        this.context = context;
        //this.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

    }

    public void updateTitle() {
        this.setTitle(Integer.toString(this.life));
    }

    @Override
    protected boolean onMarkerClickDefault(Marker marker, MapView mapView) {
        this.life = this.life - 1;
        this.updateTitle();

        if (this.life > 0) {
            return super.onMarkerClickDefault(marker, mapView);
        } else {
            return this.destroy();
        }
    }

    /**
     * Move a point
     * https://stackoverflow.com/questions/31337149/animating-markers-on-openstreet-maps-using-osmdroid
     *
     * @param to
     */
    public void moveTo(final GeoPoint to) {

        this.drawRoad(to);


        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mapView.getProjection();
        Point startPoint = proj.toPixels(this.getPosition(), null);
        final IGeoPoint startGeoPoint = proj.fromPixels(startPoint.x, startPoint.y);
        final long duration = 10000;
        final Interpolator interpolator = new LinearInterpolator();


        final double toLongitude = to.getLongitude();
        final double toLatitude = to.getLatitude();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toLongitude + (1 - t) * startGeoPoint.getLongitude();
                double lat = t * toLatitude + (1 - t) * startGeoPoint.getLatitude();
                Enemy.this.setPosition(new GeoPoint(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, DELAY_MOVEMENT);
                }else{
                    // destroy when arrived
                    // TODO remove one life to gamer
                    Enemy.this.destroy();
                }
                mapView.postInvalidate();

            }
        });
    }


    public void drawRoad(final GeoPoint to) {
        ArrayList<GeoPoint> wayPoints = new ArrayList<GeoPoint>();
        wayPoints.add(this.getPosition());
        wayPoints.add(to);
        RoadManager roadManager = new OSRMRoadManager(context);
        Road road = roadManager.getRoad(wayPoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        mapView.getOverlays().add(roadOverlay);
        mapView.invalidate();
    }

    public boolean destroy() {
        mapView.getOverlayManager().remove(this);
        return true;
    }

}
