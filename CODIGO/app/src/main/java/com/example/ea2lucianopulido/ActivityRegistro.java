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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ActivityRegistro extends AppCompatActivity
{
    private Button registrarse;
    private EditText nombre;
    private EditText apellido;
    private EditText dni;
    private EditText comision;
    private EditText email;
    private EditText password;
    private JSONObject paqueteRegistro;
    private JSONObject paqueteEvento;
    private ConnectivityManager conexion;
    private NetworkInfo informacionConexion;
    private TextView estadoConexion;
    private Intent intent;
    private Drawable colorBordeRegistro;
    private String urlRegistro;
    private String urlEvento;
    private ConexionHttpUrlConexion conexionHttpUrlConexionRegistro;
    private ConexionHttpUrlConexion conexionHttpUrlConexionEvento;
    private String headerJsonTipo;
    private String headerAuthorizationTipo;
    private String headerJsonDescripcion;
    private String headerAuthorizationDescripcion;
    private Handler comunicadorHilos;
    private ConexionHilos conexionHilos;
    private String token;
    private String error;
    private Toast mensajeError;
    private Button volverLogin;
    private boolean estadoConexionInternet;
    private static final int SINCONEXIONINTERNET = 100;
    private String token_refresh;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // hago que la activity solo se muestre verticalmente cuando se gira el celular horizontal para no tener que reacomodar la interfaz grafica
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        registrarse = (Button) findViewById(R.id.boton_registrarse);
        volverLogin = (Button) findViewById(R.id.botonRegresarLogin);
        nombre = (EditText) findViewById(R.id.inputNombre);
        apellido = (EditText) findViewById(R.id.inputApellido);
        dni = (EditText) findViewById(R.id.inputDni);
        email = (EditText) findViewById(R.id.inputEmail);
        password = (EditText) findViewById(R.id.inputPassword);
        comision = (EditText) findViewById(R.id.inputComision);

        colorBordeRegistro = email.getBackground();
        colorBordeRegistro.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        nombre.setBackground(colorBordeRegistro);
        apellido.setBackground(colorBordeRegistro);
        dni.setBackground(colorBordeRegistro);
        email.setBackground(colorBordeRegistro);
        password.setBackground(colorBordeRegistro);
        comision.setBackground(colorBordeRegistro);


        verificarConexion(); // verifico si estoy conectado a internet


        paqueteRegistro = new JSONObject();// creo objeto JSON para parsear la informacion que envio o recibo
        paqueteEvento = new JSONObject();



        registrarse.setOnClickListener(botonesListeners);
        volverLogin.setOnClickListener(botonesListeners);
        comunicadorHilos = manejadorMensajesHiloPrincipal();

        Log.i("Eject","Eject onCreate Registro");
    }

    private View.OnClickListener botonesListeners = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.boton_registrarse:

                    try
                    {
                        paqueteRegistro.put("env","PROD");
                        paqueteRegistro.put("name",nombre.getText().toString());
                        paqueteRegistro.put("lastname",apellido.getText().toString());
                        paqueteRegistro.put("dni",Integer.parseInt(dni.getText().toString()));
                        paqueteRegistro.put("email",email.getText().toString());
                        paqueteRegistro.put("password",password.getText().toString());
                        paqueteRegistro.put("commission",Integer.parseInt(comision.getText().toString()));

                        paqueteEvento.put("env","PROD");
                        paqueteEvento.put("type_events","Ejecucion background");
                        paqueteEvento.put("description","hilo de registro de usuario creado");

                        conexionHilos = new ConexionHilos();// creo hilo para que se quede escuchando la respuesta del servidor para realizar un registro
                        conexionHilos.start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case R.id.botonRegresarLogin:
                    Intent registroLogin  = new Intent(ActivityRegistro.this,ActivityLogin.class);
                    startActivity(registroLogin);
                    break;
            }

        }
    };

    private void verificarConexion()
    {
        estadoConexion = (TextView) findViewById(R.id.estadoConexion);

        conexion = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // obtengo caracteristicas actuales de la conexion
        informacionConexion = conexion.getActiveNetworkInfo(); // guardo las la informacion de las caracteristicas actuales de la conexion

        if( informacionConexion != null && informacionConexion.isConnected()) // verifico que mi dispositivo esta conectado a internet
        {
            estadoConexionInternet = true;
            estadoConexion.setText("estado de conexion: establecida");
        }

        else
        {
            estadoConexionInternet = false;
            estadoConexion.setText("estado de conexion: sin conexion");
        }


    }




    private Handler manejadorMensajesHiloPrincipal()
    {
        return new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg)
            {
                    switch ((int)msg.obj)
                    {
                        case HttpURLConnection.HTTP_OK:


                            try {
                                Intent intentRegistroSensores = new Intent(ActivityRegistro.this, ActivitySensores.class);
                                token = conexionHttpUrlConexionRegistro.getPaqueteRecibido().getString("token");
                                token_refresh = conexionHttpUrlConexionRegistro.getPaqueteRecibido().getString("token");
                                intentRegistroSensores.putExtra("token", token);
                                intentRegistroSensores.putExtra("token_refresh",token_refresh);
                                startActivity(intentRegistroSensores);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            break;

                        case HttpURLConnection.HTTP_BAD_REQUEST:

                            try {
                                error = conexionHttpUrlConexionRegistro.getPaqueteRecibido().getString("msg");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            switch (error)
                            {
                                case "El DNI del usuario ya fu\u00e9 registrado":

                                    mensajeError = Toast.makeText(ActivityRegistro.this,"El dni ingresado ya se encuentra registrado. Ingrese otro dni o inicie sesion con el email y contraseña asociado a su dni",Toast.LENGTH_LONG);
                                    mensajeError.setGravity(Gravity.CENTER,0,0);
                                    mensajeError.show();

                                    break;
                                case "El mail del usuario ya fu\u00e9 registrado":
                                    mensajeError = Toast.makeText(ActivityRegistro.this,"El dni ingresado ya se encuentra registrado. Ingrese otro dni o inicie sesion con el email y contraseña asociado a su dni",Toast.LENGTH_LONG);
                                    mensajeError.setGravity(Gravity.CENTER,0,0);
                                    mensajeError.show();
                                    break;

                                case "Error en los par\u00e1metros enviados":
                                    mensajeError = Toast.makeText(ActivityRegistro.this,"La contraseña ingresada debe tener 8 caracteres de largo como minimo",Toast.LENGTH_LONG);
                                    mensajeError.setGravity(Gravity.CENTER,0,0);
                                    mensajeError.show();
                                    break;
                            }


                            break;

                        case SINCONEXIONINTERNET:

                            Toast mensajeErrorInternet = Toast.makeText(ActivityRegistro.this,"Su dispositivo movil no esta conectado a internet. Debe conectar su dispositivo a internet para que la aplicacion funcione correctamente",Toast.LENGTH_LONG);
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
        public void run ()
        {

            headerJsonTipo = "content-type";
            headerJsonDescripcion = "application/json";
            headerAuthorizationTipo = "Authorization:";

            verificarConexion(); // chequeo la conexion de nuevo para que si no tengo internet y hago la peticion no se cierre la app de repente
            if (estadoConexionInternet)
            {
                urlRegistro = "http://so-unlam.net.ar/api/api/register";
                conexionHttpUrlConexionRegistro = new ConexionHttpUrlConexion(urlRegistro, paqueteRegistro, headerJsonTipo, headerJsonDescripcion);

                Message mensajeExitoso = new Message();
                mensajeExitoso.obj = conexionHttpUrlConexionRegistro.getRespuestaServidor();

                urlEvento = "http://so-unlam.net.ar/api/api/event"; // registro el evento del registro en las proximas 2 lineas
                headerAuthorizationDescripcion += conexionHttpUrlConexionRegistro.getToken();
                conexionHttpUrlConexionEvento   = new ConexionHttpUrlConexion(urlEvento, paqueteEvento, headerJsonTipo, headerJsonDescripcion, headerAuthorizationTipo, headerAuthorizationDescripcion);

                comunicadorHilos.sendMessage(mensajeExitoso);
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
        Log.i("Eject","Eject onStart Registro");
        super.onStart();
    }

    @Override
    protected void onResume() {

        Log.i("Eject","Eject onResume Registro");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("Eject","Eject onPause Registro");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i("Eject","Eject onStop Registro");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Eject","Eject onDestroy Registro");
        super.onDestroy();
    }

}
