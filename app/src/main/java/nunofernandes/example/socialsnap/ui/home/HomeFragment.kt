package nunofernandes.example.socialsnap.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_home.*
import nunofernandes.example.socialsnap.R
import nunofernandes.example.socialsnap.SnapItem
import nunofernandes.example.socialsnap.helpers.dateToString
import nunofernandes.example.socialsnap.helpers.stringToDate
import nunofernandes.example.socialsnap.ui.dashboard.DashboardFragmentDirections
import java.io.ByteArrayInputStream
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    var listarFotos: MutableList<SnapItem> = ArrayList()
    var fotosAdapter: HomeFragment.FotosAdapter? = null

    val db = FirebaseFirestore.getInstance()

    val storageRef = Firebase.storage.reference

    //private var date: String? = dateToString(Date())

    private var date: Date? = Date()

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fotosAdapter = FotosAdapter()
        listViewFotos_home.adapter = fotosAdapter

        listarFotos.clear()

        auth = Firebase.auth
        //val currentUser = auth.currentUser

        db.collection("snaps")
            .orderBy("date")
            .get()
            .addOnSuccessListener { result ->
                listarFotos.clear()
                for (document in result) {
                    //listarFotos.clear()
                    //O Log.d é só para aparecer no logcat
                    Log.d("exist", "${document.id} => ${document.data}")
                        val snap = SnapItem.formHash(document.data as HashMap<String, Any?>)
                        snap.itemID = document.id
                        listarFotos.add(snap)
                }
                fotosAdapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("dont exist", "Error getting documents.", exception)
            }


        floatingLogout.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToLogin()
            FirebaseAuth.getInstance().signOut()
            it.findNavController().navigate(action)
        }

        floatingActionButton.setOnClickListener {
            val action = HomeFragmentDirections.actionNavigationHomeToPhotoDetailFragment(null)
            it.findNavController().navigate(action)
        }

        floatingRecovery.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val emailAddress = auth.currentUser!!.email.toString()

            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Sucesso", "Email enviado com sucesso")
                    }
                }
        }

    }

    inner class FotosAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView = layoutInflater.inflate(R.layout.activity_rowfotos_home, parent, false)
            var comentario = rowView.findViewById<TextView>(R.id.txtComentario_home)
            var snapimg = rowView.findViewById<ImageView>(R.id.snapImg)

            rowView.setOnClickListener {
                val action = HomeFragmentDirections.actionNavigationHomeToPhotoDetailFragment(listarFotos[position].itemID)
                it.findNavController().navigate(action)
            }

            val imagesRef = storageRef.child("images/${listarFotos[position].filePath}")

            val ONE_MEGABYTE: Long = 1024 * 1024
            imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                val byte = ByteArrayInputStream(it)
                snapimg.setImageBitmap((BitmapFactory.decodeStream(byte)))

            }.addOnFailureListener {
                // Handle any errors
            }

            comentario.text = listarFotos[position].description

            return rowView
        }

        override fun getItem(position: Int): Any {
            return listarFotos[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return listarFotos.size
        }
    }
}
