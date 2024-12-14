package mobilesecurity.sit.project.invoices

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R

class AddNewCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_card)

        val cardNumberEditText = findViewById<EditText>(R.id.editTextCardNumber)
        val cardHolderNameEditText = findViewById<EditText>(R.id.editTextCardHolderName)
        val cvvEditText = findViewById<EditText>(R.id.editTextCVV)
        val monthSpinner = findViewById<Spinner>(R.id.spinnerMonth)
        val yearSpinner = findViewById<Spinner>(R.id.spinnerYear)
        val saveCardButton = findViewById<Button>(R.id.buttonSaveCard)
        val imageViewClose = findViewById<ImageView>(R.id.imageViewClose)

        imageViewClose.setOnClickListener {
            finish()  // Close the activity without adding a new card
        }

        saveCardButton.setOnClickListener {
            val cardNumber = cardNumberEditText.text.toString().trim()
            val cardHolderName = cardHolderNameEditText.text.toString().trim()
            val cvv = cvvEditText.text.toString().trim()
            val expiryMonth = monthSpinner.selectedItem.toString()
            val expiryYear = yearSpinner.selectedItem.toString()

            // Card validation
            if (cardNumber.length == 12 && cvv.length == 3) {
                val newCard = CreditCard(
                    cardNumber = cardNumber,
                    cardHolderName = cardHolderName,
                    expirationDate = "$expiryMonth/$expiryYear",
                    cvv = cvv
                )

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .collection("CreditCards").add(newCard)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Card added successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK) // Set result code
                        finish() // Close activity and go back
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add card: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

