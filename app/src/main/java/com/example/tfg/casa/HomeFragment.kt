package com.example.tfg.casa

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R
import com.example.tfg.perfil.EditPerfilFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    lateinit var miAdapter: CategoriaAdapter
    var listaCategoria = ArrayList<Categoria>()

    lateinit var recview: RecyclerView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        EditPerfilFragment().comprobarTema(activity as Activity, context as Context)

        recview = root.findViewById(R.id.rvListas)

        crearAdapter()
        rellenadoLista()

        return root
    }

    private fun crearAdapter() {
        recview.setHasFixedSize(true)
        miAdapter = CategoriaAdapter(listaCategoria,context as Context)
        recview.layoutManager = GridLayoutManager(context as Context, 2, GridLayoutManager.VERTICAL, false)
        recview.adapter = miAdapter
    }

    private fun rellenadoLista() {
        db.collection("categorias").get().addOnSuccessListener {
            for (doc in it){
                var cat = Categoria(
                    doc.getString("titulo").toString(),
                    doc.getString("logo").toString()
                )
                listaCategoria.add(cat)
            }
            crearAdapter()
        }
    }


}