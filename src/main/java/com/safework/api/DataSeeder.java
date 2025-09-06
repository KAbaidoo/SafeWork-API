package com.safework.api;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.asset.repository.AssetTypeRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.repository.OrganizationRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("dev") // IMPORTANT: This component only runs when the 'dev' profile is active
public class DataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final AssetRepository assetRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        // Idempotency check: only run if the database is empty
        if (organizationRepository.count() == 0) {
            loadSampleData();
        }
    }

    private void loadSampleData() {
        // 1. Create Organization
        Organization apexLogistics = new Organization();
        apexLogistics.setName("Apex Global Logistics");
        organizationRepository.save(apexLogistics);

        // 2. Create Users with different roles
        User admin = new User();
        admin.setOrganization(apexLogistics);
        admin.setName("Alice Admin");
        admin.setEmail("admin@apex.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(UserRole.ADMIN);

        User supervisor = new User();
        supervisor.setOrganization(apexLogistics);
        supervisor.setName("Bob Supervisor");
        supervisor.setEmail("supervisor@apex.com");
        supervisor.setPassword(passwordEncoder.encode("password"));
        supervisor.setRole(UserRole.SUPERVISOR);

        User inspector = new User();
        inspector.setOrganization(apexLogistics);
        inspector.setName("Charlie Inspector");
        inspector.setEmail("inspector@apex.com");
        inspector.setPassword(passwordEncoder.encode("password"));
        inspector.setRole(UserRole.INSPECTOR);

        userRepository.saveAll(List.of(admin, supervisor, inspector));

        // 3. Create an Asset Type
        AssetType forkliftType = new AssetType();
        forkliftType.setOrganization(apexLogistics);
        forkliftType.setName("Forklift");
        assetTypeRepository.save(forkliftType);

        // 4. Create an Asset
        Asset forklift1 = new Asset();
        forklift1.setOrganization(apexLogistics);
        forklift1.setAssetType(forkliftType);
        forklift1.setAssetTag("APX-FL-001");
        forklift1.setName("Warehouse Forklift #1");
        forklift1.setQrCodeId("SN-APX-FL-001");
        forklift1.setStatus(AssetStatus.ACTIVE);
        forklift1.setAssignedTo(inspector);
        forklift1.setPurchaseDate(LocalDate.of(2024, 5, 10));
        forklift1.setPurchaseCost(new BigDecimal("25000.00"));
        forklift1.setCustomAttributes(Map.of(
                "model", "Hyster H50FT",
                "fuelType", "LPG"
        ));

        assetRepository.save(forklift1);

        System.out.println("----------------------------------------");
        System.out.println("Sample data loaded for 'dev' profile.");
        System.out.println("----------------------------------------");
    }
}
