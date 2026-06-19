package com.example.ediltrack.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.ediltrack.model.Problematica
import com.example.ediltrack.util.ConnectDB
import io.mockk.coEvery
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProblematicheListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ProblematicheListViewModel
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProblematicheListViewModel()
        mockkObject(ConnectDB)
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    //Verifica il corretto caricamento delle problematiche dal DB
    @Test
    fun testRefreshData_LoadsProblematicheCorrectly() = runTest {
        // Arrange: Mockiamo la risposta del database
        val mockProblematica = Problematica(id = 1, descrizione = "Malfunzionamento generico", cantiere = 10)
        coEvery { ConnectDB.getAccountDet() } returns null // Simuliamo Admin o fallback
        coEvery { ConnectDB.getProblematiche(any(), any(), any(), any()) } returns listOf(mockProblematica)

        // Act
        viewModel.refreshData()

        // Assert
        assertEquals(1, viewModel.problematiche.value?.size)
        assertEquals("Malfunzionamento generico", viewModel.problematiche.value?.get(0)?.descrizione)
        assertEquals(false, viewModel.isEmpty.value)
    }
}
