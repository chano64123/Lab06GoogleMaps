package com.example.lab06googlemaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "estilo del mapa";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    GroundOverlayOptions paisOverlay = new GroundOverlayOptions();
    //COORDENADAS DE PAISES
    private LatLng japon = new LatLng(35.680513, 139.769051);
    private LatLng alemania = new LatLng(52.516934, 13.403190);
    private LatLng italia = new LatLng(41.902609, 12.494847);
    private LatLng francia = new LatLng(48.843489, 2.355331);
    private LatLng posicionaActual;
    private Location locationOrigen = new Location("Origen");
    private Location locationDestino = new Location("Destino");
    private double latitudActual = 0;
    private double longitudActual = 0;
    private double latitudFinal = 0;
    private double longitudFinal = 0;
    private String messageAlert = "";
    private boolean isAlertDisplayed = false;
    private int seleccion = 0;
    //variables para guardar valores
    static final String ALERT_STATE = "state_of_Alert";
    static final String SELECTED_TYPE = "selected_type";
    static final String MESSAGE_ALERT = "message_alert";
    static final String LATITUD_FINAL = "latitud_final";
    static final String LONGITUD_FINAL = "longitud_final";
    static final String LATITUD_ACTUAL = "latitud_actual";
    static final String LONGITUD_ACTUAL = "longitud_actual";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ALERT_STATE,isAlertDisplayed);
        savedInstanceState.putInt(SELECTED_TYPE,seleccion);
        savedInstanceState.putString(MESSAGE_ALERT,messageAlert);
        savedInstanceState.putDouble(LATITUD_FINAL,latitudFinal);
        savedInstanceState.putDouble(LONGITUD_FINAL,longitudFinal);
        savedInstanceState.putDouble(LATITUD_ACTUAL,latitudActual);
        savedInstanceState.putDouble(LONGITUD_ACTUAL,longitudActual);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //PARA RECUPERAR DATOS GUARDADOS
        if (savedInstanceState != null) {
            isAlertDisplayed = savedInstanceState.getBoolean(ALERT_STATE);
            seleccion = savedInstanceState.getInt(SELECTED_TYPE);
            messageAlert = savedInstanceState.getString(MESSAGE_ALERT);
            longitudFinal = savedInstanceState.getDouble(LONGITUD_FINAL);
            latitudFinal = savedInstanceState.getDouble(LATITUD_FINAL);
            longitudActual = savedInstanceState.getDouble(LONGITUD_ACTUAL);
            latitudActual = savedInstanceState.getDouble(LATITUD_ACTUAL);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //CARGAR ESTILO DE MAPA
        try {
            boolean success = mMap .setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(),R.raw.map_style));
            if (!success){
                Log.e(TAG,getString(R.string.falloCargaMapa));
            }
        }catch (Resources.NotFoundException e) {
            Log.e(TAG,getString(R.string.noCargarEstilo), e);
        }
        //AGREGANDOP BOTONES DE ZOOM
        mMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
        obtenerUbicacionActual();

        if (seleccion != 0){
            seleccionMapa(seleccion);
        }

        //PARA EL SPINNER
        Spinner spnTipoMapa = (Spinner)findViewById(R.id.spnTipoMapa);

        spnTipoMapa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seleccion=position;
                isAlertDisplayed=true;
                seleccionMapa(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void seleccionMapa(int seleccion){
        locationOrigen.setLatitude(latitudActual);
        locationOrigen.setLongitude(longitudActual);
        locationDestino.setLatitude(latitudFinal);
        locationDestino.setLongitude(longitudFinal);
        mMap.clear();
        switch(seleccion){
            case 1:
                //paisOverlay.image(BitmapDescriptorFactory.fromResource(R.drawable.world)).position(japon,100);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                latitudFinal = japon.latitude;
                longitudFinal = japon.longitude;
                agregarMarcadorIcono(japon,R.drawable.world);
                break;
            case 2:
                //paisOverlay.image(BitmapDescriptorFactory.fromResource(R.drawable.satellite)).position(alemania,100);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                latitudFinal = alemania.latitude;
                longitudFinal = alemania.longitude;
                agregarMarcadorIcono(alemania,R.drawable.satellite);
                break;
            case 3:
                //paisOverlay.image(BitmapDescriptorFactory.fromResource(R.drawable.mountain)).position(italia,100);
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                latitudFinal = italia.latitude;
                longitudFinal = italia.longitude;
                agregarMarcadorIcono(italia, R.drawable.mountain);
                break;
            case 4:
                //paisOverlay.image(BitmapDescriptorFactory.fromResource(R.drawable.plane)).position(francia,100);
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                latitudFinal = francia.latitude;
                longitudFinal = francia.longitude;
                agregarMarcadorIcono(francia,R.drawable.plan);
                break;
            default:
                break;
        }
        if (seleccion != 0){
            //mMap.clear();
            //mMap.addGroundOverlay(paisOverlay);
            pintarRuta();
            if (isAlertDisplayed){
                messageAlert = obtenerDistancia(locationOrigen, locationDestino);
                showMessage(messageAlert);
            }
        }
        obtenerUbicacionActual();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private String  obtenerDistancia(Location locationOrigen, Location locationDestino) {
        double distancia = locationOrigen.distanceTo(locationDestino);
        distancia /= 1000;
        return getString(R.string.desde) + obtenerNombreUbicacion(latitudActual,longitudActual) + "\n\n" + getString(R.string.hasta) + obtenerNombreUbicacion(latitudFinal,longitudFinal) + "\n\n" + getString(R.string.hay) + distancia + getString(R.string.kilometro);
    }

    private String obtenerNombreUbicacion(double latitud, double longitud) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitud, longitud, 1);
            return addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0, locListener);
        latitudActual = location.getLatitude();
        longitudActual = location.getLongitude();
        posicionaActual = new LatLng(latitudActual, longitudActual);
        agregarMarcador(posicionaActual);
    }

    private void pintarRuta() {
        Polyline line = mMap.addPolyline(new PolylineOptions().add(new LatLng(latitudActual, longitudActual), new LatLng(latitudFinal, longitudFinal)).width(15).color(Color.BLUE).geodesic(true));
    }

    private void showMessage(String message) {
        // cuztomizando al mensaje
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.distancia);
        alert.setMessage(message);
        alert.setCancelable(false);

        // agregando un boton al mensaje
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isAlertDisplayed=false;
            }
        });

        // creando y mostrando alerta
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void agregarMarcador(LatLng location) {
        CameraUpdate ubicacion = CameraUpdateFactory.newLatLngZoom(location, 16);
        mMap.addMarker(new MarkerOptions().position(location).title(getString(R.string.actual)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mMap.animateCamera(ubicacion);
    }

    private void agregarMarcadorIcono(LatLng location, int icono) {
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(location, 16);
        mMap.addMarker(new MarkerOptions().position(location).title(getString(R.string.destino)).icon(BitmapDescriptorFactory.fromResource(icono)));
        mMap.animateCamera(miUbicacion);
    }

    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };
}