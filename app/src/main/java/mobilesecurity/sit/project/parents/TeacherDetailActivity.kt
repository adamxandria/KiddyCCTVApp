
package mobilesecurity.sit.project.parents
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide // If you use Glide for image loading
import mobilesecurity.sit.project.R


class TeacherDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_teacher_detail)

        // Set up the Toolbar with a centered title
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable the default title

        // Set your custom title
//        val toolbarTitle: TextView = findViewById(R.id.toolbarTitle)
//        toolbarTitle.text = "Teacher's Profile"

        val name = intent.getStringExtra("teacher_name") ?: "Name not available"
        val email = intent.getStringExtra("teacher_email") ?: "Email not available"
        val hp = intent.getStringExtra("teacher_hp") ?: "Hp not available"
        val bio = intent.getStringExtra("teacher_bio") ?: "Bio not available"
        val subject = intent.getStringExtra("teacher_subject") ?: "Subject not available"
//        val role = intent.getStringExtra("teacher_role") ?: "Role not available"
        val profileImageUrl = intent.getStringExtra("profile_image_url")

        findViewById<TextView>(R.id.userName).text = name
        findViewById<TextView>(R.id.phoneNumber).text = hp
        findViewById<TextView>(R.id.bio).text = bio
        findViewById<TextView>(R.id.subjects).text = subject
//        findViewById<TextView>(R.id.role).text = role
        findViewById<TextView>(R.id.email).text = email

        val profileImageView = findViewById<ImageView>(R.id.profileImage)
        Glide.with(this)
            .load(profileImageUrl)
            .placeholder(R.drawable.default_profile) // Replace with your default placeholder
            .into(profileImageView)

    }
        // This method should be directly inside the class, not nested inside another method
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                android.R.id.home -> {
                    onBackPressed()
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
    }


