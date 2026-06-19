package com.example.ediltrack.util

enum class UserRole(val code: Int,val displayed: String) {
    ADMIN(0,"Admin"),
    CAPOCANTIERE(1,"Capocantiere"),
    OPERAIO(2,"Operaio");

    companion object {
        fun fromCode(code: Int): UserRole =  entries.find { it.code == code } ?: OPERAIO
        fun fromDisplayName(displayed: String): UserRole = entries.find { it.displayed == displayed } ?: OPERAIO
    }
}

enum class UtenteMode {OPERAIO, CAPOCANTIERE }