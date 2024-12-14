package mobilesecurity.sit.project.invoices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Adapter for displaying invoice details, inherits from ArrayAdapter
class InvoiceAdapter(context: Context, private val invoices: List<Invoice>) :
    ArrayAdapter<Invoice>(context, 0, invoices) {

    // Date format for displaying due dates
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Gets the view for each item in the invoice list
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate the custom layout for each item
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_invoice, parent, false)

        // Get the invoice object for the current position
        val currentInvoice = getItem(position) ?: return listItemView  // Return empty view if null

        // Assign the invoice data to UI components
        setupInvoiceData(listItemView, currentInvoice)

        // Set click listeners for the view and pay now button
        setupClickListeners(listItemView, currentInvoice)

        return listItemView
    }

    // Sets up the invoice data on the list item
    private fun setupInvoiceData(listItemView: View, currentInvoice: Invoice) {
        // Set text for month, status, and amount
        listItemView.findViewById<TextView>(R.id.invoice_month_year).text = currentInvoice.month
        listItemView.findViewById<TextView>(R.id.invoice_amount).text = context.getString(R.string.invoice_amount, currentInvoice.totalAmount)

        // Determine if the invoice is late and set the corresponding status text and color
        determineInvoiceStatus(listItemView, currentInvoice)
    }

    // Determines and sets the invoice status based on due date and payment status
    private fun determineInvoiceStatus(listItemView: View, currentInvoice: Invoice) {
        val statusTextView = listItemView.findViewById<TextView>(R.id.invoice_status)
        val dueDate = dateFormat.parse(currentInvoice.dueDate)  // Parse the due date from String to Date
        val isLate = dueDate?.before(Date()) ?: false  // Check if the invoice is overdue

        when {
            currentInvoice.paid -> {  // Invoice is paid
                statusTextView.text = context.getString(R.string.paid)
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.paid_color))
            }
            isLate -> {  // Invoice is overdue
                statusTextView.text = context.getString(R.string.late)
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.late_fee_color))
            }
            else -> {  // Invoice is pending
                statusTextView.text = context.getString(R.string.pending)
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.pending_color))
            }
        }
    }

    // Sets click listeners for the entire invoice view and the pay now button
    private fun setupClickListeners(listItemView: View, currentInvoice: Invoice) {
        // Set the click listener for the invoice item
        listItemView.setOnClickListener {
            if (!currentInvoice.paid) {  // Only navigate to details for unpaid invoices
                val intent = Intent(context, InvoiceDetailActivity::class.java).apply {
                    putExtra("INVOICE_ID", currentInvoice.id)
                }
                context.startActivity(intent)
            }
        }

        // Set the click listener for the Pay Now button
        val payNowButton = listItemView.findViewById<Button>(R.id.view_invoice_button)
        if (currentInvoice.paid) {
            payNowButton.visibility = View.GONE  // Hide button if invoice is paid
        } else {
            payNowButton.visibility = View.VISIBLE  // Show button if invoice is unpaid
            payNowButton.setOnClickListener {
                // Start InvoiceDetailActivity to pay the invoice
                context.startActivity(Intent(context, InvoiceDetailActivity::class.java).apply {
                    putExtra("INVOICE_ID", currentInvoice.id)
                })
            }
        }
    }
}
