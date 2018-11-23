package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yhshan.projet.entites.Role;

public interface RoleDao extends JpaRepository<Role,Integer> {
}
