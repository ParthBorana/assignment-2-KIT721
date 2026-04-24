package au.edu.utas.pborana.interiorquote

data class QuoteItem(
    var name: String = "",
    var price: Double = 0.0,
    var isSelected: Boolean = true
)