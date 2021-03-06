package com.example.tfg.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.tfg.MainActivity
import com.example.tfg.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import www.sanju.motiontoast.MotionToast

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val REQ_GOG = 124 // Validacion con Google
    private val REQ_INT_GOG = 125 // Intent a Activity2 con Google

    private val callbackManager = CallbackManager.Factory.create()

    companion object datos {
        val MAIL = "Correo"
    }

    private lateinit var mAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null

    //Para la validación de Google
    private lateinit var clienteGoogle: GoogleSignInClient

    private val db = FirebaseFirestore.getInstance()


    //----------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        clienteGoogle = GoogleSignIn.getClient(this, gso)
        btnSignGoogle.setOnClickListener(this)
        btnSignFace.setOnClickListener(this)
    }

    //----------------------------------------------------------------------------------------------
    override fun onClick(v: View?) {
        when (v) {
            btnSignGoogle -> {
                signInGoogle()
            }
            btnSignFace -> {
                signInFacebook()
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private fun signInFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

                override fun onSuccess(result: LoginResult?) {
                    result?.let {
                        val token = it.accessToken
                        val account = FacebookAuthProvider.getCredential(token.token)

                        FirebaseAuth.getInstance().signInWithCredential(account)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    it.result?.user?.email?.let { it1 -> irPerfil(it1) }
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Ya existe un usuario con ese correo",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.d("error", it.exception.toString())
                                }
                            }

                    }
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {
                    Toast.makeText(
                        applicationContext,
                        "Error inicio Facebook: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })
    }

    //----------------------------------------------------------------------------------------------
    private fun signInGoogle() {
        val i: Intent = clienteGoogle.signInIntent
        startActivityForResult(i, REQ_GOG)
    }
    //----------------------------------------------------------------------------------------------

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null) {
                        user.email?.let { irPerfil(it) }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error validación Google: " + task.exception,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    //----------------------------------------------------------------------------------------------
    private fun irPerfil(mail: String) {

        db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email).get()
            .addOnSuccessListener {
                if (it.getString("nickname") == null || it.getString("biografia") == null) {
                    val i: Intent =
                        Intent(this, AgregadoInfoActivity::class.java)
                    startActivity(i)

                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                        .collection("listas").document("💜 Películas favoritas").set(
                            hashMapOf("nombre" to "💜 Películas favoritas")
                        )
                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                        .collection("listas").document("⏰ Películas pendientes").set(
                            hashMapOf("nombre" to "⏰ Películas pendientes")
                        )
                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                        .collection("listas").document("👁 Películas vistas").set(
                            hashMapOf("nombre" to "👁 Películas vistas")
                        )

                } else {
                    val i = Intent(this, MainActivity::class.java)
                    i.putExtra(datos.MAIL, mail)
                    startActivityForResult(i, REQ_INT_GOG)

                    MotionToast.darkToast(
                        this,
                        "Inicio completo 😍",
                        "Has iniciado sesión correctamente!",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this, R.font.helvetica_regular)
                    )

                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                        .collection("listas").document("💜 Películas favoritas").set(
                            hashMapOf("nombre" to "💜 Películas favoritas")
                        )
                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                        .collection("listas").document("⏰ Películas pendientes").set(
                            hashMapOf("nombre" to "⏰ Películas pendientes")
                        )
                    db.collection("usuarios").document(FirebaseAuth.getInstance().currentUser.email)
                        .collection("listas").document("👁 Películas vistas").set(
                            hashMapOf("nombre" to "👁 Películas vistas")
                        )
                }
            }

    }

    //----------------------------------------------------------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_GOG) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar con Google", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == REQ_INT_GOG && resultCode == RESULT_OK) {
            clienteGoogle.signOut()
        }
    }

}