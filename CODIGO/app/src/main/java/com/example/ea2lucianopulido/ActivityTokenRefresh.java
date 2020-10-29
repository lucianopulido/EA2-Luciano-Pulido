package com.example.ea2lucianopulido;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.net.HttpURLConnection;

public class ActivityTokenRefresh extends AppCompatActivity {

    private Button seguirUsandoAplicacion;
    private Button cerrarSesion;
    private Intent tokenRefreshLogin;
    private Intent tokenRefreshSensores;
    private ConexionHilos conexionHilos;
    private String urlApiTokenRefresh;
    private String token_refresh;
    private String token;
    private String headerJsonTipo;
    private String headerAuthorizationTipo;
    private String headerJsonDescripcion;
    private String headerAuthorizationDescripcion;
    private ConexionHttpUrlConexion conexionHttpUrlConexionToken;
    private Handler comunicadorHilos;
    private Bundle datosRecibidos;
    private Intent intent;
    private boolean estadoConexionInternet;
    private ConnectivityManager conexionSensores;
    private NetworkInfo informacionConexionSensores;
    private static final int SINCONEXIONINTERNET = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// hago que la activity solo se muestre verticalmente cuando se gira el celular horizontal para no tener que reacomodar la interfaz grafica
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_refresh);

        intent = getIntent();
        datosRecibidos = intent.getExtras();
        token_refresh = datosRecibidos.getString("token_refresh");


        Log.i("Eject","Eject onCreate ActivityTokenRefresh");

        seguirUsandoAplicacion = (Button) findViewById(R.id.boton_seguir);
        cerrarSesion = (Button) findViewById(R.id.boton_cerrar_sesion);

        seguirUsandoAplicacion.setOnClickListener(botonLoginListeners);
        cerrarSesion.setOnClickListener(botonLoginListeners);
        comunicadorHilos = manejadorMensajesHiloPrincipal();

    }

    private View.OnClickListener botonLoginListeners = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.boton_seguir:

                        tokenRefreshSensores = new Intent(ActivityTokenRefresh.this,ActivitySensores.class);
                        conexionHilos = new ConexionHilos();
                        conexionHilos.start();

                    break;
                case R.id.boton_cerrar_sesion:

                    tokenRefreshLogin = new Intent(ActivityTokenRefresh.this,ActivityLogin.class);
                    startActivity(tokenRefreshLogin);

                    break;
            }
        }
    };

    private Handler manejadorMensajesHiloPrincipal()
    {
        return new Handler(Looper.getMainLooper())
        {
            public void handleMessage(@NonNull Message msg)
            {
                switch ( (int) msg.obj)
                {
                    case HttpURLConnection.HTTP_OK:

                        try {
                            token = conexionHttpUrlConexionToken.getPaqueteRecibido().getString("token");
                            token_refresh = conexionHttpUrlConexionToken.getPaqueteRecibido().getString("token_refresh");
                            tokenRefreshSensores.putExtra("token",token);
                            tokenRefreshSensores.putExtra("token_refresh",token_refresh);
                            startActivity(tokenRefreshSensores);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case SINCONEXIONINTERNET:

                        Toast mensajeErrorInternet = Toast.makeText(ActivityTokenRefresh.this,"Su dispositivo movil no esta conectado a internet. Debe conectar su dispositivo a internet para que la aplicacion funcione correctamente",Toast.LENGTH_LONG);
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

           estadoConexionInternet = ConexionHttpUrlConexion.verificarConexion(ActivityTokenRefresh.this);
            if(estadoConexionInternet)
            {
                urlApiTokenRefresh = "http://so-unlam.net.ar/api/api/refresh";

                headerJsonTipo = "content-type";
                headerJsonDescripcion = "application/json";
                headerAuthorizationTipo = "Authorization:";
                headerAuthorizationDescripcion = token_refresh;

                conexionHttpUrlConexionToken = new ConexionHttpUrlConexion(urlApiTokenRefresh, headerJsonTipo, headerJsonDescripcion, headerAuthorizationTipo, headerAuthorizationDescripcion);

                Message mensaje = new Message();
                mensaje.obj = conexionHttpUrlConexionToken.getRespuestaServidor();
                comunicadorHilos.sendMessage(mensaje);
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
        Log.i("Eject","Eject onStart ActivityTokenRefresh");
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        Log.i("Eject","Eject onResume ActivityTokenRefresh");
        super.onResume();
    }



    @Override
    protected void onPause() {
        Log.i("Eject","Eject onPause ActivityTokenRefresh");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Eject","Eject onStop ActivityTokenRefresh");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Eject","Eject onDestroy ActivityTokenRefresh");
        super.onDestroy();

    }
}