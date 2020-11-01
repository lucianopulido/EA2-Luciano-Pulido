package com.example.ea2lucianopulido;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class ConexionHttpUrlConexion {

    private HttpURLConnection peticion;
    private URL link;
    private JSONObject paquete;
    private JSONObject paqueteRecibido;
    private DataOutputStream datosSalida;
    private String url;
    private int respuestaServidor;
    private String headerJsonTipo;
    private String headerAuthorizationTipo;
    private String headerJsonDescripcion;
    private String headerAuthorizationDescripcion;
    private static String token;
    private static String token_refresh;
    private static boolean tiempoFinalizacionTokenCreado = false;
    private static ConnectivityManager conexion;
    private static NetworkInfo informacionConexion;
    private static boolean estadoConexionInternet;
    private static String activity;


    public ConexionHttpUrlConexion(String url, JSONObject paquete, String headerJsonTipo, String headerJsonDescripcion) {
        this.paquete = paquete;
        this.url = url;
        this.headerJsonTipo = headerJsonTipo;
        this.headerJsonDescripcion = headerJsonDescripcion;

        try {
            link = new URL(this.url);
            peticion = (HttpURLConnection) link.openConnection(); // abro una conexion
            peticion.setDoInput(true);
            peticion.setDoOutput(true);
            peticion.setRequestMethod("POST");// seteo el tipo de peticion
            peticion.setRequestProperty(this.headerJsonTipo, this.headerJsonDescripcion); // configuro el header
            datosSalida = new DataOutputStream(peticion.getOutputStream());
            datosSalida.writeBytes(this.paquete.toString());

            peticion.connect();// envio la consulta
            respuestaServidor = peticion.getResponseCode(); // obtengo la respuesta
            analizarRespuestaServidor(respuestaServidor, peticion);
            datosSalida.flush();
            datosSalida.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            peticion.disconnect();
        }
    }

    public ConexionHttpUrlConexion(String url, JSONObject paquete, String headerJsonTipo, String headerJsonDescripcion, String headerAuthorizationTipo, String headerAuthorizationDescripcion) {
        this.paquete = paquete;
        this.url = url;
        this.headerJsonTipo = headerJsonTipo;
        this.headerJsonDescripcion = headerJsonDescripcion;
        this.headerAuthorizationTipo = headerAuthorizationTipo;
        this.headerAuthorizationDescripcion = headerAuthorizationDescripcion;


        try {
            link = new URL(this.url);
            peticion = (HttpURLConnection) link.openConnection(); // abro una conexion
            peticion.setDoInput(true);
            peticion.setDoOutput(true);
            peticion.setRequestMethod("POST");// seteo el tipo de peticion
            peticion.setRequestProperty(this.headerJsonTipo, this.headerJsonDescripcion); // configuro el header
            peticion.setRequestProperty(this.headerAuthorizationTipo, this.headerAuthorizationDescripcion);
            datosSalida = new DataOutputStream(peticion.getOutputStream());
            datosSalida.writeBytes(this.paquete.toString());

            peticion.connect();// envio la consulta
            respuestaServidor = peticion.getResponseCode(); // obtengo la respuesta
            analizarRespuestaServidor(respuestaServidor, peticion); // identifico y seteo el tipo de respuesta

            datosSalida.flush();
            datosSalida.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            peticion.disconnect();
        }
    }


    public ConexionHttpUrlConexion(String url, String headerJsonTipo, String headerJsonDescripcion, String headerAuthorizationTipo, String headerAuthorizationDescripcion) {
        this.url = url;
        this.headerJsonTipo = headerJsonTipo;
        this.headerJsonDescripcion = headerJsonDescripcion;
        this.headerAuthorizationTipo = headerAuthorizationTipo;
        this.headerAuthorizationDescripcion = headerAuthorizationDescripcion;

        try {
            link = new URL(this.url);
            peticion = (HttpURLConnection) link.openConnection(); // abro una conexion
            peticion.setDoInput(true);
            peticion.setDoOutput(true);
            peticion.setRequestMethod("PUT");// seteo el tipo de peticion
            peticion.setRequestProperty(this.headerJsonTipo, this.headerJsonDescripcion); // configuro el header
            peticion.setRequestProperty(this.headerAuthorizationTipo, this.headerAuthorizationDescripcion);

            peticion.connect();// envio la consulta
            respuestaServidor = peticion.getResponseCode(); // obtengo la respuesta
            analizarRespuestaServidor(respuestaServidor, peticion); // identifico y seteo el tipo de respuesta

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            peticion.disconnect();
        }
    }

    private void convertInputToString(InputStreamReader inputStream) throws IOException, JSONException {

        BufferedReader leer = new BufferedReader(inputStream);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = leer.readLine()) != null) {
            sb.append(line);

        }
        leer.close();
        paqueteRecibido = new JSONObject(sb.toString());
    }

    private void analizarRespuestaServidor(int respuestaServidor, HttpURLConnection peticion) throws IOException, JSONException {


        if (respuestaServidor == HttpURLConnection.HTTP_OK) {
            convertInputToString(new InputStreamReader(peticion.getInputStream()));
            setToken(getPaqueteRecibido().getString("token"));
            setToken(getPaqueteRecibido().getString("token_refresh"));
            setRespuestaServidor(respuestaServidor);
        }
        if (respuestaServidor == HttpURLConnection.HTTP_CREATED) {
            convertInputToString(new InputStreamReader(peticion.getInputStream()));
            setRespuestaServidor(respuestaServidor);
        }
        if (respuestaServidor == HttpURLConnection.HTTP_BAD_REQUEST) {
            convertInputToString(new InputStreamReader(peticion.getErrorStream()));
            setRespuestaServidor(respuestaServidor);
        }

    }

    protected static boolean verificarConexion(Context contexto) {
        String nombreContexto = Context.CONNECTIVITY_SERVICE;
        conexion = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE); // obtengo caracteristicas actuales de la conexion
        informacionConexion = conexion.getActiveNetworkInfo(); // guardo las la informacion de las caracteristicas actuales de la conexion

        if (informacionConexion != null && informacionConexion.isConnected()) // verifico que mi dispositivo esta conectado a internet
            estadoConexionInternet = true;
        else
            estadoConexionInternet = false;


        return estadoConexionInternet;
    }

    public int getRespuestaServidor() {
        return respuestaServidor;
    }

    public void setRespuestaServidor(int respuestaServidor) {
        this.respuestaServidor = respuestaServidor;
    }

    public HttpURLConnection getPeticion() {
        return peticion;
    }

    public JSONObject getPaqueteRecibido() {
        return paqueteRecibido;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken_refresh() {
        return token_refresh;
    }

    public void setToken_refresh(String token_refresh) {
        this.token_refresh = token_refresh;
    }

    public static boolean isTiempoFinalizacionTokenCreado() {
        return tiempoFinalizacionTokenCreado;
    }

    public static void setTiempoFinalizacionTokenCreado(boolean tiempoFinalizacionTokenCreado) {
        ConexionHttpUrlConexion.tiempoFinalizacionTokenCreado = tiempoFinalizacionTokenCreado;
    }

    public  static  String getActivity() {
        return activity;
    }

    public static  void  setActivity(String activity) {
        ConexionHttpUrlConexion.activity = activity;
    }
}
