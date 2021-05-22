package com.example.tfg.perfil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R
import com.example.tfg.login.AgregadoInfoActivity
import com.example.tfg.pelicula.recomendaciones.Recomendacion
import com.example.tfg.perfil.listas.Lista
import com.example.tfg.perfil.listas.ListaFragment
import com.example.tfg.perfil.reseñas.MisRecomenFragment
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*
import kotlin.collections.ArrayList

class PerfilFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var numListas: TextView
    lateinit var recview: RecyclerView
    lateinit var edit: TextView
    lateinit var FirestoreRecyclerAdapter: FirestoreRecyclerAdapter<Lista, PerfilFragment.ListaViewHolder>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_perfil, container, false)

        /* Declaración de varibles */
        val btnMisResenas: Button = root.findViewById(R.id.misReseñas)
        var fotoPerfil: ImageView = root.findViewById(R.id.userFoto)
        var nombreUser: TextView = root.findViewById(R.id.tvUserName)
        var tvBio: TextView = root.findViewById(R.id.tvBio)
        edit = root.findViewById(R.id.tvEditPerfil)
        numListas = root.findViewById(R.id.tvNumListas)
        recview = root.findViewById(R.id.recListas)

        if (FirebaseAuth.getInstance().currentUser != null) {
            Picasso.get().load(FirebaseAuth.getInstance().currentUser.photoUrl).into(fotoPerfil)
            db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email).get()
                .addOnSuccessListener {
                    if (it.getString("nickname").toString() == "" || it.getString("nickname")
                            .isNullOrEmpty()
                    ) {
                        nombreUser.text = FirebaseAuth.getInstance().currentUser.displayName
                    } else {
                        nombreUser.text = it.getString("nickname").toString()
                    }
                }
            establecerBio()
            establecerListas()
            cargarRecyclerView()
            agregarInfoUser()
            funcionPruebaRetrieveRTDB()
        } else {
            nombreUser.visibility = View.GONE
            tvBio.visibility = View.GONE
            edit.visibility = View.GONE
            btnMisResenas.text = "Iniciar Sesión"
        }

        edit.setOnClickListener { checkPerfil() }


        btnMisResenas.setOnClickListener {

            if (btnMisResenas.text == "Iniciar Sesión") {
                val i: Intent =
                    Intent(context as Context, com.example.tfg.login.LoginActivity::class.java)
                startActivity(i)
            } else {
                var nickname = ""

                db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                    .get().addOnSuccessListener {
                    nickname = it.getString("nickname").toString()
                    val misRecomenFragment = MisRecomenFragment()
                    var bundle: Bundle = Bundle()
                    bundle.putString("nickname", nickname)
                    misRecomenFragment.arguments = bundle

                    activity?.supportFragmentManager?.beginTransaction()
                        ?.setCustomAnimations(
                            R.anim.slide_bottom_up,
                            R.anim.slide_bottom_down
                        )
                        ?.replace(R.id.container, misRecomenFragment)
                        ?.addToBackStack(null)
                        ?.commit();
                }
            }

        }

        return root
    }

    //-----------------------------------------------------------------------------------------------
    private fun funcionPruebaRetrieveRTDB() {

        var nickname = ""

        db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email).get()
            .addOnSuccessListener {
                nickname = it.getString("nickname").toString()
                Log.d("nombre", nickname)

                var query: com.google.firebase.database.Query =
                    FirebaseDatabase.getInstance().getReference("recomendaciones")
                        .orderByChild("nomUsuario").equalTo(nickname)

                var lista = ArrayList<Recomendacion>()

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap: DataSnapshot in snapshot.children) {
                                var rec: Recomendacion? = snap.getValue(Recomendacion::class.java)
                                if (rec != null) {
                                    lista.add(rec)
                                }
                            }
                            for (i in lista) {
                                println(i)
                            }
                        }
                    }
                })

            }


    }

    //-----------------------------------------------------------------------------------------------
    private fun agregarInfoUser() {
        db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email).get()
            .addOnSuccessListener {
                if (it.getString("nickname").isNullOrEmpty() || it.getString("biografia")
                        .isNullOrEmpty()
                ) {
                    val i: Intent =
                        Intent(context as Context, AgregadoInfoActivity::class.java)
                    startActivity(i)
                }
            }
    }

    //-----------------------------------------------------------------------------------------------

    private fun cargarRecyclerView() {

        val query: Query =
            db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                .collection("listas").orderBy("nombre")

        val options: FirestoreRecyclerOptions<Lista> =
            FirestoreRecyclerOptions.Builder<Lista>()
                .setQuery(query, Lista::class.java)
                .build()

        FirestoreRecyclerAdapter =
            object : FirestoreRecyclerAdapter<Lista, ListaViewHolder>(options) {

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ListaViewHolder {
                    val inflate = LayoutInflater.from(parent.context)
                    val v =
                        inflate.inflate(R.layout.custom_listas_layout, parent, false)
                    return ListaViewHolder(v)
                }

                override fun onBindViewHolder(
                    holder: ListaViewHolder,
                    position: Int,
                    model: Lista
                ) {
                    holder.tvLista.text = model.nombre
                    holder.tvContador.text = model.total

                    holder.itemView.setOnClickListener {
                        val listaFragment = ListaFragment()

                        var bundle: Bundle = Bundle()
                        bundle.putString("nombre", holder.tvLista.text.toString())
                        listaFragment.arguments = bundle

                        activity?.getSupportFragmentManager()?.beginTransaction()
                            ?.setCustomAnimations(
                                R.anim.slide_bottom_up,
                                R.anim.slide_bottom_down
                            )
                            ?.replace(R.id.container, listaFragment)
                            ?.addToBackStack(null)
                            ?.commit();
                    }
                }
            }

        recview.setHasFixedSize(true)
        recview.layoutManager = LinearLayoutManager(context as Context)
        recview.adapter = FirestoreRecyclerAdapter

    }


    class ListaViewHolder(var v: View) : RecyclerView.ViewHolder(v) {
        val tvLista: TextView = v.findViewById(R.id.tvNombreLista)
        val tvContador: TextView = v.findViewById(R.id.tvTotalPelis)
    }

    override fun onStart() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            super.onStart()
            FirestoreRecyclerAdapter.startListening()
        } else {
            super.onStart()
        }
    }

    override fun onStop() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            super.onStop()
            FirestoreRecyclerAdapter.stopListening()
        } else {
            super.onStop()
        }
    }


    //    /*
//    * Función rellenadoDatos()
//    *   Rellena el RecyclerView con las listas del usuario
//    * */
//    private fun rellenadoDatos() {
//
//        db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
//            .collection("listas").orderBy("nombre").get().addOnSuccessListener {
//                for (doc in it) {
//                    var titulo = doc.getString("nombre").toString()
//                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
//                        .collection("listas").document(titulo).collection("peliculas").get()
//                        .addOnSuccessListener {
//                            var contador = it.size().toString()
//                            var lista = Lista(
//                                titulo,
//                                contador
//                            )
//                            añadidoLista(lista)
//                        }
//                }
//            }
//
//    }
//    /*
//    * Función añadidoLista(lista: Lista)
//    *   Recibe el objeto de tipo Lista, y lo agrega a la lista, llamando luego a la función
//    *   crearAdapter()
//    * */
//    private fun añadidoLista(lista: Lista) {
//        listaListas.add(lista)
//        crearAdapter()
//    }
//
//
//    //-----------------------------------------------------------------------------------------------
//
//    /*
//    * Función crearAdapter()
//    *   Crear el adapter para el RecyclerView y establece el evento onClick sobre los elementos
//    * */
//    private fun crearAdapter() {
//        recview.setHasFixedSize(true)
//        miAdapter = ListaAdapter(listaListas, context as Context)
//        recview.layoutManager = LinearLayoutManager(context as Context)
//        recview.adapter = miAdapter
//
//        miAdapter.setOnClickListener(View.OnClickListener {
//            val listaFragment = ListaFragment()
//
//            var bundle: Bundle = Bundle()
//            bundle.putString("nombre", listaListas.get(recview.getChildAdapterPosition(it)).nombre)
//            listaFragment.arguments = bundle
//
//            activity?.getSupportFragmentManager()?.beginTransaction()
//                ?.setCustomAnimations(
//                    R.anim.slide_bottom_up,
//                    R.anim.slide_bottom_down
//                )
//                ?.replace(R.id.container,listaFragment)
//                ?.addToBackStack(null)
//                ?.commit();
//        })
//    }
//
//    /*
//    * Función establecerListas()
//    *   Esta función devuelve el total de listas del usuario
//    * */
    private fun establecerListas() {
        db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
            .collection("listas").get().addOnSuccessListener {
                numListas.text = it.size().toString()
            }
    }

    /*
    * Función establecerBio()
    *   Esta función busca la biografía del propio del usuario y la coloca en la etiqueta del layout
    * */
    private fun establecerBio() {
        db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    tvBio.setText(it.get("biografia") as String?)
                } else {
                    tvBio.visibility = View.GONE
                }
            }
    }

    /*
    *
    * Función checkPerfil()
    *   Función que comprueba si está logeado el usuario, y dependiendo de esto, redirige a una actividad
    *   o fragmento diferente
    * */
    private fun checkPerfil() {
        val user = FirebaseAuth.getInstance().getCurrentUser()
        if (user == null) {
            val i: Intent =
                Intent(context as Context, com.example.tfg.login.LoginActivity::class.java)
            startActivity(i)
        } else {
            val editPerfilFragment = EditPerfilFragment()
            activity?.getSupportFragmentManager()?.beginTransaction()
                ?.replace(R.id.container, editPerfilFragment)
                ?.addToBackStack(null)
                ?.commit();

        }
    }


}