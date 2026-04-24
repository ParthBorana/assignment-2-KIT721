package au.edu.utas.pborana.interiorquote

data class Window(
    var id: String? = null,
    var name: String = "",
    var width: Double = 0.0,
    var height: Double = 0.0,
    var productName: String = "",
    var productPricePerSqm: Double = 50.0,
    var productColour: String = ""
)