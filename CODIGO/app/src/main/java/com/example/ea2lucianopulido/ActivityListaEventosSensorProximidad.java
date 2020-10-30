package com.example.ea2lucianopulido;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ActivityListaEventosSensorProximidad extends AppCompatActivity {


    private RecyclerView listaEventos;
    private RecyclerView.LayoutManager layoutListaEventos;
    private Adaptador adaptador;
    private Bundle datosRecibidos;
    private Intent intent;
    private Button verSensores;
    private Intent listaEventosAcitivitySensores;
    private String token;
    private String token_refresh;
    private SharedPreferences preferencias;
    private SharedPreferences.Editor editor;
    private ArrayList <String> eventosSensor;
    private Set <String> setEventosSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_eventos_sensor_proximidad);

        listaEventos = (RecyclerView) findViewById(R.id.listaDeEventosUi);
        verSensores = (Button) findViewById(R.id.boton_sensores);
        listaEventos.setHasFixedSize(true);

        verSensores.setOnClickListener(botonesListeners);

        eventosSensor = new ArrayList<String>();
        setEventosSensor = new HashSet<String>();

        intent = getIntent();
        datosRecibidos = intent.getExtras();

        Log.i("Eject","Eject onCreate ActivityListaEventos");

        eventosSensor.addAll(datosRecibidos.getStringArrayList("eventosSensor"));
        token = datosRecibidos.getString("token");
        token_refresh = datosRecibidos.getString("token_refresh");


        adaptador = new Adaptador(this,eventosSensor);
        listaEventos.setAdapter(adaptador);
        layoutListaEventos = new LinearLayoutManager(this);
        listaEventos.setLayoutManager(layoutListaEventos);

    }

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            listaEventosAcitivitySensores = new Intent(ActivityListaEventosSensorProximidad.this,ActivitySensores.class);
            listaEventosAcitivitySensores.putExtra("token",token);
            listaEventosAcitivitySensores.putExtra("token_refresh",token_refresh);
            startActivity(listaEventosAcitivitySensores);
        }
    };

    private void guardarEventosSharePreferences()
    {

        if(!eventosSensor.isEmpty())
        {
            preferencias = PreferenceManager.getDefaultSharedPreferences(this);
            editor = preferencias.edit();
            setEventosSensor.addAll(eventosSensor);
            editor.putStringSet("eventosSensor",setEventosSensor);
            editor.commit(); // grabo el archivo
        }
    }

    private void cargarEventosSharePreferences()
    {
            preferencias = PreferenceManager.getDefaultSharedPreferences(this);
            setEventosSensor = preferencias.getStringSet("eventosSensor",null);

            if(setEventosSensor!=null)
            eventosSensor.addAll(setEventosSensor);
            else
                setEventosSensor = new HashSet<String>();
    }

    @Override
    protected void onStart() {
        Log.i("Eject","Eject onStart ActivityListaEventos");
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.i("Eject","Eject onResume ActivityListaEventos");
        super.onResume();
        cargarEventosSharePreferences(); // cargo los eventos del sensor de proximidad grabados previamente en el sharepreferences
    }

    @Override
    protected void onPause() {
        Log.i("Eject","Eject onPause ActivityListaEventos");
        super.onPause();
        guardarEventosSharePreferences();
    }

    @Override
    protected void onStop() {
        Log.i("Eject","Eject onStop ActivityListaEventos");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        Log.i("Eject","Eject onDestroy ActivityListaEventos");
        super.onDestroy();

    }

}