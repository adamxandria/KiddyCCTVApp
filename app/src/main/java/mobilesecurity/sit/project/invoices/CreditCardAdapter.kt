package mobilesecurity.sit.project.invoices

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import mobilesecurity.sit.project.R

class CreditCardAdapter(context: Context, creditCards: List<CreditCard>) :
    ArrayAdapter<CreditCard>(context, 0, creditCards) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        val card = getItem(position)

        val cardNumberView = view.findViewById<TextView>(R.id.card_number)
        cardNumberView.text = "**** **** **** ${card?.cardNumber?.takeLast(4)}" // Masking card number for security

        return view
    }
}