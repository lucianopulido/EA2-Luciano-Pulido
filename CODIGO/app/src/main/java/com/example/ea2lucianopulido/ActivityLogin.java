package com.example.ea2lucianopulido;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Calendar;


public class ActivityLogin extends AppCompatActivity{



    private static final int SINCONEXIONINTERNET = 100;
    private ConnectivityManager conexionLogin;
    private NetworkInfo informacionConexionLogin;
    private TextView estadoConexionLogin;
    private JSONObject paquete;
    private JSONObject paqueteEvento;
    private Button iniciarSesion;
    private EditText emailLogin;
    private EditText passwordLogin;
    private Button crearCuenta;
    private Intent intentLoginRegistro;
    private Drawable colorBordeLogin;
    private  ConexionHttpUrlConexion conexionHttpUrlConexionLogin;
    private  String urlLogin;
    private String headerJsonTipo;
    private String headerAuthorizationTipo;
    private String headerJsonDescripcion;
    private String headerAuthorizationDescripcion;
    private ConexionHttpUrlConexion conexionHttpUrlConexionEvento;
    private String urlEvento;
    private ConexionHilos conexionHilos;
    private Handler comunicadorHilos;
    private String token;
    private String token_refresh;
    private boolean estadoConexionInternet;
    private Intent estadoBateria;
    private IntentFilter intentFiltro;
    private int nivelBateria;
    private int escalaBateria;
    private float porcentajeBateria;
    private TextView vistaBateria;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// hago que la activity solo se muestre verticalmente cuando se gira el celular horizontal para no tener que reacomodar la interfaz grafica
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         emailLogin = (EditText) findViewById(R.id.inputEmailLogin);
         passwordLogin  = (EditText) findViewById(R.id.inputPasswordLogin);
         iniciarSesion = (Button) findViewById(R.id.botonIniciarSesion);
         crearCuenta = (Button) findViewById(R.id.boton_crear_cuenta);
         vistaBateria = (TextView)findViewById(R.id.textViewBateria);
         colorBordeLogin = emailLogin.getBackground();
         colorBordeLogin.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
         emailLogin.setBackground(colorBordeLogin);
         passwordLogin.setBackground(colorBordeLogin);
         estadoConexionLogin = (TextView) findViewById(R.id.estadoConexionLogin);

         Log.i("Eject","Eject onCreate Login");

         estadoConexionInternet = ConexionHttpUrlConexion.verificarConexion(ActivityLogin.this);
         verificarEstadoCargaBateria();

         if(estadoConexionInternet)
         {
             estadoConexionLogin.setText("conexion establecida");
         }
         else
         {
             estadoConexionLogin.setText("conexion no establecida");
         }

         paquete =  new JSONObject();
         paqueteEvento = new JSONObject();

         iniciarSesion.setOnClickListener(botonLoginListeners);
         crearCuenta.setOnClickListener(botonLoginListeners);
         comunicadorHilos = manejadorMensajesHiloPrincipal();




    }

    private void verificarEstadoCargaBateria()
    {
        intentFiltro = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        estadoBateria = registerReceiver(null,intentFiltro);
        nivelBateria = estadoBateria.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        escalaBateria = estadoBateria.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        porcentajeBateria = nivelBateria * 100 / (float)escalaBateria;
        vistaBateria.setText("bateria: %"+Float.toString(porcentajeBateria));

    }

    private View.OnClickListener botonLoginListeners = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.botonIniciarSesion:
                    try {
                        paquete.put("email",emailLogin.getText().toString());
                        paquete.put("password",passwordLogin.getText().toString());

                        paqueteEvento.put("env","PROD");
                        paqueteEvento.put("type_events","Ejecucion background");
                        paqueteEvento.put("description","hilo de login de usuario creado");

                        conexionHilos = new ConexionHilos();
                        conexionHilos.start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.boton_crear_cuenta:
                    intentLoginRegistro = new Intent(ActivityLogin.this,ActivityRegistro.class);
                    startActivity(intentLoginRegistro);
                    break;
            }



        }
    };

    private Handler manejadorMensajesHiloPrincipal()
    {
        return new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {

                         switch((int) msg.obj)
                         {
                             case HttpURLConnection.HTTP_OK:

                                     try {
                                         token = conexionHttpUrlConexionLogin.getPaqueteRecibido().getString("token");
                                         token_refresh = conexionHttpUrlConexionLogin.getPaqueteRecibido().getString("token_refresh");
                                         Intent intentLoginSensores = new Intent(ActivityLogin.this, ActivitySensores.class);
                                         intentLoginSensores.putExtra("token", token);
                                         intentLoginSensores.putExtra("token_refresh",token_refresh);
                                         startActivity(intentLoginSensores);
                                     } catch (JSONException e) {
                                         e.printStackTrace();
                                     }
                                 break;

                             case HttpURLConnection.HTTP_BAD_REQUEST:

                                     Toast mensajeError = Toast.makeText(ActivityLogin.this,"El email o la contraseña no son correctos. Vuelva a ingresar el email y la contraseña correctamente correctamente",Toast.LENGTH_LONG);
                                     mensajeError.setGravity(Gravity.CENTER,0,0);
                                     mensajeError.show();

                                 break;

                             case SINCONEXIONINTERNET:

                                 Toast mensajeErrorInternet = Toast.makeText(ActivityLogin.this,"Su dispositivo movil no esta conectado a internet. Debe conectar su dispositivo a internet para que la aplicacion funcione",Toast.LENGTH_LONG);
                                 mensajeErrorInternet.setGravity(Gravity.CENTER,0,0);
                                 mensajeErrorInternet.show();

                                 break;
                         }
            }
        };
    }
private class ConexionHilos extends  Thread
{
    @Override
    public void run()
    {
                headerJsonTipo = "content-type";
                headerJsonDescripcion = "application/json";
                headerAuthorizationTipo = "Authorization:";

                estadoConexionInternet = ConexionHttpUrlConexion.verificarConexion(ActivityLogin.this); // chequeo la conexion de nuevo para que si no tengo internet y hago la peticion no se cierre la app de repente
                if(estadoConexionInternet)
                {
                    urlLogin = "http://so-unlam.net.ar/api/api/login";
                    conexionHttpUrlConexionLogin = new ConexionHttpUrlConexion(urlLogin, paquete, headerJsonTipo, headerJsonDescripcion);

                    Message mensajeExitoso = new Message();
                    mensajeExitoso.obj = conexionHttpUrlConexionLogin.getRespuestaServidor();

                    urlEvento = "http://so-unlam.net.ar/api/api/event";// registro el evento del login en las proximas 2 lineas
                    headerAuthorizationDescripcion = conexionHttpUrlConexionLogin.getToken();
                    conexionHttpUrlConexionEvento = new ConexionHttpUrlConexion(urlEvento, paqueteEvento, headerJsonTipo, headerJsonDescripcion, headerAuthorizationTipo, headerAuthorizationDescripcion);

                    comunicadorHilos.sendMessage(mensajeExitoso); // pongo el mensaje en la cola del hilo principal para que transicione a la activitySensores

                }
                else
                {
                    Message mensajeErrorInternet = new Message();
                    mensajeErrorInternet.obj = SINCONEXIONINTERNET;
                    comunicadorHilos.sendMessage(mensajeErrorInternet);
                }
            }
}

    @Override
    protected void onStart() {
        Log.i("Eject","Eject onStart Login");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i("Eject","Eject onResume Login");
        super.onResume();
    }



    @Override
    protected void onPause() {
        Log.i("Eject","Eject onPause Login");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Eject","Eject onStop Login");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Eject","Eject onDestroy Login");
        super.onDestroy();
    }

}