package com.example.ea2lucianopulido;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
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
    private ArrayList <String> eventosSensor;
    private Set <String> setEventosSensor;
    private SharedPreferences.Editor editor;
    private boolean shpLimpio;
    private long tiempoFinalizacionToken;
    private long tiempoActual;
    private Calendar fechaYhoraActual;
    private ConexionHilos conexionHilos;
    private boolean enActivityListaEventosSensorProximidad;
    private boolean estadoConexionInternet;
    private Message mensaje;
    private static final int SINCONEXIONINTERNET = 100;
    private static final int TOKENEXPIRADO = 101;
    private Handler comunicadorHilos;
    private Intent sensoresTokenRefresh;
    private Calendar fechaYhoraFinalizacionToken;

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

        enActivityListaEventosSensorProximidad = true;

        intent = getIntent();
        datosRecibidos = intent.getExtras();

        Log.i("Eject","Eject onCreate ActivityListaEventos");

        eventosSensor.addAll(datosRecibidos.getStringArrayList("eventosSensor"));
        token = datosRecibidos.getString("token");
        token_refresh = datosRecibidos.getString("token_refresh");
        tiempoFinalizacionToken = datosRecibidos.getLong("tiempoFinalizacionToken");

        adaptador = new Adaptador(this,eventosSensor);
        listaEventos.setAdapter(adaptador);
        layoutListaEventos = new LinearLayoutManager(this);
        listaEventos.setLayoutManager(layoutListaEventos);

        fechaYhoraActual = Calendar.getInstance();
        tiempoActual = fechaYhoraActual.getTimeInMillis();
        comunicadorHilos = manejadorMensajesHiloPrincipal();


    }

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            listaEventosAcitivitySensores = new Intent(ActivityListaEventosSensorProximidad.this,ActivitySensores.class);
            listaEventosAcitivitySensores.putExtra("token",token);
            listaEventosAcitivitySensores.putExtra("token_refresh",token_refresh);
            listaEventosAcitivitySensores.putExtra("tiempoFinalizacionToken",tiempoFinalizacionToken);
            ConexionHttpUrlConexion.setTiempoFinalizacionTokenCreado(true);
            startActivity(listaEventosAcitivitySensores);
        }
    };

    private void guardarEventosSharePreferences()
    {

            preferencias = getSharedPreferences("eventosSensorProximidad",Context.MODE_PRIVATE);
            editor = preferencias.edit();
            setEventosSensor.addAll(eventosSensor);

            if(!shpLimpio)
            {
                editor.clear(); // si tiene algo borro lo que tenia y luego lo vuelvo a escribir porque sino no puedo actualizar el sharepreferences porque es inmutable
            }

            editor.putStringSet("eventosSensor",setEventosSensor);
            editor.commit(); // grabo el archivo
    }

    private void cargarEventosSharePreferences()
    {
            preferencias = getSharedPreferences("eventosSensorProximidad",Context.MODE_PRIVATE);
            setEventosSensor = preferencias.getStringSet("eventosSensor",null);

            if(setEventosSensor!=null)
            {
                eventosSensor.addAll(setEventosSensor);
            }
            else
            {
                shpLimpio = true;
                setEventosSensor = new HashSet<String>();
            }
    }

    private Handler manejadorMensajesHiloPrincipal()
    {
        return new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg)
            {
                switch ((int)msg.obj)
                {
                    case TOKENEXPIRADO:

                        sensoresTokenRefresh = new Intent(ActivityListaEventosSensorProximidad.this,ActivityTokenRefresh.class);
                        sensoresTokenRefresh.putExtra("token_refresh",token_refresh);
                        sensoresTokenRefresh.putExtra("eventosSensor",eventosSensor);
                        ConexionHttpUrlConexion.setActivity("ActivityListaEventosSensorProximidad");
                        startActivity(sensoresTokenRefresh);

                        break;

                    case SINCONEXIONINTERNET:

                        Toast mensajeErrorInternet = Toast.makeText(ActivityListaEventosSensorProximidad.this,"Su dispositivo movil no esta conectado a internet. Debe conectar su dispositivo a internet para que la aplicacion funcione correctamente",Toast.LENGTH_LONG);
                        mensajeErrorInternet.setGravity(Gravity.CENTER,0,0);
                        mensajeErrorInternet.show();

                        break;
                }
            }
        };
    }

    private class ConexionHilos extends Thread
    {
        @Override
        public void run()
        {

            while (enActivityListaEventosSensorProximidad)
            {
                fechaYhoraActual = Calendar.getInstance();
                tiempoActual = fechaYhoraActual.getTimeInMillis();

                estadoConexionInternet = ConexionHttpUrlConexion.verificarConexion(ActivityListaEventosSensorProximidad.this); // chequeo la conexion de nuevo para que si no tengo internet y hago la peticion no se cierre la app de repente
                if (estadoConexionInternet)
                {
                        if (tiempoActual == tiempoFinalizacionToken)
                        {
                            mensaje = new Message();
                            mensaje.obj = TOKENEXPIRADO;
                            comunicadorHilos.sendMessage(mensaje);
                        }

                }
                else
                {
                    Message mensajeErrorInternet = new Message();
                    mensajeErrorInternet.obj = SINCONEXIONINTERNET;
                    comunicadorHilos.sendMessage(mensajeErrorInternet);
                }
            }
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
        cargarEventosSharePreferences(); // cargo los eventos del sensor de proximidad grabados previamente en el sharepreferences

        if(!ConexionHttpUrlConexion.isTiempoFinalizacionTokenCreado())
        {
            fechaYhoraFinalizacionToken = Calendar.getInstance();
            fechaYhoraFinalizacionToken.add(fechaYhoraFinalizacionToken.MINUTE,30);
            tiempoFinalizacionToken = fechaYhoraFinalizacionToken.getTimeInMillis();
        }
        else
        {
            tiempoFinalizacionToken = datosRecibidos.getLong("tiempoFinalizacionToken");
        }

        conexionHilos = new ConexionHilos();
        conexionHilos.start();
    }

    @Override
    protected void onPause() {
        Log.i("Eject","Eject onPause ActivityListaEventos");
        super.onPause();
        guardarEventosSharePreferences();
        enActivityListaEventosSensorProximidad = false; // libero el hilo
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