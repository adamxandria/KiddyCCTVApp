package mobilesecurity.sit.project.invoices
import androidx.annotation.Keep
import java.security.Timestamp

@Keep data class Invoice(
    var id: String = "",
    var month: String = "",
    var totalAmount: Double = 0.0,
    var paid: Boolean = false,
    var dueDate: String = ""
)
