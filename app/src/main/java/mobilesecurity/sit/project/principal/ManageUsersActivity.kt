package mobilesecurity.sit.project.principal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity

class ManageUsersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_manage_users)

        // Assuming you have buttons or some kind of UI elements to select different roles
        // Assuming you have buttons or some kind of UI elements to select different roles
        val principalButton: View = findViewById(R.id.manageAdmins)
        val teacherButton: View = findViewById(R.id.manageTeachers)
        val parentButton: View = findViewById(R.id.manageParents)
        val childrenButton: View = findViewById(R.id.manageChildren)
        val backButton: ImageView = findViewById(R.id.ivBackArrow)
        backButton.setOnClickListener {
            finish()
        }
        val addNewUserButton: LinearLayout = findViewById(R.id.addNewUser)
        addNewUserButton.setOnClickListener {
            // Start the NewUsersActivity
            val intent = Intent(this, NewUsersActivity::class.java)
            startActivity(intent)
        }

        principalButton.setOnClickListener { showUsersForRole("Principal") }
        teacherButton.setOnClickListener { showUsersForRole("Teacher") }
        parentButton.setOnClickListener { showUsersForRole("Parent") }
        childrenButton.setOnClickListener { showUsersForRole("Children") }

        val bottomNavigationView =findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.message -> {
                    val intent = Intent(this, ConversationActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }

        }
    }

    private fun showUsersForRole(role: String) {
        val intent = Intent(this, ManageRoleActivity::class.java).apply {
            putExtra("role", role)
            putExtra("roleTitle", role)
        }
        startActivity(intent)
    }
}
