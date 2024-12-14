package mobilesecurity.sit.project.userauth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import mobilesecurity.sit.project.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_onboarding)

        val layouts = intArrayOf(
            R.layout.activity_onboarding1,
            R.layout.activity_onboarding2
        )

        viewPager = findViewById(R.id.viewPager)
        adapter = OnboardingAdapter(this, layouts)
        viewPager.adapter = adapter

        val btnNext = findViewById<TextView>(R.id.nextTextView)
        val btnSkip = findViewById<TextView>(R.id.skipTextView)

        btnSkip.setOnClickListener {
            startActivity(Intent(this, LoginSignUpActivity::class.java))
            finish()
        }
        btnNext.setOnClickListener {
            val current = viewPager.currentItem + 1
            if (current < layouts.size) {
                viewPager.currentItem = current
            } else {
                startActivity(Intent(this, LoginSignUpActivity::class.java))
                finish()
            }
        }
    }
}
