package com.safework.api;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.asset.model.ComplianceStatus;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.asset.repository.AssetTypeRepository;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.location.model.LocationType;
import com.safework.api.domain.location.repository.LocationRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data seeder for development environment sample data.
 * 
 * Provides sample data for development and testing. Only runs in 'dev' profile
 * and includes idempotency checks to prevent duplicate data creation.
 */
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final AssetRepository assetRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        // Force reload for debugging the data persistence issue
        System.out.println("Organizations in database: " + organizationRepository.count());
        System.out.println("Assets in database: " + assetRepository.count());
        
        if (organizationRepository.count() > 0) {
            System.out.println("Clearing existing data to force fresh reload...");
            clearExistingData();
        }
        
        loadSampleData();
    }
    
    private void clearExistingData() {
        System.out.println("Clearing existing data...");
        assetRepository.deleteAll();
        assetTypeRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        locationRepository.deleteAll();
        organizationRepository.deleteAll();
        System.out.println("Existing data cleared.");
    }

    @Transactional
    private void loadSampleData() {
        // 1. Create Organization
        Organization apexLogistics = new Organization();
        apexLogistics.setName("Apex Global Logistics");
        organizationRepository.save(apexLogistics);

        // 2. Create Departments
        Department warehouseDept = new Department();
        warehouseDept.setOrganization(apexLogistics);
        warehouseDept.setName("Warehouse Operations");
        warehouseDept.setDescription("Main warehouse and logistics operations");

        Department itDept = new Department();
        itDept.setOrganization(apexLogistics);
        itDept.setName("IT Department");
        itDept.setDescription("Information Technology and Systems");

        Department safetyDept = new Department();
        safetyDept.setOrganization(apexLogistics);
        safetyDept.setName("Safety & Compliance");
        safetyDept.setDescription("Workplace safety and regulatory compliance");

        departmentRepository.saveAll(List.of(warehouseDept, itDept, safetyDept));

        // 3. Create Locations
        Location mainWarehouse = new Location();
        mainWarehouse.setOrganization(apexLogistics);
        mainWarehouse.setName("Main Warehouse");
        mainWarehouse.setDescription("Primary storage and distribution facility");
        mainWarehouse.setLocationType(LocationType.WAREHOUSE);

        Location officeBuilding = new Location();
        officeBuilding.setOrganization(apexLogistics);
        officeBuilding.setName("Office Building");
        officeBuilding.setDescription("Administrative offices and meeting rooms");
        officeBuilding.setLocationType(LocationType.OFFICE);

        Location loadingDock = new Location();
        loadingDock.setOrganization(apexLogistics);
        loadingDock.setName("Loading Dock");
        loadingDock.setDescription("Truck loading and unloading area");
        loadingDock.setLocationType(LocationType.LOADING_DOCK);

        Location parkingLot = new Location();
        parkingLot.setOrganization(apexLogistics);
        parkingLot.setName("Parking Lot");
        parkingLot.setDescription("Employee and visitor vehicle parking");
        parkingLot.setLocationType(LocationType.YARD);

        locationRepository.saveAll(List.of(mainWarehouse, officeBuilding, loadingDock, parkingLot));

        // 4. Create Users with different roles
        User admin = new User();
        admin.setOrganization(apexLogistics);
        admin.setDepartment(itDept);
        admin.setName("Alice Admin");
        admin.setEmail("admin@apex.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(UserRole.ADMIN);

        User supervisor = new User();
        supervisor.setOrganization(apexLogistics);
        supervisor.setDepartment(warehouseDept);
        supervisor.setName("Bob Supervisor");
        supervisor.setEmail("supervisor@apex.com");
        supervisor.setPassword(passwordEncoder.encode("password"));
        supervisor.setRole(UserRole.SUPERVISOR);

        User inspector = new User();
        inspector.setOrganization(apexLogistics);
        inspector.setDepartment(safetyDept);
        inspector.setName("Charlie Inspector");
        inspector.setEmail("inspector@apex.com");
        inspector.setPassword(passwordEncoder.encode("password"));
        inspector.setRole(UserRole.INSPECTOR);

        User operator1 = new User();
        operator1.setOrganization(apexLogistics);
        operator1.setDepartment(warehouseDept);
        operator1.setName("David Operator");
        operator1.setEmail("operator1@apex.com");
        operator1.setPassword(passwordEncoder.encode("password"));
        operator1.setRole(UserRole.INSPECTOR);

        User operator2 = new User();
        operator2.setOrganization(apexLogistics);
        operator2.setDepartment(warehouseDept);
        operator2.setName("Emma Worker");
        operator2.setEmail("operator2@apex.com");
        operator2.setPassword(passwordEncoder.encode("password"));
        operator2.setRole(UserRole.INSPECTOR);

        userRepository.saveAll(List.of(admin, supervisor, inspector, operator1, operator2));

        // 5. Create Asset Types
        AssetType forkliftType = new AssetType();
        forkliftType.setOrganization(apexLogistics);
        forkliftType.setName("Forklift");

        AssetType safetyEquipType = new AssetType();
        safetyEquipType.setOrganization(apexLogistics);
        safetyEquipType.setName("Safety Equipment");

        AssetType vehicleType = new AssetType();
        vehicleType.setOrganization(apexLogistics);
        vehicleType.setName("Vehicle");

        AssetType itEquipType = new AssetType();
        itEquipType.setOrganization(apexLogistics);
        itEquipType.setName("IT Equipment");

        AssetType machineryType = new AssetType();
        machineryType.setOrganization(apexLogistics);
        machineryType.setName("Industrial Machinery");

        assetTypeRepository.saveAll(List.of(forkliftType, safetyEquipType, vehicleType, itEquipType, machineryType));

        // 6. Create Assets
        
        // Forklifts
        Asset forklift1 = new Asset();
        forklift1.setOrganization(apexLogistics);
        forklift1.setAssetType(forkliftType);
        forklift1.setDepartment(warehouseDept);
        forklift1.setLocation(mainWarehouse);
        forklift1.setAssetTag("APX-FL-001");
        forklift1.setName("Warehouse Forklift #1");
        forklift1.setQrCodeId("SN-APX-FL-001");
        forklift1.setStatus(AssetStatus.ACTIVE);
        forklift1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        forklift1.setAssignedTo(operator1);
        forklift1.setPurchaseDate(LocalDate.of(2024, 5, 10));
        forklift1.setPurchaseCost(new BigDecimal("25000.00"));
        forklift1.setWarrantyExpiryDate(LocalDate.of(2027, 5, 10));
        forklift1.setNextServiceDate(LocalDate.now().plusDays(30));
        forklift1.setCustomAttributes(Map.of(
                "model", "Hyster H50FT",
                "fuelType", "LPG",
                "maxCapacity", "5000 lbs"
        ));

        Asset forklift2 = new Asset();
        forklift2.setOrganization(apexLogistics);
        forklift2.setAssetType(forkliftType);
        forklift2.setDepartment(warehouseDept);
        forklift2.setLocation(loadingDock);
        forklift2.setAssetTag("APX-FL-002");
        forklift2.setName("Loading Dock Forklift #2");
        forklift2.setQrCodeId("SN-APX-FL-002");
        forklift2.setStatus(AssetStatus.UNDER_MAINTENANCE);
        forklift2.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
        forklift2.setAssignedTo(operator2);
        forklift2.setPurchaseDate(LocalDate.of(2023, 8, 15));
        forklift2.setPurchaseCost(new BigDecimal("28000.00"));
        forklift2.setWarrantyExpiryDate(LocalDate.of(2026, 8, 15));
        forklift2.setNextServiceDate(LocalDate.now().minusDays(5));
        forklift2.setCustomAttributes(Map.of(
                "model", "Toyota 8FGU25",
                "fuelType", "Propane",
                "maxCapacity", "5000 lbs"
        ));

        // Safety Equipment
        Asset fireExtinguisher1 = new Asset();
        fireExtinguisher1.setOrganization(apexLogistics);
        fireExtinguisher1.setAssetType(safetyEquipType);
        fireExtinguisher1.setDepartment(safetyDept);
        fireExtinguisher1.setLocation(mainWarehouse);
        fireExtinguisher1.setAssetTag("APX-FE-001");
        fireExtinguisher1.setName("Fire Extinguisher - Warehouse Entrance");
        fireExtinguisher1.setQrCodeId("SN-APX-FE-001");
        fireExtinguisher1.setStatus(AssetStatus.ACTIVE);
        fireExtinguisher1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        fireExtinguisher1.setAssignedTo(inspector);
        fireExtinguisher1.setPurchaseDate(LocalDate.of(2024, 1, 20));
        fireExtinguisher1.setPurchaseCost(new BigDecimal("85.00"));
        fireExtinguisher1.setNextServiceDate(LocalDate.now().plusMonths(6));
        fireExtinguisher1.setCustomAttributes(Map.of(
                "type", "ABC Dry Chemical",
                "capacity", "10 lbs",
                "certificationRequired", true
        ));

        Asset hardHat1 = new Asset();
        hardHat1.setOrganization(apexLogistics);
        hardHat1.setAssetType(safetyEquipType);
        hardHat1.setDepartment(warehouseDept);
        hardHat1.setLocation(mainWarehouse);
        hardHat1.setAssetTag("APX-HH-001");
        hardHat1.setName("Hard Hat - David");
        hardHat1.setQrCodeId("SN-APX-HH-001");
        hardHat1.setStatus(AssetStatus.ACTIVE);
        hardHat1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        hardHat1.setAssignedTo(operator1);
        hardHat1.setPurchaseDate(LocalDate.of(2024, 3, 1));
        hardHat1.setPurchaseCost(new BigDecimal("25.00"));
        hardHat1.setCustomAttributes(Map.of(
                "color", "Yellow",
                "size", "Large",
                "ANSICompliant", true
        ));

        // Vehicles
        Asset deliveryTruck1 = new Asset();
        deliveryTruck1.setOrganization(apexLogistics);
        deliveryTruck1.setAssetType(vehicleType);
        deliveryTruck1.setDepartment(warehouseDept);
        deliveryTruck1.setLocation(parkingLot);
        deliveryTruck1.setAssetTag("APX-VH-001");
        deliveryTruck1.setName("Delivery Truck #1");
        deliveryTruck1.setQrCodeId("SN-APX-VH-001");
        deliveryTruck1.setStatus(AssetStatus.ACTIVE);
        deliveryTruck1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        deliveryTruck1.setAssignedTo(supervisor);
        deliveryTruck1.setPurchaseDate(LocalDate.of(2023, 11, 5));
        deliveryTruck1.setPurchaseCost(new BigDecimal("45000.00"));
        deliveryTruck1.setWarrantyExpiryDate(LocalDate.of(2026, 11, 5));
        deliveryTruck1.setNextServiceDate(LocalDate.now().plusMonths(3));
        deliveryTruck1.setCustomAttributes(Map.of(
                "make", "Ford",
                "model", "Transit",
                "licensePlate", "APX-001",
                "VIN", "1FTBW2CM5GKA12345"
        ));

        // IT Equipment
        Asset laptop1 = new Asset();
        laptop1.setOrganization(apexLogistics);
        laptop1.setAssetType(itEquipType);
        laptop1.setDepartment(itDept);
        laptop1.setLocation(officeBuilding);
        laptop1.setAssetTag("APX-LT-001");
        laptop1.setName("Admin Laptop - Alice");
        laptop1.setQrCodeId("SN-APX-LT-001");
        laptop1.setStatus(AssetStatus.ACTIVE);
        laptop1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        laptop1.setAssignedTo(admin);
        laptop1.setPurchaseDate(LocalDate.of(2024, 2, 14));
        laptop1.setPurchaseCost(new BigDecimal("1200.00"));
        laptop1.setWarrantyExpiryDate(LocalDate.of(2027, 2, 14));
        laptop1.setCustomAttributes(Map.of(
                "brand", "Dell",
                "model", "Latitude 5520",
                "serialNumber", "DL123456789",
                "RAM", "16GB",
                "storage", "512GB SSD"
        ));

        Asset tablet1 = new Asset();
        tablet1.setOrganization(apexLogistics);
        tablet1.setAssetType(itEquipType);
        tablet1.setDepartment(safetyDept);
        tablet1.setLocation(mainWarehouse);
        tablet1.setAssetTag("APX-TB-001");
        tablet1.setName("Inspection Tablet - Charlie");
        tablet1.setQrCodeId("SN-APX-TB-001");
        tablet1.setStatus(AssetStatus.ACTIVE);
        tablet1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        tablet1.setAssignedTo(inspector);
        tablet1.setPurchaseDate(LocalDate.of(2024, 4, 10));
        tablet1.setPurchaseCost(new BigDecimal("800.00"));
        tablet1.setWarrantyExpiryDate(LocalDate.of(2026, 4, 10));
        tablet1.setCustomAttributes(Map.of(
                "brand", "Samsung",
                "model", "Galaxy Tab S8",
                "storageCapacity", "256GB",
                "screenSize", "11 inches"
        ));

        // Industrial Machinery
        Asset conveyorBelt1 = new Asset();
        conveyorBelt1.setOrganization(apexLogistics);
        conveyorBelt1.setAssetType(machineryType);
        conveyorBelt1.setDepartment(warehouseDept);
        conveyorBelt1.setLocation(mainWarehouse);
        conveyorBelt1.setAssetTag("APX-CB-001");
        conveyorBelt1.setName("Main Conveyor Belt System");
        conveyorBelt1.setQrCodeId("SN-APX-CB-001");
        conveyorBelt1.setStatus(AssetStatus.ACTIVE);
        conveyorBelt1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        conveyorBelt1.setAssignedTo(supervisor);
        conveyorBelt1.setPurchaseDate(LocalDate.of(2023, 6, 20));
        conveyorBelt1.setPurchaseCost(new BigDecimal("15000.00"));
        conveyorBelt1.setWarrantyExpiryDate(LocalDate.of(2028, 6, 20));
        conveyorBelt1.setNextServiceDate(LocalDate.now().plusMonths(2));
        conveyorBelt1.setCustomAttributes(Map.of(
                "length", "50 feet",
                "capacity", "500 lbs per linear foot",
                "speed", "Variable 0-100 FPM",
                "motorPower", "5 HP"
        ));

        Asset palletJack1 = new Asset();
        palletJack1.setOrganization(apexLogistics);
        palletJack1.setAssetType(machineryType);
        palletJack1.setDepartment(warehouseDept);
        palletJack1.setLocation(loadingDock);
        palletJack1.setAssetTag("APX-PJ-001");
        palletJack1.setName("Hydraulic Pallet Jack #1");
        palletJack1.setQrCodeId("SN-APX-PJ-001");
        palletJack1.setStatus(AssetStatus.ACTIVE);
        palletJack1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        palletJack1.setAssignedTo(operator2);
        palletJack1.setPurchaseDate(LocalDate.of(2024, 1, 8));
        palletJack1.setPurchaseCost(new BigDecimal("450.00"));
        palletJack1.setWarrantyExpiryDate(LocalDate.of(2026, 1, 8));
        palletJack1.setCustomAttributes(Map.of(
                "capacity", "5500 lbs",
                "forkLength", "48 inches",
                "liftHeight", "7.5 inches"
        ));

        // Additional Safety Equipment
        Asset safetyVest1 = new Asset();
        safetyVest1.setOrganization(apexLogistics);
        safetyVest1.setAssetType(safetyEquipType);
        safetyVest1.setDepartment(warehouseDept);
        safetyVest1.setLocation(mainWarehouse);
        safetyVest1.setAssetTag("APX-SV-001");
        safetyVest1.setName("Safety Vest - High Visibility");
        safetyVest1.setQrCodeId("SN-APX-SV-001");
        safetyVest1.setStatus(AssetStatus.ACTIVE);
        safetyVest1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        safetyVest1.setAssignedTo(operator1);
        safetyVest1.setPurchaseDate(LocalDate.of(2024, 3, 15));
        safetyVest1.setPurchaseCost(new BigDecimal("35.00"));
        safetyVest1.setCustomAttributes(Map.of(
                "size", "Large",
                "color", "Orange",
                "ANSI_Class", "2",
                "reflectiveStripes", true
        ));

        Asset firstAidKit1 = new Asset();
        firstAidKit1.setOrganization(apexLogistics);
        firstAidKit1.setAssetType(safetyEquipType);
        firstAidKit1.setDepartment(safetyDept);
        firstAidKit1.setLocation(mainWarehouse);
        firstAidKit1.setAssetTag("APX-FA-001");
        firstAidKit1.setName("First Aid Kit - Warehouse");
        firstAidKit1.setQrCodeId("SN-APX-FA-001");
        firstAidKit1.setStatus(AssetStatus.ACTIVE);
        firstAidKit1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        firstAidKit1.setAssignedTo(inspector);
        firstAidKit1.setPurchaseDate(LocalDate.of(2024, 1, 10));
        firstAidKit1.setPurchaseCost(new BigDecimal("150.00"));
        firstAidKit1.setNextServiceDate(LocalDate.now().plusMonths(12));
        firstAidKit1.setCustomAttributes(Map.of(
                "capacity", "50 person",
                "type", "Industrial",
                "lastRestocked", "2024-09-15"
        ));

        Asset emergencyEyeWash = new Asset();
        emergencyEyeWash.setOrganization(apexLogistics);
        emergencyEyeWash.setAssetType(safetyEquipType);
        emergencyEyeWash.setDepartment(safetyDept);
        emergencyEyeWash.setLocation(mainWarehouse);
        emergencyEyeWash.setAssetTag("APX-EW-001");
        emergencyEyeWash.setName("Emergency Eye Wash Station");
        emergencyEyeWash.setQrCodeId("SN-APX-EW-001");
        emergencyEyeWash.setStatus(AssetStatus.ACTIVE);
        emergencyEyeWash.setComplianceStatus(ComplianceStatus.COMPLIANT);
        emergencyEyeWash.setAssignedTo(inspector);
        emergencyEyeWash.setPurchaseDate(LocalDate.of(2023, 12, 5));
        emergencyEyeWash.setPurchaseCost(new BigDecimal("800.00"));
        emergencyEyeWash.setNextServiceDate(LocalDate.now().plusMonths(6));
        emergencyEyeWash.setCustomAttributes(Map.of(
                "type", "Plumbed",
                "flowRate", "3.0 GPM",
                "OSHACompliant", true
        ));

        // Additional IT Equipment
        Asset desktop1 = new Asset();
        desktop1.setOrganization(apexLogistics);
        desktop1.setAssetType(itEquipType);
        desktop1.setDepartment(itDept);
        desktop1.setLocation(officeBuilding);
        desktop1.setAssetTag("APX-DT-001");
        desktop1.setName("Desktop Computer - Reception");
        desktop1.setQrCodeId("SN-APX-DT-001");
        desktop1.setStatus(AssetStatus.ACTIVE);
        desktop1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        desktop1.setPurchaseDate(LocalDate.of(2023, 9, 20));
        desktop1.setPurchaseCost(new BigDecimal("900.00"));
        desktop1.setWarrantyExpiryDate(LocalDate.of(2026, 9, 20));
        desktop1.setCustomAttributes(Map.of(
                "brand", "HP",
                "model", "EliteDesk 800",
                "processor", "Intel i5-12500",
                "RAM", "16GB",
                "storage", "256GB SSD"
        ));

        Asset barcodeScanners = new Asset();
        barcodeScanners.setOrganization(apexLogistics);
        barcodeScanners.setAssetType(itEquipType);
        barcodeScanners.setDepartment(warehouseDept);
        barcodeScanners.setLocation(mainWarehouse);
        barcodeScanners.setAssetTag("APX-BS-001");
        barcodeScanners.setName("Barcode Scanner Set (5 units)");
        barcodeScanners.setQrCodeId("SN-APX-BS-001");
        barcodeScanners.setStatus(AssetStatus.ACTIVE);
        barcodeScanners.setComplianceStatus(ComplianceStatus.COMPLIANT);
        barcodeScanners.setAssignedTo(supervisor);
        barcodeScanners.setPurchaseDate(LocalDate.of(2024, 6, 1));
        barcodeScanners.setPurchaseCost(new BigDecimal("750.00"));
        barcodeScanners.setWarrantyExpiryDate(LocalDate.of(2027, 6, 1));
        barcodeScanners.setCustomAttributes(Map.of(
                "brand", "Zebra",
                "model", "DS3608",
                "quantity", "5",
                "connectionType", "Wireless"
        ));

        // Additional Vehicles
        Asset van1 = new Asset();
        van1.setOrganization(apexLogistics);
        van1.setAssetType(vehicleType);
        van1.setDepartment(warehouseDept);
        van1.setLocation(parkingLot);
        van1.setAssetTag("APX-VN-001");
        van1.setName("Service Van #1");
        van1.setQrCodeId("SN-APX-VN-001");
        van1.setStatus(AssetStatus.ACTIVE);
        van1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        van1.setAssignedTo(supervisor);
        van1.setPurchaseDate(LocalDate.of(2024, 1, 15));
        van1.setPurchaseCost(new BigDecimal("35000.00"));
        van1.setWarrantyExpiryDate(LocalDate.of(2027, 1, 15));
        van1.setNextServiceDate(LocalDate.now().plusMonths(2));
        van1.setCustomAttributes(Map.of(
                "make", "Chevrolet",
                "model", "Express 2500",
                "licensePlate", "APX-002",
                "VIN", "1GCWGAFG3K1234567",
                "mileage", "15,245"
        ));

        // Additional Machinery
        Asset electricHoist = new Asset();
        electricHoist.setOrganization(apexLogistics);
        electricHoist.setAssetType(machineryType);
        electricHoist.setDepartment(warehouseDept);
        electricHoist.setLocation(mainWarehouse);
        electricHoist.setAssetTag("APX-EH-001");
        electricHoist.setName("Electric Chain Hoist");
        electricHoist.setQrCodeId("SN-APX-EH-001");
        electricHoist.setStatus(AssetStatus.ACTIVE);
        electricHoist.setComplianceStatus(ComplianceStatus.COMPLIANT);
        electricHoist.setAssignedTo(supervisor);
        electricHoist.setPurchaseDate(LocalDate.of(2023, 7, 10));
        electricHoist.setPurchaseCost(new BigDecimal("3500.00"));
        electricHoist.setWarrantyExpiryDate(LocalDate.of(2026, 7, 10));
        electricHoist.setNextServiceDate(LocalDate.now().plusMonths(3));
        electricHoist.setCustomAttributes(Map.of(
                "capacity", "2 tons",
                "liftHeight", "20 feet",
                "voltage", "480V",
                "certificationRequired", true
        ));

        Asset loadingRamp = new Asset();
        loadingRamp.setOrganization(apexLogistics);
        loadingRamp.setAssetType(machineryType);
        loadingRamp.setDepartment(warehouseDept);
        loadingRamp.setLocation(loadingDock);
        loadingRamp.setAssetTag("APX-LR-001");
        loadingRamp.setName("Hydraulic Loading Ramp");
        loadingRamp.setQrCodeId("SN-APX-LR-001");
        loadingRamp.setStatus(AssetStatus.ACTIVE);
        loadingRamp.setComplianceStatus(ComplianceStatus.COMPLIANT);
        loadingRamp.setAssignedTo(operator2);
        loadingRamp.setPurchaseDate(LocalDate.of(2023, 4, 8));
        loadingRamp.setPurchaseCost(new BigDecimal("8500.00"));
        loadingRamp.setWarrantyExpiryDate(LocalDate.of(2028, 4, 8));
        loadingRamp.setNextServiceDate(LocalDate.now().plusMonths(4));
        loadingRamp.setCustomAttributes(Map.of(
                "capacity", "15,000 lbs",
                "width", "84 inches",
                "adjustableHeight", "48-60 inches",
                "powerSource", "Hydraulic"
        ));

        // Create Office Equipment Asset Type
        AssetType officeEquipType = new AssetType();
        officeEquipType.setOrganization(apexLogistics);
        officeEquipType.setName("Office Equipment");

        // Create Tools Asset Type
        AssetType toolsType = new AssetType();
        toolsType.setOrganization(apexLogistics);
        toolsType.setName("Tools");

        // Save new asset types
        assetTypeRepository.saveAll(List.of(officeEquipType, toolsType));

        // Office Equipment
        Asset conferenceTable = new Asset();
        conferenceTable.setOrganization(apexLogistics);
        conferenceTable.setAssetType(officeEquipType);
        conferenceTable.setDepartment(itDept);
        conferenceTable.setLocation(officeBuilding);
        conferenceTable.setAssetTag("APX-CT-001");
        conferenceTable.setName("Conference Room Table");
        conferenceTable.setQrCodeId("SN-APX-CT-001");
        conferenceTable.setStatus(AssetStatus.ACTIVE);
        conferenceTable.setComplianceStatus(ComplianceStatus.COMPLIANT);
        conferenceTable.setPurchaseDate(LocalDate.of(2023, 8, 20));
        conferenceTable.setPurchaseCost(new BigDecimal("1200.00"));
        conferenceTable.setCustomAttributes(Map.of(
                "material", "Oak Wood",
                "capacity", "12 people",
                "dimensions", "10ft x 4ft",
                "color", "Dark Brown"
        ));

        Asset projector = new Asset();
        projector.setOrganization(apexLogistics);
        projector.setAssetType(officeEquipType);
        projector.setDepartment(itDept);
        projector.setLocation(officeBuilding);
        projector.setAssetTag("APX-PJ-002");
        projector.setName("Conference Room Projector");
        projector.setQrCodeId("SN-APX-PJ-002");
        projector.setStatus(AssetStatus.ACTIVE);
        projector.setComplianceStatus(ComplianceStatus.COMPLIANT);
        projector.setPurchaseDate(LocalDate.of(2024, 2, 28));
        projector.setPurchaseCost(new BigDecimal("1800.00"));
        projector.setWarrantyExpiryDate(LocalDate.of(2027, 2, 28));
        projector.setCustomAttributes(Map.of(
                "brand", "Epson",
                "model", "PowerLite L610U",
                "resolution", "1920x1200",
                "brightness", "6000 lumens"
        ));

        // Tools
        Asset toolSet1 = new Asset();
        toolSet1.setOrganization(apexLogistics);
        toolSet1.setAssetType(toolsType);
        toolSet1.setDepartment(warehouseDept);
        toolSet1.setLocation(mainWarehouse);
        toolSet1.setAssetTag("APX-TS-001");
        toolSet1.setName("Maintenance Tool Set");
        toolSet1.setQrCodeId("SN-APX-TS-001");
        toolSet1.setStatus(AssetStatus.ACTIVE);
        toolSet1.setComplianceStatus(ComplianceStatus.COMPLIANT);
        toolSet1.setAssignedTo(supervisor);
        toolSet1.setPurchaseDate(LocalDate.of(2024, 3, 5));
        toolSet1.setPurchaseCost(new BigDecimal("450.00"));
        toolSet1.setCustomAttributes(Map.of(
                "brand", "Craftsman",
                "pieces", "120",
                "case", "Rolling Tool Chest",
                "socketSizes", "SAE and Metric"
        ));

        Asset drillSet = new Asset();
        drillSet.setOrganization(apexLogistics);
        drillSet.setAssetType(toolsType);
        drillSet.setDepartment(warehouseDept);
        drillSet.setLocation(mainWarehouse);
        drillSet.setAssetTag("APX-DR-001");
        drillSet.setName("Cordless Drill Set");
        drillSet.setQrCodeId("SN-APX-DR-001");
        drillSet.setStatus(AssetStatus.ACTIVE);
        drillSet.setComplianceStatus(ComplianceStatus.COMPLIANT);
        drillSet.setAssignedTo(operator1);
        drillSet.setPurchaseDate(LocalDate.of(2024, 4, 18));
        drillSet.setPurchaseCost(new BigDecimal("280.00"));
        drillSet.setCustomAttributes(Map.of(
                "brand", "DeWalt",
                "model", "DCD771C2",
                "voltage", "20V MAX",
                "batteryCount", "2",
                "chuckSize", "1/2 inch"
        ));

        // Additional safety equipment with different statuses
        Asset hardHat2 = new Asset();
        hardHat2.setOrganization(apexLogistics);
        hardHat2.setAssetType(safetyEquipType);
        hardHat2.setDepartment(warehouseDept);
        hardHat2.setLocation(mainWarehouse);
        hardHat2.setAssetTag("APX-HH-002");
        hardHat2.setName("Hard Hat - Emma");
        hardHat2.setQrCodeId("SN-APX-HH-002");
        hardHat2.setStatus(AssetStatus.ACTIVE);
        hardHat2.setComplianceStatus(ComplianceStatus.COMPLIANT);
        hardHat2.setAssignedTo(operator2);
        hardHat2.setPurchaseDate(LocalDate.of(2024, 3, 1));
        hardHat2.setPurchaseCost(new BigDecimal("25.00"));
        hardHat2.setCustomAttributes(Map.of(
                "color", "White",
                "size", "Medium",
                "ANSICompliant", true
        ));

        Asset fireExtinguisher2 = new Asset();
        fireExtinguisher2.setOrganization(apexLogistics);
        fireExtinguisher2.setAssetType(safetyEquipType);
        fireExtinguisher2.setDepartment(safetyDept);
        fireExtinguisher2.setLocation(loadingDock);
        fireExtinguisher2.setAssetTag("APX-FE-002");
        fireExtinguisher2.setName("Fire Extinguisher - Loading Dock");
        fireExtinguisher2.setQrCodeId("SN-APX-FE-002");
        fireExtinguisher2.setStatus(AssetStatus.UNDER_MAINTENANCE);
        fireExtinguisher2.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
        fireExtinguisher2.setAssignedTo(inspector);
        fireExtinguisher2.setPurchaseDate(LocalDate.of(2023, 11, 15));
        fireExtinguisher2.setPurchaseCost(new BigDecimal("95.00"));
        fireExtinguisher2.setNextServiceDate(LocalDate.now().minusDays(10));
        fireExtinguisher2.setCustomAttributes(Map.of(
                "type", "CO2",
                "capacity", "15 lbs",
                "certificationRequired", true,
                "lastInspection", "2024-06-15"
        ));

        // Decommissioned asset for variety
        Asset oldPrinter = new Asset();
        oldPrinter.setOrganization(apexLogistics);
        oldPrinter.setAssetType(itEquipType);
        oldPrinter.setDepartment(itDept);
        oldPrinter.setLocation(officeBuilding);
        oldPrinter.setAssetTag("APX-PR-001");
        oldPrinter.setName("Old Office Printer");
        oldPrinter.setQrCodeId("SN-APX-PR-001");
        oldPrinter.setStatus(AssetStatus.DECOMMISSIONED);
        oldPrinter.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
        oldPrinter.setPurchaseDate(LocalDate.of(2020, 3, 15));
        oldPrinter.setPurchaseCost(new BigDecimal("300.00"));
        oldPrinter.setDisposalDate(LocalDate.of(2024, 9, 30));
        oldPrinter.setCustomAttributes(Map.of(
                "brand", "HP",
                "model", "LaserJet Pro",
                "reason", "End of life, replaced with newer model"
        ));

        List<Asset> savedAssets = assetRepository.saveAll(List.of(
                forklift1, forklift2, fireExtinguisher1, hardHat1, deliveryTruck1,
                laptop1, tablet1, conveyorBelt1, palletJack1, 
                safetyVest1, firstAidKit1, emergencyEyeWash, desktop1, barcodeScanners,
                van1, electricHoist, loadingRamp, conferenceTable, projector,
                toolSet1, drillSet, hardHat2, fireExtinguisher2, oldPrinter
        ));

        // Verify asset persistence
        System.out.println("Assets saved to database: " + savedAssets.size());
        System.out.println("Assets in database after save: " + assetRepository.count());
        System.out.println("Assets for organization " + apexLogistics.getId() + ": " + 
                          assetRepository.findAllByOrganizationId(apexLogistics.getId(), 
                          org.springframework.data.domain.PageRequest.of(0, 50)).getTotalElements());

        System.out.println("========================================");
        System.out.println("Sample data loaded for 'dev' profile:");
        System.out.println("- 1 Organization: " + apexLogistics.getName());
        System.out.println("- 3 Departments: Warehouse, IT, Safety");
        System.out.println("- 4 Locations: Warehouse, Office, Loading Dock, Parking");
        System.out.println("- 5 Users: 1 Admin, 1 Supervisor, 3 Inspectors");
        System.out.println("- 7 Asset Types: Forklift, Safety Equipment, Vehicle, IT Equipment, Machinery, Office Equipment, Tools");
        System.out.println("- 24 Assets: Various statuses and compliance levels");
        System.out.println("  • Safety Equipment: 7 (vests, hard hats, fire extinguishers, first aid, eye wash)");
        System.out.println("  • IT Equipment: 4 (laptops, tablets, desktops, scanners)");
        System.out.println("  • Vehicles: 2 (truck, van)");
        System.out.println("  • Machinery: 4 (forklifts, conveyor belt, pallet jack, hoist, loading ramp)");
        System.out.println("  • Office Equipment: 2 (conference table, projector)");
        System.out.println("  • Tools: 2 (tool set, drill set)");
        System.out.println("  • Decommissioned: 1 (old printer)");
        System.out.println("========================================");
    }
}
