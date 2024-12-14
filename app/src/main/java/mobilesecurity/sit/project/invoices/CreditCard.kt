package mobilesecurity.sit.project.invoices

import androidx.annotation.Keep

@Keep data class CreditCard(
    var cardNumber: String = "",
    var cardHolderName: String = "",
    var expirationDate: String = "",
    var cvv: String = ""
)
