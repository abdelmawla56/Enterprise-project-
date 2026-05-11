package edu.csai.youssef_service;

import edu.csai.youssef_service.entity.Tenant;
import edu.csai.youssef_service.entity.User;
import edu.csai.youssef_service.repository.TenantRepository;
import edu.csai.youssef_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (tenantRepository.count() == 0) {
            Tenant acme = tenantRepository.save(Tenant.builder()
                    .name("Acme Corp")
                    .plan("ENTERPRISE")
                    .build());

            Tenant globex = tenantRepository.save(Tenant.builder()
                    .name("Globex")
                    .plan("BASIC")
                    .build());

            User acmeUser = User.builder()
                    .email("admin@acme.com")
                    .password(passwordEncoder.encode("password"))
                    .roles("TENANT_ADMIN")
                    .build();
            acmeUser.setTenantId(acme.getId());
            userRepository.save(acmeUser);

            User globexUser = User.builder()
                    .email("user@globex.com")
                    .password(passwordEncoder.encode("password"))
                    .roles("TENANT_USER")
                    .build();
            globexUser.setTenantId(globex.getId());
            userRepository.save(globexUser);
            
            userRepository.flush();
        }
    }
}

