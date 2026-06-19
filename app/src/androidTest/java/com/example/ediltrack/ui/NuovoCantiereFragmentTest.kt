package com.example.ediltrack.ui

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.recyclerview.widget.RecyclerView
import com.example.ediltrack.R
import com.example.ediltrack.ui.view.activity.LoginActivity
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NuovoCantiereFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testFlussoCompletoNuovoCantiere() {
        // 1. FAI IL LOGIN AUTOMATICO COME ADMIN
        onView(withId(R.id.editTextTextEmailAddress))
            .perform(typeText("m.radici02@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.editTextTextPassword))
            .perform(typeText("mattia"), closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        // 2. Attesa caricamento HomeGestione
        Thread.sleep(4000)

        // 3. Clicca sul FAB per il nuovo cantiere
        onView(withId(R.id.fabNuovoCantiere)).check(matches(isDisplayed())).perform(click())
        Thread.sleep(1000)

        // 4. Compila i campi base
        onView(withId(R.id.NomeCantiere))
            .perform(typeText("Cantiere Residenziale Alpha"), closeSoftKeyboard())
        onView(withId(R.id.Luogo))
            .perform(typeText("Milano Centro"), closeSoftKeyboard())

        // 5. Associa Capocantiere (selezionando la checkbox interna)
        onView(withId(R.id.btnAssociaCapoCant)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.recyclerUtenti))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, clickChildViewWithId(R.id.checkOperaio)
            ))
        Thread.sleep(1000) // Aspetta che il fragment si chiuda da solo (popBackStack automatico)

        // 6. Associa Operai (selezionando le checkbox interne)
        onView(withId(R.id.btnAssociaOperaio)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.recyclerUtenti))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, clickChildViewWithId(R.id.checkOperaio)
            ))
        onView(withId(R.id.recyclerUtenti))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, clickChildViewWithId(R.id.checkOperaio)
            ))
        // Clicca sul FAB per salvare la selezione multipla
        onView(withId(R.id.fabSalvaOperatori)).perform(click())
        Thread.sleep(500)

        // 7. Associa e compila Fasi
        onView(withId(R.id.btnFasi)).perform(click())
        Thread.sleep(1000)
        
        // Aggiungi la prima fase e compilala
        onView(withId(R.id.btnAggiungiFase)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.recyclerFasi))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, typeTextInChildViewWithId(R.id.titolo, "Demolizione e Scavi")
            ))
        onView(withId(R.id.recyclerFasi))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, typeTextInChildViewWithId(R.id.descrizione, "Fase preliminare di pulizia del cantiere")
            ))

        // Aggiungi la seconda fase e compilala
        onView(withId(R.id.btnAggiungiFase)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.recyclerFasi))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, typeTextInChildViewWithId(R.id.titolo, "Posa Fondamenta")
            ))
        onView(withId(R.id.recyclerFasi))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, typeTextInChildViewWithId(R.id.descrizione, "Gettata di cemento armato e posa ferri")
            ))

        // Salva le fasi e torna indietro
        onView(withId(R.id.btnSalva)).perform(click())
        Thread.sleep(500)

        // 8. Salva Cantiere definitivo (usa forceClick perché il layout non ha una ScrollView)
        onView(withId(R.id.btnSalvaCantiere)).perform(forceClick())
    }

    // --- HELPER CUSTOM VIEW ACTIONS PER RECYCLER VIEW ---

    private fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = androidx.test.espresso.matcher.ViewMatchers.isEnabled()
            override fun getDescription(): String = "Click on a child view with specified id."
            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v.performClick()
            }
        }
    }

    private fun typeTextInChildViewWithId(id: Int, text: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = androidx.test.espresso.matcher.ViewMatchers.isEnabled()
            override fun getDescription(): String = "Type text in a child view with specified id."
            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<android.widget.EditText>(id)
                v.setText(text)
            }
        }
    }

    private fun forceClick(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = androidx.test.espresso.matcher.ViewMatchers.isEnabled()
            override fun getDescription(): String = "Force click ignoring visibility constraints"
            override fun perform(uiController: UiController, view: View) {
                view.performClick()
            }
        }
    }
}
