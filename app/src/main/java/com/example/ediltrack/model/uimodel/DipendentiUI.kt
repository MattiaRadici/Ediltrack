package com.example.ediltrack.model.uimodel

data class DipendentiUI(
    var uid: String = "",
    var nome :String = "",
    var ruolo : String = "",
    var img : String? = ""
) {
     constructor(): this ("","","","")
}