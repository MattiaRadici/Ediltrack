package com.example.ediltrack.util

enum class ProblemaStato(val code: Int, val displayed: String,val colorHex: String) {
    // 0 -> nonletto
    NON_LETTO(0, "Non Letto","#757575"),
    // 1 -> controllare
    DA_CONTROLLARE(1, "Da Controllare","#757575"),
    // 2 -> Errore
    ERRORE(2, "Errore","#757575"),
    // 3 -> Approvato
    APPROVATO(3, "Approvato","#757575");

    companion object {
        // Restituisce lo stato corrispondente al codice, oppure NON_LETTO come default
        fun fromCode(code: Int): ProblemaStato = entries.find { it.code == code } ?: NON_LETTO

        // Utile se ricevi la stringa visualizzata dalla UI e devi tornare all'Enum
        fun fromDisplayName(displayed: String): ProblemaStato = entries.find { it.displayed == displayed } ?: NON_LETTO
    }
}