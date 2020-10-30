package com.example.ea2lucianopulido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public  class Adaptador extends  RecyclerView.Adapter<Adaptador.ViewHolder>
{
    ArrayList<String> idEvento;
    ArrayList<String> descripcionEvento;
    ArrayList<String> tipoEvento;
    ArrayList<String> dni;
    ArrayList <String> eventosSensor;
    Context contexto;
    LayoutInflater inflador;

    public Adaptador(Context contexto, ArrayList <String> eventosSensor) {

        this.contexto = contexto;
        this.eventosSensor = eventosSensor;
        inflador = (LayoutInflater) this.contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = inflador.inflate(R.layout.elemento_lista_eventos_sensor, null);
        ViewHolder vh = new ViewHolder(vista);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.eventosSensor.setText(this.eventosSensor.get(position));
    }

    @Override
    public int getItemCount() {
        return eventosSensor.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView eventosSensor;
        public ViewHolder(@NonNull View itemVista) {
            super(itemVista);
            this.eventosSensor = (TextView) itemVista.findViewById(R.id.textViewEventosSensor);
        }
    }
}