package au.edu.utas.pborana.interiorquote

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HouseDetailActivity : AppCompatActivity() {

    private var houseId: String? = null
    private var houseName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_house_detail)

        houseId = intent.getStringExtra("HOUSE_ID")
        houseName = intent.getStringExtra("HOUSE_NAME")

        findViewById<TextView>(R.id.txtHouseTitle).text = houseName ?: "Rooms"

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}