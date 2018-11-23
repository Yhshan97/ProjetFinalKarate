package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yhshan.projet.entites.Examen;

public interface ExamenDao  extends JpaRepository<Examen,String> {
}
