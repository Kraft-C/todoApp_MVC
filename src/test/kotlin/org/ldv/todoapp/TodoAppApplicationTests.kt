package org.ldv.todoapp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.ldv.todoapp.config.CompteAuthentifie
import org.ldv.todoapp.config.ComptesUtilisateur
import org.ldv.todoapp.model.entity.Utilisateur
import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest
@AutoConfigureMockMvc
class TodoAppApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var comptesUtilisateur: ComptesUtilisateur

    @Autowired
    private lateinit var utilisateurRepository: UtilisateurRepository

    @Test
    fun contextLoads() {
    }

    @Test
    fun loginPageAccessible() {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
    }

    @Test
    fun protectedPagesRedirectToLogin() {
        mockMvc.perform(get("/mes-taches"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }

    @Test
    fun authenticationSuccess() {
        mockMvc.perform(
            formLogin("/login")
                .user("username", "alice@example.com")
                .password("password", "Alice-TP3!")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))
            .andExpect(authenticated().withUsername("alice@example.com"))
    }

    @Test
    fun authenticationFailure() {
        mockMvc.perform(
            formLogin("/login")
                .user("username", "alice@example.com")
                .password("password", "mauvais_mdp")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated())
    }

    @Test
    fun logoutSuccess() {
        mockMvc.perform(post("/logout").with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login?logout"))
            .andExpect(unauthenticated())
    }

    @Test
    fun loadUserByUsernameReturnsCompteAuthentifie() {
        val userDetails = comptesUtilisateur.loadUserByUsername("alice@example.com")
        val compte = assertInstanceOf(CompteAuthentifie::class.java, userDetails)
        assertEquals("alice@example.com", compte.username)
        val utilisateur = utilisateurRepository.findByEmail("alice@example.com")
        assertNotNull(utilisateur)
        assertEquals(utilisateur?.idUtilisateur, compte.idUtilisateur)
    }

    @Test
    fun utilisateurConnecteAdviceInjectsUtilisateurActif() {
        val userDetails = comptesUtilisateur.loadUserByUsername("alice@example.com")
        val compte = assertInstanceOf(CompteAuthentifie::class.java, userDetails)

        mockMvc.perform(get("/").with(user(compte)))
            .andExpect(status().isOk)
            .andExpect(model().attributeExists("utilisateurActif"))
            .andExpect(request().attribute("utilisateurActif", org.hamcrest.Matchers.notNullValue()))
    }

    @Test
    fun mesTachesAccessibleWhenAuthenticated() {
        val userDetails = comptesUtilisateur.loadUserByUsername("alice@example.com")
        val compte = assertInstanceOf(CompteAuthentifie::class.java, userDetails)

        mockMvc.perform(get("/mes-taches").with(user(compte)))
            .andExpect(status().isOk)
            .andExpect(view().name("mes-taches"))
            .andExpect(model().attributeExists("taches"))
    }

    @Test
    fun formulaireCreationAccessibleWhenAuthenticated() {
        val userDetails = comptesUtilisateur.loadUserByUsername("alice@example.com")
        val compte = assertInstanceOf(CompteAuthentifie::class.java, userDetails)

        mockMvc.perform(get("/mes-taches/creer").with(user(compte)))
            .andExpect(status().isOk)
            .andExpect(view().name("formulaire-tache"))
            .andExpect(model().attributeExists("tacheForm"))
    }
}
