package mobilesecurity.sit.project.principal

data class User(
    var id: String = "", // Document ID from Firestore
    var fullName: String = "",
    var name: String = "",
    var email: String = "",
    var hp: String = "",
    var gender: String = "",
    var role: String = "",
    var address: String = "",
    val profileImg: String = "", // URL or path to the profile image
    // for parent and teachers?
    var password: String = "",
    var childID: String = "",
    var classGroup: String = ""
)

