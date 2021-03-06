package nunofernandes.example.socialsnap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Splash : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        currentUser?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }?:run{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        /*Handler().postDelayed({
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        },1000) */
    }
}
