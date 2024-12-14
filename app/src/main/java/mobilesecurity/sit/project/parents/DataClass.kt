package mobilesecurity.sit.project.parents

import androidx.annotation.Keep

@Keep data class DataClass(
    var url: String? = null,  // This should match the field name in Firestore.
    var folderId: String? = null
)
