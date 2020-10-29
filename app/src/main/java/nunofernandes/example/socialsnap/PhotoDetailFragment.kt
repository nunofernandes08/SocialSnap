package nunofernandes.example.socialsnap
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_photo_detail.*
import kotlinx.android.synthetic.main.fragment_detailsnap.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import java.util.*

class PhotoDetailFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private var bitmap : Bitmap? = null
    private var date : Date? = Date()

    val db = FirebaseFirestore.getInstance()

    val storageRef = Firebase.storage.reference
    val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

    var snapitemId : String? = null
    var snapItem: SnapItem? = null

    val args: PhotoDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_photo_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapitemId = args.snapID

        snapitemId?.let{
            db.collection("snaps").document(it)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null) {
                        snapItem = SnapItem.formHash(querySnapshot.data as HashMap<String, Any?>)
                        snapItem?.itemID = querySnapshot.id
                        editTextDescription.setText(snapItem?.description)

                        val storageRef = Firebase.storage.reference
                        val imagesRef = storageRef.child("images/${snapItem?.filePath}")

                        val ONE_MEGABYTE: Long = 1024 * 1024
                        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                            val bais = ByteArrayInputStream(it)
                            this.imageViewPhoto.setImageBitmap(BitmapFactory.decodeStream(bais))
                        }.addOnFailureListener {

                        }
                        floatingActionButtonTakePhoto.visibility = View.GONE

                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId.equals(snapItem?.userID)){
                            btPublicar.text = "Update"
                        }else {
                            btPublicar.visibility = View.GONE
                        }
                    }
                }
        }

        floatingActionButtonTakePhoto.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }

        btPublicar.setOnClickListener {

            var auth = Firebase.auth
            val currentUser = auth.currentUser

            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = imagesRef.putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads

            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.

                auth = Firebase.auth
                val currentUser = auth.currentUser

                val snap = SnapItem(imagesRef.name,
                                    editTextDescription.text.toString(), date,
                                    currentUser!!.uid)

                db.collection("snaps")
                    .add(snap.toHasMap())
                    .addOnSuccessListener { documentReference ->
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Erro ao guardar imagem", Toast.LENGTH_SHORT).show()
                    }
            }

            /*snapItem?.let {

                it.description = editTextDescription.text.toString()

                db.collection("snaps")
                    .document(it.itemID!!)
                    .set(it.toHasMap())
                    .addOnSuccessListener {
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Algo correu mal!", Toast.LENGTH_SHORT)
                            .show()
                    }

            }?:run {

                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                var uploadTask = imagesRef.putBytes(data)
                uploadTask.addOnFailureListener {
                }.addOnSuccessListener { taskSnapshot ->
                    val snap = SnapItem(
                        imagesRef.name,
                        editTextDescription.text.toString(),
                        date,
                        currentUser!!.uid
                    )

                    db.collection("snaps")
                        .add(snap.toHasMap())
                        .addOnSuccessListener {
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Algo correu mal!", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }*/
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode === Activity.RESULT_OK){
            if (requestCode == REQUEST_CODE_PHOTO){
                // new photo has arrive
                data?.extras?.let {
                    bitmap = it.get("data") as Bitmap
                    imageViewPhoto.setImageBitmap(bitmap)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PHOTO = 23524
     }
}