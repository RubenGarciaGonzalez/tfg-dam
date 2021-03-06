package com.example.tfg.casa

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R
import com.google.firebase.firestore.FirebaseFirestore

class CategoriaAdapter(private val miLista: ArrayList<Categoria>, val c: Context):
    RecyclerView.Adapter<CategoriaAdapter.MiViewHolder>(), View.OnClickListener{

    lateinit var listener: View.OnClickListener

    class MiViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val tvTitulo: TextView = v.findViewById(R.id.tvTitMov)
        val tvLogo: TextView = v.findViewById(R.id.tvLogoCV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val v = inflate.inflate(R.layout.custom_grid_layout, parent, false)
        v.setOnClickListener(this)
        return MiViewHolder(v)
    }

    override fun getItemCount(): Int {
        return miLista.size
    }

    override fun onBindViewHolder(holder: MiViewHolder, position: Int) {
        val item = miLista[position]
        holder.tvTitulo.text = item.titulo
        holder.tvLogo.text = item.logo
    }

    fun setOnClickListener(listener: View.OnClickListener){
        this.listener = listener
    }

    override fun onClick(v: View?) {
        if (listener != null){
            listener.onClick(v)
        }
    }

}