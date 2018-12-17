package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yhshan.projet.entites.Compte;
import yhshan.projet.entites.Examen;

import java.util.List;

public interface ExamenDao  extends JpaRepository<Examen,Integer> {

    List<Examen> findAllByEvalueOrderByDateAsc(Compte evalue);
}
