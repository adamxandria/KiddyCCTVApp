package mobilesecurity.sit.project.parents;

import androidx.annotation.Keep

@Keep data class TeacherInfoDataClass(
        var name: String = "",
        var email: String = "",
        var role: String = "",
        var hp: String = "",
        var bio: String = "",
        var subject: String="",
        var profileImg: String =""
)
