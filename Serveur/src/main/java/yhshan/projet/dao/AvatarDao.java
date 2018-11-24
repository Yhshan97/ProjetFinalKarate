package yhshan.projet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yhshan.projet.entites.Avatar;

public interface AvatarDao extends JpaRepository<Avatar, Integer> {

}
