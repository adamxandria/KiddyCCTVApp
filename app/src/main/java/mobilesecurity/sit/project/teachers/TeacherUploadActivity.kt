package mobilesecurity.sit.project.teachers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.google.android.material.floatingactionbutton.FloatingActionButton

import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.mainpage.MainActivity


class TeacherUploadActivity : AppCompatActivity() {

    private lateinit var uploadButton: FloatingActionButton
    private lateinit var uploadImage: ImageView
    private lateinit var uploadCaption: EditText
    private lateinit var progressBar: ProgressBar
    private var imageUri: Uri? = null
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Images")
    private val storageReference: StorageReference = FirebaseStorage.getInstance().getReference()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_teacher_upload)

        uploadButton = findViewById(R.id.activityUploadButton)
        uploadCaption = findViewById(R.id.uploadCaption)
        uploadImage = findViewById(R.id.uploadImage)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    imageUri = data?.data
                    uploadImage.setImageURI(imageUri)
                } else {
                    Toast.makeText(this@TeacherUploadActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
                }
            }
        )

        uploadImage.setOnClickListener {
            val photoPicker = Intent()
            photoPicker.action = Intent.ACTION_GET_CONTENT
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }

        uploadButton.setOnClickListener {
            if (imageUri != null) {
                uploadToFirebase(imageUri!!)
            } else {
                Toast.makeText(this@TeacherUploadActivity, "Please select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadToFirebase(uri: Uri) {
        val caption: String = uploadCaption.text.toString()
        val imageReference = storageReference.child(System.currentTimeMillis().toString() + "." + getFileExtension(uri))

        imageReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            imageReference.downloadUrl.addOnSuccessListener { uri ->
                val dataClass = DataClass(uri.toString(), caption)
                val key: String? = databaseReference.push().key
                databaseReference.child(key!!).setValue(dataClass)
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@TeacherUploadActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@TeacherUploadActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnProgressListener {
            progressBar.visibility = View.VISIBLE
        }.addOnFailureListener {
            progressBar.visibility = View.INVISIBLE
            Toast.makeText(this@TeacherUploadActivity, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileExtension(fileUri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri))
    }
}