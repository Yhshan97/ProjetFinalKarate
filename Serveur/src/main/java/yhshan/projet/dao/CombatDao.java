package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yhshan.projet.entites.Combat;

import java.util.List;

public interface CombatDao extends JpaRepository<Combat, Integer> {

    List<Combat> findAllByArbitre (String arbitre);

    List<Combat> findAllByDateBetweenOrderByDateAsc(Long after, Long before);
}
