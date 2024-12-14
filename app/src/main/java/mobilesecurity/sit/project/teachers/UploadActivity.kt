package mobilesecurity.sit.project.teachers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.databinding.ActivityUploadimagesBinding
import mobilesecurity.sit.project.mainpage.MainActivity
import java.io.ByteArrayOutputStream
import java.util.*

class UploadActivity : AppCompatActivity() {

    private lateinit var folderId: String // Define folderId at the class level
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var folderName : String
    private lateinit var binding: ActivityUploadimagesBinding
    private var imageList = ArrayList<DataClass>() // Adjust DataClass according to your data model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        binding = ActivityUploadimagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize imageAdapter here before calling setupRecyclerView()
        imageAdapter = ImageAdapter(ArrayList())

        // Initialize 'folderId' with the value passed in the intent
        folderId = intent.getStringExtra("folderId") ?: throw IllegalStateException("Folder ID must be provided.")
        folderName = intent.getStringExtra("FOLDER_NAME") ?: "Category" // Default to "Category" if null

        binding.CategoryTextView.text = folderName

        Log.d("UploadActivity", "Received folder ID: $folderId, folder name:$folderName")
        if (folderId == null) {
            Log.e("UploadActivity", "No folderId was passed to the activity")
            finish() // Closex the activity or handle the error appropriately
        } else {
            Log.d("UploadActivity", "Received folderId: $folderId")
            setupRecyclerView()
            fetchImagesForFolder(folderId)

        }

        // Setup the button to open the camera
        val uploadImageLayout: LinearLayout = findViewById(R.id.takeimage)
        uploadImageLayout.setOnClickListener {
            openCamera()
        }

        val uploadImageButton: LinearLayout = findViewById(R.id.uploadimage)
        uploadImageButton.setOnClickListener {
            Toast.makeText(this, "Upload image button pressed", Toast.LENGTH_SHORT).show()
            openGallery()
        }

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

    private fun setupRecyclerView() {
        with(binding.recyclerView) {
            setHasFixedSize(true) // This can help improve performance if the size of the RecyclerView itself doesn't change
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) // Set up the layout manager
            adapter = imageAdapter
        }
    }

    companion object {
        private const val CAMERA_REQ_CODE = 101
        private const val CAMERA_PERMISSION_CODE = 102
        private const val GALLERY_REQ_CODE = 103
        private const val GALLERY_PERMISSION_CODE = 104
    }

    private fun openGallery() {
        Log.d("UploadActivity", "Trying to open gallery.")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UploadActivity", "Permission not granted. Requesting permission.")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSION_CODE)
        } else {
            Log.d("UploadActivity", "Permission granted. Opening gallery.")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQ_CODE)
        }
    }


    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            // Permission has already been granted, open the camera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQ_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission was granted
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQ_CODE)
            } else {
                // Camera permission was denied
                Toast.makeText(this, "Camera permission is needed to use the camera", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Gallery permission was granted, open the gallery
                openGallery()
            } else {
                // Gallery permission was denied
                Toast.makeText(this, "Storage permission is needed to access the gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                uploadImageToFirebaseStorage(folderId, imageUri)
            }
        } else if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageToFirebaseStorage(folderId, imageBitmap)
        }
    }

    private fun uploadImageToFirebaseStorage(folderId: String, imageData: Any) {
        val storageRef = FirebaseStorage.getInstance().reference
        val filePath = storageRef.child("images/${folderId}/${UUID.randomUUID()}.jpg")

        when (imageData) {
            is Bitmap -> {
                val baos = ByteArrayOutputStream()
                imageData.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                uploadBytes(filePath, data)
            }
            is Uri -> {
                val inputStream = contentResolver.openInputStream(imageData as Uri) // Ensure imageData is cast to Uri here
                val data = inputStream?.readBytes()
                if (data != null) {
                    uploadBytes(filePath, data)
                } else {
                    Toast.makeText(this, "Error loading image data", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "Invalid image data type", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadBytes(filePath: StorageReference, data: ByteArray) {
        val uploadTask = filePath.putBytes(data)
        uploadTask.addOnSuccessListener {
            filePath.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                saveImageInfoToFirestore(folderId, downloadUrl)
            }
        }.addOnFailureListener { exception ->
            Log.e("Upload", "Upload failed", exception)
            Toast.makeText(baseContext, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageInfoToFirestore(folderId: String, downloadUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val imageInfo = hashMapOf(
            "folderId" to folderId,
            "url" to downloadUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("images").add(imageInfo).addOnSuccessListener { documentReference ->
            Toast.makeText(this, "Image saved to Firestore", Toast.LENGTH_SHORT).show()
            fetchImagesForFolder(folderId)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error saving image to Firestore", Toast.LENGTH_SHORT).show()
        }
    }



    private fun fetchImagesForFolder(folderId: String) {
        FirebaseFirestore.getInstance().collection("images")
            .whereEqualTo("folderId", folderId) // Ensure this matches your Firestore structure
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("UploadActivity", "Fetched documents count: ${documents.size()}")
                Log.d("UploadActivity", "Looking for images in folder: $folderId")
                val newImageList = documents.mapNotNull { it.getString("url") }
                imageAdapter.updateData(newImageList)
                imageAdapter.notifyDataSetChanged()

//                val imageUrls = documents.mapNotNull { it.getString("url") } // Extracting URLs
//                imageAdapter.updateData(imageUrls) // Update adapter with filtered images
            }
            .addOnFailureListener { e ->
                Log.e("UploadActivity", "Error fetching images for folder: ", e)
            }
    }
}
