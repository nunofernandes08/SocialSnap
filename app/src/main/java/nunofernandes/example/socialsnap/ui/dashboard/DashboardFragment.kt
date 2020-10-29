package nunofernandes.example.socialsnap.ui.dashboard

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_dashboard.*
import nunofernandes.example.socialsnap.R
import nunofernandes.example.socialsnap.SnapItem
import java.io.ByteArrayInputStream
import java.util.*

class   DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    var listarFotos: MutableList<SnapItem> = ArrayList()
    var fotosAdapter: DashboardFragment.FotosAdapter? = null

    val db = FirebaseFirestore.getInstance()

    val storageRef = Firebase.storage.reference

    private var date: Date? = Date()

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser

        fotosAdapter = FotosAdapter()
        listViewFotos.adapter = fotosAdapter

        listarFotos.clear()

        db.collection("snaps")
            .whereEqualTo("userID", currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                listarFotos.clear()
                for (document in result) {
                    //listarFotos.clear()
                    //O Log.d é só para aparecer no logcat
                    Log.d("exist", "${document.id} => ${document.data}")
                    listarFotos.add(SnapItem(
                        document.data.getValue("filePath").toString(),
                        document.data.getValue("description").toString(), date,
                        document.data.getValue("userID").toString()))
                }
                fotosAdapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                //O Log.d é só para aparecer no logcat
                Log.w("dont exist", "Error getting documents.", exception)
            }
    }

    inner class FotosAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView = layoutInflater.inflate(R.layout.activity_rowfotos, parent, false)
            var comentario = rowView.findViewById<TextView>(R.id.txtComentario)
            var snapimg = rowView.findViewById<ImageView>(R.id.imgSnap)

            rowView.setOnClickListener {
                /*val action = DashboardFragmentDirections.actionNavigationDashboardToDetailSnap()
                    val fragment = Fragment()
                    val bundle = Bundle()
                    bundle.putString("description" ,listarFotos[position].description)
                    fragment.arguments = bundle
                it.findNavController().navigate(action)*/
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
