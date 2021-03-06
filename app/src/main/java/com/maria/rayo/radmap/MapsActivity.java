package com.maria.rayo.radmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.lang.*;
import android.R.*;


import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;


import java.text.DecimalFormat;
import java.util.ArrayList;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private Marker markerprueba;
    public ArrayList<Antena> misAntenas;
    public Antena currentAntena;
    private FloatingActionButton quienButton;
    private FloatingActionButton usoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //requestPermissions();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        quienButton = (FloatingActionButton) findViewById(R.id.quien);
        usoButton = (FloatingActionButton) findViewById(R.id.instrucciones);



        quienButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), Quienes.class);
                startActivity(myIntent);

            }
        });

        usoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), Uso.class);
                startActivity(myIntent);

            }
        });



    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {


        mMap = googleMap;
        //mMap.getUiSettings().setZoomGesturesEnabled(false);
        //mMap.getUiSettings().setZoomControlsEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

        }


        LatLng miUbicacion = new LatLng(4.751328, -74.029998);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15));

        Antut(googleMap);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(mMap != null)
                    mMap.clear();

                DecimalFormat formato1 = new DecimalFormat("#.000");
                Log.i("CENTRO: ", String.valueOf(mMap.getCameraPosition().target.latitude) + " , " + String.valueOf(mMap.getCameraPosition().target.longitude));
                Toast.makeText(getApplicationContext(), "Calculando Potencias", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "Potencia Total: "+ (formato1.format(potenciaTotalP(latLng))), Toast.LENGTH_LONG).show();


                drawSquares(latLng.latitude, latLng.longitude);
                pintarAntenas(getAntenas());

            }



        });
    }

    public String KEY_OPENCELLID = "90a214f0f9935d";

    public void Antut(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng miUbicacion =  new LatLng (4.751328,-74.029998);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15));

        double ubicacionX=4.751328;
        double ubicacionY=-74.029998;

        Mylatlng miposicion = new Mylatlng(ubicacionX, ubicacionY);
        Cuadrados miCuadrado = new Cuadrados(miposicion);


        //calculo del punto inicial
        // posicion del cuadro respecto a mi posicion

        Mylatlng puntoInicio = new Mylatlng(miposicion.getLatitud()+miCuadrado.getDiagonal()*11, miposicion.getLongitud()-miCuadrado.getDiagonal()*6);

        Mylatlng puntoReferencia = puntoInicio;

        //Instanciamos la clase que maneja los colores
        Colors colors = new Colors();


        for(double j = 0; j<23; j++){

            for(double i = 0; i< 14; i++){
                //Establecer colores con una variable

                Cuadrados cuadradoActual = new Cuadrados(puntoReferencia);
                LatLng puntoReferenviaLatLon= new LatLng(puntoReferencia.getLatitud(),puntoReferencia.getLongitud());
                double potenciaCuadrado=potenciaTotalP(puntoReferenviaLatLon);
                Polygon polygon = mMap.addPolygon(cuadradoActual.getPolygonOptions());
                polygon.setStrokeWidth(0);
                polygon.setFillColor(colors.getColorCuadrado(potenciaCuadrado));

               // Log.i("potencia: ", String.valueOf(potenciaCuadrado));

                puntoReferencia = new Mylatlng(puntoReferencia.getLatitud(), puntoReferencia.getLongitud()+(cuadradoActual.getDiagonal()));
            }
            puntoReferencia = new Mylatlng(puntoReferencia.getLatitud()-((miCuadrado.getDiagonal())), puntoInicio.getLongitud());

        }

        // Creacion de los marcadores con las coordenadas de las antenas
        ArrayList<Antena> antenasList = getAntenas();

        pintarAntenas(getAntenas());
        googleMap.setOnMarkerClickListener(this);
    }

    public double calcularPotencia(LatLng posicion, LatLng locAntena) {
        double potencia = 0;
        int factor = new Integer(1000);




        //pasar latitudes y longitudes a metros

        double diferenciaLat = Math.abs(posicion.latitude - locAntena.latitude) ;
        double diferenciaLong =  Math.abs(posicion.longitude - locAntena.longitude);

        double distancia=Math.sqrt((diferenciaLat*diferenciaLat)+(diferenciaLong*diferenciaLong))*1000;
        //Log.i("Distancia ",String.valueOf(distancia));
        potencia=(1/(4*Math.PI*(distancia*distancia)))*factor;


        return potencia;
    }

    public double potenciaTotalP(LatLng posiscion){
        ArrayList<Antena> antenasList = getAntenas();

        double potenciaTotal=0;
        for(int i=0; i<antenasList.size(); i++ ){


            potenciaTotal= potenciaTotal+calcularPotencia(posiscion,antenasList.get(i).posicion());
           // Log.i(String.valueOf(i) + "  :  ", String.valueOf(potenciaTotal));

        }

        return potenciaTotal;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(markerprueba)){

            String latitud, longitud;
            latitud = Double.toString(marker.getPosition().latitude);
            longitud = Double.toString(marker.getPosition().longitude);

            // Al tocar una marca aparecera la logitud y la latitud
            Toast.makeText(this, "Latitud " + latitud + ", " + "Longittud " + longitud, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private ArrayList<Antena> getAntenas (){
        misAntenas = new ArrayList<Antena>();

        currentAntena = new Antena( -74.029701, "UMTS", 4.747425, 1000);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.036609, "UMTS", 4.756269, 40179);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.037845, "UMTS", 4.754475, 68302);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.027939, "GSM", 4.751587, 1164);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.025192, "UMTS", 4.75502, 1000);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.045622, "LTE", 4.767238, 1000);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.045722, "UMTS", 4.754625, 5512);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.039179, "UMTS", 4.766257, 1302);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.045622, "UMTS", 4.745195, 1190);
        misAntenas.add(currentAntena);
        currentAntena = new Antena( -74.027546, "UMTS", 4.75502, 1000);
        misAntenas.add(currentAntena);



        return misAntenas;
    }

    public void pintarAntenas (ArrayList<Antena> antenas){

        for(int i=0; i<antenas.size(); i++ ){
            double miLat=antenas.get(i).lat;
            double milong=antenas.get(i).lon;
            mMap.addMarker(new MarkerOptions().position(new LatLng(miLat, milong)).icon(BitmapDescriptorFactory.fromResource(R.drawable.antenamarcador2)));

        }
    }

    public void drawSquares (Double LAT, Double LON){

        Mylatlng miposicion = new Mylatlng(LAT, LON);
        Log.i("AQUI3", String.valueOf( miposicion));

        Cuadrados miCuadrado = new Cuadrados(miposicion);

        Mylatlng puntoInicio = new Mylatlng(miposicion.getLatitud()+miCuadrado.getDiagonal()*11, miposicion.getLongitud()-miCuadrado.getDiagonal()*6);
        Mylatlng puntoReferencia = puntoInicio;

        Log.i("AQUI4", String.valueOf( puntoInicio));

        Colors colors = new Colors();
        Integer colorPosition =  0;

        //Pintar cuadrados
        for(double j = 0; j< 23; j++){
            for(double i = 0; i< 14; i++){
                //Establecer colores con una variable
                Cuadrados cuadradoActual = new Cuadrados(puntoReferencia);
                LatLng puntoReferenviaLatLon= new LatLng(puntoReferencia.getLatitud(),puntoReferencia.getLongitud());
                double potenciaCuadrado=potenciaTotalP(puntoReferenviaLatLon);
                Polygon polygon = mMap.addPolygon(cuadradoActual.getPolygonOptions());
                polygon.setStrokeWidth(0);
                polygon.setFillColor(colors.getColorCuadrado(potenciaCuadrado));

                puntoReferencia = new Mylatlng(puntoReferencia.getLatitud(), puntoReferencia.getLongitud()+cuadradoActual.getDiagonal());
            }
            puntoReferencia = new Mylatlng(puntoReferencia.getLatitud()-(miCuadrado.getDiagonal()), puntoInicio.getLongitud());

        }
    }

   
}
