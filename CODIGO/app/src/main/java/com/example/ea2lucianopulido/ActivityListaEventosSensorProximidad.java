package com.example.ea2lucianopulido;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class ActivityListaEventosSensorProximidad extends AppCompatActivity {

    private ArrayList<String> idEvento;
    private ArrayList <String> descripcionEvento;
    private ArrayList <String> tipoEvento ;
    private ArrayList <String> dni;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_eventos_sensor_proximidad);

        listaEventos = (RecyclerView) findViewById(R.id.listaDeEventosUi);
        verSensores = (Button) findViewById(R.id.boton_sensores);
        listaEventos.setHasFixedSize(true);

        verSensores.setOnClickListener(botonesListeners);

        idEvento = new ArrayList<String>();
        tipoEvento = new ArrayList<String>();
        descripcionEvento = new ArrayList<String>();
        dni = new ArrayList<String>();

        intent = getIntent();
        datosRecibidos = intent.getExtras();

        Log.i("Eject","Eject onCreate ActivityListaEventos");

        idEvento = datosRecibidos.getStringArrayList("idEvento");
        tipoEvento = datosRecibidos.getStringArrayList("tipoEvento");
        descripcionEvento = datosRecibidos.getStringArrayList("descripcionEvento");
        dni = datosRecibidos.getStringArrayList("dni");
        token = datosRecibidos.getString("token");
        token_refresh = datosRecibidos.getString("token_refresh");

        adaptador = new Adaptador(this,tipoEvento,descripcionEvento,dni,idEvento);
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

    private void guardarPreferencias()
    {
      //  preferencias = getSharedPreferences();

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