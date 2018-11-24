package yhshan.projet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import yhshan.projet.dao.*;
import yhshan.projet.entites.Combat;
import yhshan.projet.entites.Compte;

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
/*
        Compte compte1 = new Compte("a",passwordEncoder.encode("a"),"abc",
                avatarDao.getOne("Suricate"),roledao.getOne(3),groupedao.getOne(1));

        Compte compte2 = new Compte("b",passwordEncoder.encode("b"),"Compte B",
                avatarDao.getOne("Sorcière"),roledao.getOne(3),groupedao.getOne(1));

        Compte compte3 = new Compte("venerable",passwordEncoder.encode("wow"),"Maitre",
                avatarDao.getOne("James Bond 2"),roledao.getOne(0),groupedao.getOne(7));

        Compte compte4 = new Compte("c",passwordEncoder.encode("c"),"compte C",
                avatarDao.getOne("James Bond"),roledao.getOne(3),groupedao.getOne(1));

        Compte compte5 = new Compte("d",passwordEncoder.encode("d"),"compte D",
                avatarDao.getOne("Empereur"),roledao.getOne(3),groupedao.getOne(1));

        Compte compte6 = new Compte("e",passwordEncoder.encode("e"),"compte Ancien",
                avatarDao.getOne("Singe"),roledao.getOne(3),groupedao.getOne(1));


        for (int x=0;x<30;x++) {
            Combat c = new Combat("a", "b", "d", "draw");
            combatDao.save(c);
        }

        for (int x=0;x<36;x++) {
            Combat c = new Combat("a", "b", "c", "draw");
            combatDao.save(c);
        }

        compte1.setPoints(10);
        compte1.setCredits(123);
        compte1.setGroupe(groupedao.getOne(2));

        compte2.setPoints(20);
        compte2.setRole(roledao.getOne(2));

        compte3.setGroupe(groupedao.getOne(4));

        compte4.setPoints(50);
        compte4.setCredits(10);

        compte5.setCredits(25);

        compte6.setRole(roledao.getOne(2));

        comptedao.save(compte1);
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
