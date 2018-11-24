package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import yhshan.projet.entites.Compte;
import yhshan.projet.entites.Groupe;
import yhshan.projet.entites.Role;

import javax.transaction.Transactional;
import java.util.List;

public interface CompteDao extends JpaRepository<Compte,String> {

/*
    @Modifying
    @Query("update Compte u set u.points = ?1 where u.courriel = ?2")
    @Transactional
    void setComptePtsById(int pts, String courriel);

    @Modifying
    @Query("update Compte u set u.credits = ?1 where u.courriel = ?2")
    @Transactional
    void setCompteCreditsById(int crds, String courriel);

    @Modifying
    @Query("update Compte u set u.credits = ?1,u.points = ?2, u.groupe = ?3 where u.courriel = ?4")
    @Transactional
    void passer(int crds, int pts, Groupe idGrp, String courriel);

    @Modifying
    @Query("update Compte u set u.credits = ?1,u.honte = ?2 where u.courriel = ?3")
    @Transactional
    void couler(int crds,boolean honte, String courriel);

    @Modifying
    @Query("update Compte u set u.role = ?1 where u.courriel = ?2")
    @Transactional
    void transferrer(Role role, String courriel);

    List<Compte> findByPointsAndCreditsGreaterThan(int points, int credits);

*/

}
