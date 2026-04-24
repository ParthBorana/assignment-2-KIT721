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

class AddEditWindowActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var houseId: String? = null
    private var roomId: String? = null
    private var windowId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_window)

        houseId = intent.getStringExtra("HOUSE_ID")
        roomId = intent.getStringExtra("ROOM_ID")
        windowId = intent.getStringExtra("WINDOW_ID")

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val txtName = findViewById<EditText>(R.id.txtWindowName)
        val txtWidth = findViewById<EditText>(R.id.txtWidth)
        val txtHeight = findViewById<EditText>(R.id.txtHeight)
        val btnSave = findViewById<Button>(R.id.btnSaveWindow)
        val btnDelete = findViewById<Button>(R.id.btnDeleteWindow)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)

        btnBack.setOnClickListener { finish() }

        // EDIT MODE
        if (windowId != null) {
            txtTitle.text = "Edit Window"
            btnSave.text = "Update Window"
            btnDelete.visibility = View.VISIBLE

            txtName.setText(intent.getStringExtra("WINDOW_NAME"))
            txtWidth.setText(intent.getDoubleExtra("WINDOW_WIDTH", 0.0).toString())
            txtHeight.setText(intent.getDoubleExtra("WINDOW_HEIGHT", 0.0).toString())
        }

        btnSave.setOnClickListener {

            val name = txtName.text.toString().trim()
            val width = txtWidth.text.toString().toDoubleOrNull()
            val height = txtHeight.text.toString().toDoubleOrNull()

            if (name.isEmpty() || width == null || height == null) {
                Toast.makeText(this, "Enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (houseId == null || roomId == null) return@setOnClickListener

            val window = Window(
                name = name,
                width = width,
                height = height
            )

            val ref = db.collection("houses")
                .document(houseId!!)
                .collection("rooms")
                .document(roomId!!)
                .collection("windows")

            if (windowId == null) {
                // ADD
                ref.add(window)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Window saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            } else {
                // UPDATE
                ref.document(windowId!!)
                    .set(window)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Window updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Window")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("houses")
                        .document(houseId!!)
                        .collection("rooms")
                        .document(roomId!!)
                        .collection("windows")
                        .document(windowId!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Window deleted", Toast.LENGTH_SHORT).show()
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