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

class HouseDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var houseId: String? = null
    private var houseName: String? = null

    private val rooms = mutableListOf<Room>()
    private lateinit var rvRooms: RecyclerView

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

        rvRooms = findViewById(R.id.rvRooms)
        rvRooms.layoutManager = LinearLayoutManager(this)

        if (houseId != null) {
            rvRooms.adapter = RoomAdapter(rooms, houseId!!)
            loadRooms()
        } else {
            Toast.makeText(this, "Missing house information", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnAddRoom).setOnClickListener {
            val intent = Intent(this, AddEditRoomActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewQuote).setOnClickListener {
            val intent = Intent(this, QuoteActivity::class.java)
            intent.putExtra("HOUSE_ID", houseId)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadRooms() {
        if (houseId == null) return

        db.collection("houses")
            .document(houseId!!)
            .collection("rooms")
            .get()
            .addOnSuccessListener { result ->
                rooms.clear()

                for (document in result) {
                    val room = document.toObject(Room::class.java)
                    room.id = document.id
                    rooms.add(room)
                }

                rvRooms.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load rooms", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (::rvRooms.isInitialized) {
            loadRooms()
        }
    }
}