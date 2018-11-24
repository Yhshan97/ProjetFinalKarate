package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yhshan.projet.entites.Groupe;

public interface GroupeDao extends JpaRepository<Groupe,Integer> {
}
