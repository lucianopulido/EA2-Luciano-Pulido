package com.example.ea2lucianopulido;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


public class ActivitySensores extends AppCompatActivity implements SensorEventListener {
    private SensorManager administradorSensores;
    private Sensor acelerometro;
    private Sensor sensorProximidad;
    private TextView textFieldAcelerometro;
    private TextView textFieldVibracion;
    private TextView textFieldProximidad;
    private ConexionHttpUrlConexion conexionHttpUrlConexionEvento;
    private String headerJsonTipo;
    private String headerAuthorizationTipo;
    private String headerJsonDescripcion;
    private String headerAuthorizationDescripcion;
    private String urlEvento;
    private JSONObject paqueteEvento;
    private Bundle datosRecibidos;
    private Intent intent;
    private ConexionHilos conexionHilos;
    private Handler comunicadorHilos;
    private Message mensaje;
    private boolean estadoConexionInternet;
    private ConnectivityManager conexionSensores;
    private NetworkInfo informacionConexionSensores;
    private Calendar fechaYhoraActual;
    private Calendar fechaYhoraFinalizacionToken;
    private boolean enActivitySensores;
    private boolean sensorEstimulado;
    private long  tiempoActual;
    private long tiempoFinalizacionToken;
    private String token;
    private String token_refresh;
    private static final int SINCONEXIONINTERNET = 100;
    private static final int TOKENEXPIRADO = 101;
    private ArrayList <String> idEvento;
    private ArrayList <String> descripcionEvento;
    private ArrayList <String> tipoEvento ;
    private ArrayList <String> dni;
    private Button verEventosSensorProximidad;
    private Intent sensoresTokenRefresh;
    private Intent sensoresListaEventos;
    private JSONObject evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// hago que la activity solo se muestre verticalmente cuando se gira el celular horizontal para no tener que reacomodar la interfaz grafica
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensores);


        textFieldProximidad   = (TextView) findViewById(R.id.outputSensorProximidad);
        textFieldAcelerometro = (TextView) findViewById(R.id.outPutAcelerometro);
        textFieldVibracion = (TextView) findViewById(R.id.outPutVibracion);
        verEventosSensorProximidad = (Button) findViewById(R.id.boton_lista_eventos_sensor);

        idEvento = new ArrayList<String>();
        tipoEvento = new ArrayList<String>();
        descripcionEvento = new ArrayList<String>();
        dni = new ArrayList<String>();


        Intent intent = getIntent();
        datosRecibidos = intent.getExtras();
        token = datosRecibidos.getString("token");
        token_refresh = datosRecibidos.getString("token_refresh");

        System.out.println("token: "+token);


        fechaYhoraActual = Calendar.getInstance();
        fechaYhoraFinalizacionToken = Calendar.getInstance();
        fechaYhoraFinalizacionToken.add(fechaYhoraFinalizacionToken.MINUTE,30);
        tiempoActual = fechaYhoraActual.getTimeInMillis();
        tiempoFinalizacionToken = fechaYhoraFinalizacionToken.getTimeInMillis();

        enActivitySensores = true;

        conexionHilos = new ConexionHilos();
        conexionHilos.start();

        verEventosSensorProximidad.setOnClickListener(botonesListeners);

        administradorSensores = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = administradorSensores.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorProximidad = administradorSensores.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        paqueteEvento = new JSONObject();
        comunicadorHilos = manejadorMensajesHiloPrincipal();


    }

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            sensoresListaEventos = new Intent(ActivitySensores.this,ActivityListaEventosSensorProximidad.class);
            sensoresListaEventos.putExtra("idEvento",idEvento);
            sensoresListaEventos.putExtra("tipoEvento",tipoEvento);
            sensoresListaEventos.putExtra("descripcionEvento",descripcionEvento);
            sensoresListaEventos.putExtra("token",token);
            sensoresListaEventos.putExtra("token_refresh",token_refresh);
            sensoresListaEventos.putExtra("dni",dni);
            startActivity(sensoresListaEventos);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        String texto="";

        synchronized (this)
        {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    texto += "Acelerometro:"+"\n";
                    texto += "x: "   + event.values[0] + "\n";
                    texto += "y: "   + event.values[1] + "\n";
                    texto += "z: "   + event.values[2] + "\n";
                    textFieldAcelerometro.setText(texto);

                    if( event.values[0] > 25 || event.values[1] > 25 || event.values[2] > 25 )
                    {
                        textFieldVibracion.setText("Vibracion Detectada");
                    }
                    break;
                case Sensor.TYPE_PROXIMITY :
                    texto += "Sensor de proximidad" + "\n";
                    texto += "Distancia: "+ event.values[0] + "cm\n";

                    if(event.values[0] < sensorProximidad.getMaximumRange())
                    {
                        textFieldProximidad.setText(texto);
                        sensorEstimulado = true;
                    }
                    else
                    {
                        textFieldProximidad.setText(texto);
                    }
                    break;
            }
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

                        sensoresTokenRefresh = new Intent(ActivitySensores.this,ActivityTokenRefresh.class);
                        sensoresTokenRefresh.putExtra("token_refresh",token_refresh);
                        startActivity(sensoresTokenRefresh);

                        break;

                    case SINCONEXIONINTERNET:

                        Toast mensajeErrorInternet = Toast.makeText(ActivitySensores.this,"Su dispositivo movil no esta conectado a internet. Debe conectar su dispositivo a internet para que la aplicacion funcione correctamente",Toast.LENGTH_LONG);
                        mensajeErrorInternet.setGravity(Gravity.CENTER,0,0);
                        mensajeErrorInternet.show();

                        break;
                }
            }
        };
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
    public void verificarConexion()
    {
        conexionSensores = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // obtengo caracteristicas actuales de la conexion
        informacionConexionSensores = conexionSensores.getActiveNetworkInfo(); // guardo las la informacion de las caracteristicas actuales de la conexion

        if( informacionConexionSensores != null && informacionConexionSensores.isConnected())// verifico que mi dispositivo esta conectado a internet
        {
            estadoConexionInternet = true;
        }
        else
        {
            estadoConexionInternet = false;
        }

    }

    private class ConexionHilos extends Thread
    {


        @Override
        public void run()
        {

            while (enActivitySensores)
            {
                    fechaYhoraActual = Calendar.getInstance();
                    tiempoActual = fechaYhoraActual.getTimeInMillis();

                    verificarConexion(); // chequeo la conexion de nuevo para que si no tengo internet y hago la peticion no se cierre la app de repente
                    if (estadoConexionInternet)
                    {
                        if (tiempoActual == tiempoFinalizacionToken)
                        {
                            mensaje = new Message();
                            mensaje.obj = TOKENEXPIRADO;
                            comunicadorHilos.sendMessage(mensaje);
                        }
                        if(sensorEstimulado)
                        {
                            try {
                                sensorEstimulado = false;
                                paqueteEvento.put("env", "PROD");
                                paqueteEvento.put("type_events", "sensor");
                                paqueteEvento.put("description", "sensor proximidad");

                                urlEvento = "http://so-unlam.net.ar/api/api/event";

                                headerJsonTipo = "content-type";
                                headerJsonDescripcion = "application/json";
                                headerAuthorizationTipo = "Authorization:";
                                headerAuthorizationDescripcion = token;

                                conexionHttpUrlConexionEvento = new ConexionHttpUrlConexion(urlEvento, paqueteEvento, headerJsonTipo, headerJsonDescripcion, headerAuthorizationTipo, headerAuthorizationDescripcion);

                                evento = conexionHttpUrlConexionEvento.getPaqueteRecibido().getJSONObject("event"); // obtengo y guardo el json para listar el evento
                                tipoEvento.add(evento.getString("type_events"));
                                dni.add(evento.getString("dni"));
                                descripcionEvento.add(evento.getString("description"));
                                idEvento.add(evento.getString("id"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Message mensajeErrorInternet = new Message();
                        mensajeErrorInternet.obj = SINCONEXIONINTERNET;
                        comunicadorHilos.sendMessage(mensajeErrorInternet);
                    }
            }
        }

    }

    @Override
    protected void onStart() {
        Log.i("Eject","Eject onStart ActivitySensores");
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.i("Eject","Eject onResume ActivitySensores");
        super.onResume();
        administradorSensores.registerListener(this,acelerometro,SensorManager.SENSOR_DELAY_NORMAL);
        administradorSensores.registerListener(this, sensorProximidad,SensorManager.SENSOR_DELAY_NORMAL);
    }



    @Override
    protected void onPause() {
        Log.i("Eject","Eject onPause ActivitySensores");
        super.onPause();
        administradorSensores.unregisterListener(this,administradorSensores.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        administradorSensores.unregisterListener(this,administradorSensores.getDefaultSensor(Sensor.TYPE_PROXIMITY));


        }

    @Override
    protected void onStop() {
        Log.i("Eject","Eject onStop ActivitySensores");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Eject","Eject onDestroy ActivitySensores");
        super.onDestroy();
        enActivitySensores = false; // libero los hilos
    }
}