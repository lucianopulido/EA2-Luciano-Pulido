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
    Context contexto;
    LayoutInflater inflador;

    public Adaptador(Context contexto, ArrayList<String> tipoEvento, ArrayList<String> descripcionEvento, ArrayList<String> dni,ArrayList<String> idEvento) {
        this.idEvento = new ArrayList<String>();
        this.tipoEvento = new ArrayList<String>();
        this.descripcionEvento = new ArrayList<String>();
        this.dni = new ArrayList<String>();

        this.contexto = contexto;
        this.tipoEvento = tipoEvento;
        this.descripcionEvento = descripcionEvento;
        this.dni = dni;
        this.idEvento = idEvento;

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


        holder.idEvento.setText(this.idEvento.get(position));
        holder.tipoEvento.setText(this.tipoEvento.get(position));
        holder.descripcionEvento.setText(this.descripcionEvento.get(position));
        holder.dni.setText(this.dni.get(position));
    }

    @Override
    public int getItemCount() {
        return idEvento.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView idEvento;
        TextView descripcionEvento;
        TextView tipoEvento;
        TextView dni;

        public ViewHolder(@NonNull View itemVista) {
            super(itemVista);
            this.idEvento = (TextView)  itemVista.findViewById(R.id.textViewIdEventoSensor);
            this.tipoEvento = (TextView) itemVista.findViewById(R.id.textViewTipoEvento);
            this.descripcionEvento = (TextView) itemVista.findViewById(R.id.textViewDescripcionEvento);
            this.dni = (TextView) itemVista.findViewById(R.id.textViewDni);


        }
    }
}