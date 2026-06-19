package com.example.ediltrack.model.uimodel


data class FaseUI(

    var id: Int? = null,
    var cantiere: Int? = null,

    var numeroFase: String = "",
    var titolo: String = "",
    var descrizione: String = "",
    var terminata: Boolean = false
) {
    constructor() : this(null, null, "", "", "", false)
}

