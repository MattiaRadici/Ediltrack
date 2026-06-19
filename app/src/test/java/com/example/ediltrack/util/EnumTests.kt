package com.example.ediltrack.util

import org.junit.Assert.assertEquals
import org.junit.Test

class EnumTests {

    // Test 1: Verifica la corretta risoluzione del ruolo utente dal codice numerico
    @Test
    fun testUserRoleFromCode_ReturnsCorrectRole() {
        // Assert per il ruolo Admin (Codice 0)
        val adminRole = UserRole.fromCode(0)
        assertEquals(UserRole.ADMIN, adminRole)

        // Assert per il ruolo Capocantiere (Codice 1)
        val capocantiereRole = UserRole.fromCode(1)
        assertEquals(UserRole.CAPOCANTIERE, capocantiereRole)

        // Assert per il fallback di sicurezza su codice non riconosciuto
        val defaultRole = UserRole.fromCode(99)
        assertEquals(UserRole.OPERAIO, defaultRole)
    }

    // Test 2: Verifica la logica di parsing dello stato della problematica da stringa UI
    @Test
    fun testProblemaStatoFromDisplayName_ReturnsCorrectState() {
        // Assert per stato valido
        val approvato = ProblemaStato.fromDisplayName("Approvato")
        assertEquals(ProblemaStato.APPROVATO, approvato)

        // Assert per stato di fallback in caso di stringa corrotta o nulla
        val defaultStato = ProblemaStato.fromDisplayName("Stato_Inesistente_123")
        assertEquals(ProblemaStato.NON_LETTO, defaultStato)
    }
}
