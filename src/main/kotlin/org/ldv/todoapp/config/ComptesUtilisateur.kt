package org.ldv.todoapp.config

import org.ldv.todoapp.model.repository.UtilisateurRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ComptesUtilisateur(
    private val utilisateurRepository: UtilisateurRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val utilisateur = utilisateurRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("Compte indisponible")
        val empreinte = utilisateur.empreinteMotDePasse
            ?: throw UsernameNotFoundException("Compte indisponible")
        val role = utilisateur.role
            ?: throw UsernameNotFoundException("Compte indisponible")

        return CompteAuthentifie(
            requireNotNull(utilisateur.idUtilisateur), utilisateur.email, empreinte, role.nom
        )
    }
}

// L'identifiant reste stable même après une modification de l'adresse e-mail.
class CompteAuthentifie(
    val idUtilisateur: Long,
    email: String,
    empreinte: String,
    role: String
) : User(email, empreinte, listOf(SimpleGrantedAuthority(role)))