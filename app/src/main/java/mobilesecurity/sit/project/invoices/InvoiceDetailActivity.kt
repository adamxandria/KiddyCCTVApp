package mobilesecurity.sit.project.invoices

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class InvoiceDetailActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var cardsListView: ListView
    private var cardsList = arrayListOf<CreditCard>()
    private lateinit var cardsAdapter: CreditCardAdapter
    private var selectedCard: CreditCard? = null
    private var invoiceId: String? = null


    companion object {
        private const val REQUEST_ADD_CARD = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_CARD && resultCode == RESULT_OK) {
            // Code to refresh the card list
            setupCardListView()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_invoice)

        // Initialization
        firestoreDb = FirebaseFirestore.getInstance()
        cardsListView = findViewById<ListView>(R.id.cards_list)
        cardsAdapter = CreditCardAdapter(this, cardsList)
        cardsListView.adapter = cardsAdapter
        setupPayNowButton()

        invoiceId = intent.getStringExtra("INVOICE_ID")
        if (invoiceId == null) {
            Toast.makeText(this, "Invoice ID is missing", Toast.LENGTH_SHORT).show()
            finish() // Finish the activity if no invoice ID was passed
            return
        }
        loadInvoiceDetails(invoiceId!!)

        setupCardListView()

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish() // Finish this activity and go back
        }

        val addNewCardButton = findViewById<Button>(R.id.buttonAddNewCard)
        addNewCardButton.setOnClickListener {
            val intent = Intent(this, AddNewCardActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_CARD)
        }
    }


    private fun loadInvoiceDetails(invoiceId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("Invoices").document(invoiceId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val invoice = documentSnapshot.toObject(Invoice::class.java) ?: return@addOnSuccessListener

                findViewById<TextView>(R.id.invoiceTitle).text = getString(R.string.invoice_title, invoice.month)
                findViewById<TextView>(R.id.invoiceAmount).text = getString(R.string.invoice_amount_format, DecimalFormat("#,###.00").format(invoice.totalAmount))
                findViewById<TextView>(R.id.invoiceStatus).text = if (invoice.paid) "Paid" else "Unpaid"
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun setupCardListView() {
        val listView = findViewById<ListView>(R.id.cards_list)
        val adapter = CreditCardAdapter(this, ArrayList()) // Initialize adapter with an empty list
        listView.adapter = adapter

        // Load credit cards from Firestore and update the adapter
        loadCreditCards { cards ->
            adapter.clear()
            adapter.addAll(cards)
            adapter.notifyDataSetChanged()
        }

        // Set the item click listener for the ListView
        listView.setOnItemClickListener { _, view, position, _ ->
            // Update the UI to highlight the selected item
            for (i in 0 until listView.childCount) {
                listView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT)  // Remove background from all items
            }
            view.setBackgroundColor(Color.LTGRAY)  // Highlight the selected item

            // Store the selected card
            selectedCard = adapter.getItem(position)
        }
    }

    private fun loadCreditCards(onCardsLoaded: (List<CreditCard>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("CreditCards")
            .get()
            .addOnSuccessListener { documents ->
                val cards = documents.mapNotNull { it.toObject(CreditCard::class.java) }
                onCardsLoaded(cards)
            }
            .addOnFailureListener { e ->
                Log.e("InvoiceDetailActivity", "Error getting credit cards: ", e)
                Toast.makeText(this, "Error loading credit cards", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupPayNowButton() {
        val payNowButton = findViewById<Button>(R.id.payNowButton)
        payNowButton.setOnClickListener {
            if (selectedCard != null) {
                invoiceId?.let {
                    markInvoiceAsPaid(it)
                } ?: Toast.makeText(this, "Invoice ID is missing", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select a card", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markInvoiceAsPaid(invoiceId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("Invoices").document(invoiceId)
            .update("paid", true)
            .addOnSuccessListener {
                // Update the UI when the invoice is paid
                val invoiceStatus = findViewById<TextView>(R.id.invoiceStatus)
                invoiceStatus.text = "Paid"
                invoiceStatus.setTextColor(ContextCompat.getColor(this, R.color.paid_color))
                finish()

//                // Hide the 'Pay Now' button and card list
//                findViewById<Button>(R.id.payNowButton).visibility = View.GONE
//                findViewById<ListView>(R.id.cards_list).visibility = View.GONE
//                findViewById<Button>(R.id.buttonAddNewCard).visibility = View.GONE
//                findViewById<TextView>(R.id.creditAndDebitCardsTitle).visibility = View.GONE
//                Toast.makeText(this, "Invoice marked as paid", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error marking invoice as paid: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}