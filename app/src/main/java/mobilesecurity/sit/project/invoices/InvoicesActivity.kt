package mobilesecurity.sit.project.invoices

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class InvoicesActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var invoiceListView: ListView
    private var invoices: ArrayList<Invoice> = ArrayList()
    private lateinit var adapter: InvoiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_invoices)
        // Make sure you have the correct layout file for your activity

        firestoreDb = FirebaseFirestore.getInstance()
        invoiceListView = findViewById(R.id.invoices_list)
        adapter = InvoiceAdapter(this, invoices)
        invoiceListView.adapter = adapter

        loadInvoices(firestoreDb, invoices, adapter)
        checkAndAddNewInvoice()

        val addButton: FloatingActionButton = findViewById(R.id.add_invoice_button)
        addButton.setOnClickListener {
            addNewInvoice()
        }

        // Set up the toolbar back button
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // This line will make the back button return to the previous fragment or activity
            finish()
        }

        //navigation bar:

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

    private fun loadInvoices(firestoreDb: FirebaseFirestore, invoices: ArrayList<Invoice>, adapter: InvoiceAdapter) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestoreDb.collection("users").document(it).collection("Invoices")
                // Use a snapshot listener for real-time updates
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(this, "Error listening for invoice updates: $e", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        invoices.clear()
                        for (document in snapshot.documents) {
                            val invoice = document.toObject(Invoice::class.java)?.apply {
                                id = document.id
                            }
                            if (invoice != null) {
                                invoices.add(invoice)
                            }
                        }

                        adapter.notifyDataSetChanged() // Notify the adapter to refresh the list
                    } else {
                        Toast.makeText(this, "No invoices found", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun addNewInvoice() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val newInvoice = Invoice().apply {
                month = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                totalAmount = 1200.0  // Monthly school fees at $1200
                paid = false
                dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 3) // Due date is 3 days after start of each month
                    }.time
                )
            }

            // Adding the invoice without setting an ID
            val docRef = firestoreDb.collection("users").document(it).collection("Invoices").document() // Create document reference with auto ID
            newInvoice.id = docRef.id // Set the invoice's ID to the document's auto-generated ID
            docRef.set(newInvoice) // Add the invoice with the ID to Firestore
                .addOnSuccessListener {
                    Toast.makeText(this, "Invoice added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add invoice: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun checkAndAddNewInvoice() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        userId?.let {
            firestoreDb.collection("users").document(it).collection("Invoices")
                .whereEqualTo("month", currentMonth)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {  // No invoice for the current month
                        addNewInvoice()  // Adds a new invoice if one doesn't exist
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error checking for new invoice: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }


}

