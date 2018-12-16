package yhshan.projet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import yhshan.projet.dao.*;
import yhshan.projet.entites.Combat;
import yhshan.projet.entites.Compte;

import java.util.List;

@SpringBootApplication
public class ProjetApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ProjetApplication.class, args);
    }
/*
    @Autowired
    private CompteDao comptedao;

    @Autowired
    private RoleDao roledao;

    @Autowired
    private GroupeDao groupedao;

    @Autowired
    private AvatarDao avatarDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CombatDao combatDao;
*/
    @Override
    public void run(String... args)throws Exception{

        /*Compte compte1 = new Compte("a", "a_a", passwordEncoder.encode("a"), 0, 0,0,
        new Long(000), avatarDao.getOne(23), roledao.getOne(1),groupedao.getOne(1), null, null,
                null, null, null);
/*
        Compte compte2 = new Compte("b",passwordEncoder.encode("b"),"Compte B",
                avatarDao.getOne(4),roledao.getOne(3),groupedao.getOne(1));

        Compte compte3 = new Compte("venerable",passwordEncoder.encode("wow"),"Maitre",
                avatarDao.getOne(2),roledao.getOne(0),groupedao.getOne(7));

        Compte compte4 = new Compte("c",passwordEncoder.encode("c"),"compte C",
                avatarDao.getOne(3),roledao.getOne(3),groupedao.getOne(1));


        //comptedao.save(compte1);
        /*
        comptedao.save(compte2);
        comptedao.save(compte3);
        comptedao.save(compte4);
        comptedao.save(compte5);
        comptedao.save(compte6);

        System.out.println(comptedao.findAll());
        System.out.println(roledao.findAll());
        System.out.println(groupedao.findAll());
*/
    }
}
