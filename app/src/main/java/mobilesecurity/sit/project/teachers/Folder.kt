package mobilesecurity.sit.project.teachers

import androidx.annotation.Keep

@Keep data class Folder(
    var id: String = "", // Unique identifier for the folder, useful for Firestore document IDs
    var name: String = "" // The name of the folder
)