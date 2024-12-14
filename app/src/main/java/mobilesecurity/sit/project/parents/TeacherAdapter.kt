
package mobilesecurity.sit.project.parents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mobilesecurity.sit.project.R


class TeacherAdapter(private var teachers: List<TeacherInfoDataClass>, private val onClick: (TeacherInfoDataClass) -> Unit) :
    RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(teacher: TeacherInfoDataClass)
    }

    var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_teachers, parent, false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacher = teachers[position]
        holder.itemView.setOnClickListener { onClick(teacher) }
        holder.textViewName.text = teacher.name

    }

    override fun getItemCount(): Int = teachers.size

    class TeacherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.teacher_name)
    }

    fun updateTeachers(newTeachers: List<TeacherInfoDataClass>) {
        teachers = newTeachers
        notifyDataSetChanged()
    }

    }



