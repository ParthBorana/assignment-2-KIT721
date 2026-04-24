package au.edu.utas.pborana.interiorquote

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddEditFloorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var floorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_floor)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        floorId = intent.getStringExtra("FLOOR_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)
        val txtName = findViewById<EditText>(R.id.txtFloorName)
        val txtWidth = findViewById<EditText>(R.id.txtWidth)
        val txtDepth = findViewById<EditText>(R.id.txtDepth)
        val btnSave = findViewById<Button>(R.id.btnSaveFloor)
        val btnDelete = findViewById<Button>(R.id.btnDeleteFloor)

        btnBack.setOnClickListener { finish() }

        if (floorId != null) {
            txtTitle.text = "Edit Floor Space"
            btnSave.text = "Update Floor Space"
            btnDelete.visibility = View.VISIBLE

            txtName.setText(intent.getStringExtra("FLOOR_NAME"))
            txtWidth.setText(intent.getDoubleExtra("FLOOR_WIDTH", 0.0).toString())
            txtDepth.setText(intent.getDoubleExtra("FLOOR_DEPTH", 0.0).toString())
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val depth = txtDepth.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || depth == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) {
                Toast.makeText(this, "Missing room information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val floor = FloorSpace(
                name = name,
                width = width,
                depth = depth
            )

            val ref = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")
                .document(roomId!!)
                .collection("floors")

            if (floorId == null) {
                ref.add(floor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Floor space saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error saving floor space", Toast.LENGTH_SHORT).show()
                    }
            } else {
                ref.document(floorId!!)
                    .set(floor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Floor space updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating floor space", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnDelete.setOnClickListener {
            if (houseId == null || roomId == null || floorId == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Delete Floor Space")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .collection("floors")
                        .document(floorId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Floor space deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}