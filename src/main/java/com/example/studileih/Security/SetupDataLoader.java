package com.example.studileih.Security;

import com.example.studileih.Dto.CityEnum;
import com.example.studileih.Entity.Privilege;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.Role;
import com.example.studileih.Entity.User;
import com.example.studileih.Repository.PrivilegeRepository;
import com.example.studileih.Repository.RoleRepository;
import com.example.studileih.Repository.UserRepository;
import com.example.studileih.Service.DormService;
import com.example.studileih.Service.ProductBuilder;
import com.example.studileih.Service.ProductService;
import com.example.studileih.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DormService dormService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup)
            return;
        Privilege readPrivilege
                = createPrivilegeIfNotFound("READ_PRIVILEGE");
        Privilege writePrivilege
                = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

        List<Privilege> adminPrivileges = Arrays.asList(
                readPrivilege, writePrivilege);
        createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound("ROLE_USER", Arrays.asList(readPrivilege));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        Role userRole = roleRepository.findByName("ROLE_USER");
        User user = new User();
        user.setName("Test");
        user.setPassword(passwordEncoder.encode("test"));
        user.setEmail("test@test.com");
        user.setRoles(Arrays.asList(adminRole));
        user.setEnabled(true);
        userRepository.save(user);

        if (productService.listAllProducts().isEmpty()) {

//            Role role = new Role("ROLE_USER");
//            roleRepository.save(role);

            Product product1 = new ProductBuilder().withTitle("VW 3er Golf, BJ. 1998, 100.000km").withDescription("Mein VW Golf zum Ausleihen, wiedersehen macht Freude höhö").withPrice(30).withAvailable(false).withDorm("Alexanderstraße").withCity(CityEnum.Stuttgart.toString()).build();
            Product product2 = new ProductBuilder().withTitle("Bosch Bohrmaschine").withDescription("Haralds Bohrmaschine").withPrice(0).withIsBeerOk(true).withCategory("Werkzeug").withAvailable(true).withDorm("Alexanderstraße").withCity(CityEnum.Stuttgart.toString()).build();
            Product product3 = new ProductBuilder().withTitle("Hartmuts Bohrmaschine").withDescription("Hartmuts Bohrmaschine").withPrice(5).withIsBeerOk(true).withCategory("Werkzeug").withAvailable(true).withDorm("Anna-Herrigel-Haus").withCity(CityEnum.Stuttgart.toString()).build();
            List<Product> haraldsList = Stream.of(product1, product2).collect(Collectors.toList());
            List<Product> hartmutsList = Stream.of(product3).collect(Collectors.toList());
            String HaraldsEncodedPassword = passwordEncoder.encode("2345");
            String HartmutsEncodedPassword = passwordEncoder.encode("5432");
            User harald = new User("Harald", "grg.kro@gmail.com", HaraldsEncodedPassword, haraldsList, dormService.getDormById(1L).get());
            User hartmut = new User("Hartmut", "georgkromer@pm.me", HartmutsEncodedPassword, hartmutsList, dormService.getDormById(2L).get());
            harald.setRoles(Arrays.asList(adminRole));
            hartmut.setRoles(Arrays.asList(userRole));
            harald.setEnabled(true);
            hartmut.setEnabled(true);
            product1.setUser(harald);
            product2.setUser(harald);
            product3.setUser(hartmut);

            // durch das Speichern der user werden die verknüpften Produkte auch gespeichert. Es ist also unnötig die Produkte mit productService.saveProduct() nochmal zu speichern.
            userService.saveOrUpdateUser(harald);
            userService.saveOrUpdateUser(hartmut);
        }

        alreadySetup = true;
    }

//    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

//    @Transactional
    Role createRoleIfNotFound(
            String name, Collection<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }

        return role;
    }
}