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
    private  SharedPreferences preferencias;
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

        cargarEventosSharePreferences(); // cargo los eventos del sensor de proximidad grabados previamente en el sharepreferences

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
            preferencias = getSharedPreferences("eventosSensorProximidad",Context.MODE_PRIVATE);
            editor = preferencias.edit();
            setEventosSensor.addAll(eventosSensor);
            System.out.println("cantidad de elementos del arraylist setEventosSensor:"+setEventosSensor.size());
            editor.putStringSet("eventosSensor",setEventosSensor);
            editor.commit(); // grabo el archivo
            System.out.println("estoy grabando en SharePreferences");

    }

    private void cargarEventosSharePreferences()
    {
            preferencias = getSharedPreferences("eventosSensorProximidad",Context.MODE_PRIVATE);
            setEventosSensor = preferencias.getStringSet("eventosSensor",null);

            System.out.println("cantidad de elementos del arraylist setEventosSensor:"+setEventosSensor.size());
            if(setEventosSensor!=null)
            {
                System.out.println("Estoy cargando el SharePreferences");
                eventosSensor.addAll(setEventosSensor);
                System.out.println("cantidad de elementos del arraylist eventosSensor:"+eventosSensor.size());
            }

            else
            {
                System.out.println("El archivo de preferencias no existe");
                setEventosSensor = new HashSet<String>();
            }



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