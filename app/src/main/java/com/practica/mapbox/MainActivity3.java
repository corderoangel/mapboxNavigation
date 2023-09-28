package com.practica.mapbox;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity3 extends AppCompatActivity {
    //private MapView mapView;
    MapboxMap map;
    Point origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        origin = Point.fromLngLat(-75.88410323174762, 8.767312597222844);
        MapView mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            map = mapboxMap;
            map.addOnMapClickListener(point -> {
                Point destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                getRoute(mapboxMap, origin, destination);
                return true;
            });
            style.addImage("red-pin-icon-id", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.baseline_place_48)));
            style.addLayer(new SymbolLayer("icon-layer-id", "icon-source-id").withProperties(
                    iconImage("red-pin-icon-id"),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true),
                    iconOffset(new Float[]{0f, -9f})
            ));
            style.addSource(new GeoJsonSource("route-source-id"));
            LineLayer routeLayer = new LineLayer("route-layer-id", "route-source-id");
            routeLayer.setProperties(
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND),
                    lineWidth(5f),
                    lineColor(Color.parseColor("#006eff"))
            );

            style.addLayer(routeLayer);

            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(origin.latitude(),origin.longitude()), 16));
            /*Point destination = Point.fromLngLat(8.767312597222844, -75.88410323174762);
            getRoute(mapboxMap, origin, destination);*/

        }));





        //mapView = findViewById(R.id.mapView);
        /*mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle((Style.MAPBOX_STREETS));
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(8.767312597222844, -75.88410323174762))
                    .title("Unisinú")
                    .snippet("Universidad del sinú")
            );
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(8.789452639960507, -75.85761042770775))
                    .title("Unicor")
                    .snippet("Universidad de córdoba")
            );
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(8.80602140775475, -75.85017860328682))
                    .title("Upb")
                    .snippet("Universidad pontificia bolivariana")
            );
        });*/

        /*mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle(Style.MAPBOX_STREETS);

            LatLng originLatLng = new LatLng(8.767312597222844, -75.88410323174762);
            LatLng destinationLatLng = new LatLng(8.789452639960507, -75.85761042770775);

            mapboxMap.addMarker(new MarkerOptions()
                    .position(originLatLng)
                    .title("Unisinú")
                    .snippet("Universidad del sinú")
            );

            mapboxMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title("Unicor")
                    .snippet("Universidad de córdoba")
            );

            List<LatLng> routeCoordinates = new ArrayList<>();
            routeCoordinates.add(originLatLng);
            routeCoordinates.add(destinationLatLng);

            mapboxMap.addPolyline(new PolylineOptions()
                    .addAll(routeCoordinates)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(4));

        });*/



    }
    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination){
        MapboxDirections client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.e("algo", "pilas el token"+ response);
                if(response.body() == null){
                    Log.e("algo", "pilas el token"+ response);
                    return;
                }else if(response.body().routes().size() < 1){
                    Log.e("algo", "ruta no encontrada");
                    return;
                }
                DirectionsRoute drivingRoute = response.body().routes().get(0);
                if(mapboxMap != null){
                    mapboxMap.getStyle(style -> {
                        GeoJsonSource routeLineSource = style.getSourceAs("route-source-id");
                        GeoJsonSource iconGeoJsonSource = style.getSourceAs("icon-source-id");

                        if(routeLineSource != null){
                            routeLineSource.setGeoJson(LineString.fromPolyline(drivingRoute.geometry(), PRECISION_6));
                            if(iconGeoJsonSource == null){
                                iconGeoJsonSource = new GeoJsonSource("icon-source-id", Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude())));
                            }else {
                                iconGeoJsonSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude())));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

            }
        });
    }

}