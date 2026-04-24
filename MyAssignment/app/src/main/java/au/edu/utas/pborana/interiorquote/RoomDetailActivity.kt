package au.edu.utas.pborana.interiorquote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class RoomDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var roomName: String? = null

    private val windows = mutableListOf<Window>()
    private lateinit var rvWindows: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_room_detail)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        roomName = intent.getStringExtra("ROOM_NAME")

        findViewById<TextView>(R.id.txtRoomTitle).text = roomName ?: "Room Details"

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvWindows = findViewById(R.id.rvWindows)
        rvWindows.layoutManager = LinearLayoutManager(this)

        if (houseId != null && roomId != null) {
            rvWindows.adapter = WindowAdapter(windows, houseId!!, roomId!!)
            loadWindows()
        } else {
            Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnAddWindow).setOnClickListener {
            val intent = Intent(this, AddEditWindowActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            intent.putExtra("ROOM_ID", roomId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAddFloor).setOnClickListener {
            Toast.makeText(this, "Floor spaces will be added next", Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadWindows() {
        if (houseId == null || roomId == null) return

        db.collection("houses")
            .document(houseId!!)
            .collection("rooms")
            .document(roomId!!)
            .collection("windows")
            .get()
            .addOnSuccessListener { result ->
                windows.clear()

                for (document in result) {
                    val window = document.toObject(Window::class.java)
                    window.id = document.id
                    windows.add(window)
                }

                rvWindows.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load windows", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (::rvWindows.isInitialized) {
            loadWindows()
        }
    }
}