package mobilesecurity.sit.project.userauth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import mobilesecurity.sit.project.R

class LoginSignUpActivity : AppCompatActivity() {

    private val permissionsToRequest = mutableListOf<String>()

    // Registering for activity result to handle results from launched activities if needed
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        // You can handle the result here if needed
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.entries.filter { !it.value }.map { it.key }
        if (deniedPermissions.isEmpty()) {
            // All permissions are granted
            showSettingsDialog()
        } else {
            // Handle the denied permissions
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login_sign_up)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        checkPermissions()

        // Initialize buttons for login and sign up
        val loginButton = findViewById<Button>(R.id.logBtn)
        val signUpButton = findViewById<TextView>(R.id.StaffView)

        // Set onClickListener for the login button to start LoginActivity
        loginButton.setOnClickListener {
            val intent = Intent(this, ParentLoginActivity::class.java)
            startForResult.launch(intent)
        }

        // Set onClickListener for the sign-up button to start SignUpActivity
        signUpButton.setOnClickListener {
            val intent = Intent(this, StaffLoginActivity::class.java)
            startForResult.launch(intent)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CALL_PHONE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_SMS)
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Settings Required")
            .setMessage("Please enable the necessary settings for Accessibility and Notification for the app to function properly.")
            .setPositiveButton("Go to Settings") { dialog, which ->
                // If the user agrees, start the Accessibility settings intent
                val intent1 = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent1)

                // Implement your delay or subsequent settings launch logic here
                // For simplicity, launching immediately here
                val intent2 = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent2)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}
