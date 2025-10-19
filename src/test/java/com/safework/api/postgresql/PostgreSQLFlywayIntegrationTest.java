package com.safework.api.postgresql;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("postgresql-test")
@Transactional
class PostgreSQLFlywayIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Test
    void testPostgreSQLConnectionAndFlywayTables() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Verify we're connected to PostgreSQL
            assertThat(metaData.getDatabaseProductName()).isEqualTo("PostgreSQL");
            
            // Check if Flyway history table exists
            try (ResultSet tables = metaData.getTables(null, null, "flyway_schema_history", null)) {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    void testSchemaTablesExist() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            List<String> expectedTables = List.of(
                "organizations", "users", "departments", "locations", 
                "asset_types", "assets", "suppliers"
            );
            
            List<String> foundTables = new ArrayList<>();
            try (ResultSet tables = metaData.getTables(null, "public", null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (expectedTables.contains(tableName)) {
                        foundTables.add(tableName);
                    }
                }
            }
            
            assertThat(foundTables).containsAll(expectedTables);
        }
    }

    @Test
    void testJsonbColumnExists() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check if custom_attributes column exists in assets table
            try (ResultSet columns = metaData.getColumns(null, "public", "assets", "custom_attributes")) {
                assertThat(columns.next()).isTrue();
                String columnType = columns.getString("TYPE_NAME");
                assertThat(columnType).isEqualTo("jsonb");
            }
        }
    }

    @Test
    void testBasicCRUDOperations() {
        // Test basic database operations work with PostgreSQL
        Organization org = new Organization();
        org.setName("Test Organization");
        org.setAddress("123 Test St");
        org.setWebsite("https://test.com");
        org.setIndustry("Technology");
        Organization savedOrg = organizationRepository.save(org);
        
        assertThat(savedOrg.getId()).isNotNull();
        assertThat(savedOrg.getName()).isEqualTo("Test Organization");
        
        // Verify we can find it
        Organization foundOrg = organizationRepository.findById(savedOrg.getId()).orElse(null);
        assertThat(foundOrg).isNotNull();
        assertThat(foundOrg.getName()).isEqualTo("Test Organization");
    }

    @Test
    void testJsonbColumnFunctionality() {
        // Create organization first
        Organization org = new Organization();
        org.setName("JSONB Test Org");
        org.setAddress("456 JSONB St");
        org.setWebsite("https://jsonb.com");
        org.setIndustry("Testing");
        Organization savedOrg = organizationRepository.save(org);
        
        // Create asset with JSONB data
        Asset asset = new Asset();
        asset.setAssetTag("JSONB-001");
        asset.setName("JSONB Test Asset");
        asset.setOrganization(savedOrg);
        asset.setCustomAttributes(Map.of(
            "manufacturer", "TestCorp",
            "model", "T-1000",
            "specifications", Map.of(
                "power", "100W",
                "voltage", "220V"
            )
        ));
        
        Asset savedAsset = assetRepository.save(asset);
        
        // Verify JSONB data is saved and retrieved correctly
        Asset foundAsset = assetRepository.findById(savedAsset.getId()).orElse(null);
        assertThat(foundAsset).isNotNull();
        assertThat(foundAsset.getCustomAttributes()).isNotNull();
        assertThat(foundAsset.getCustomAttributes().get("manufacturer")).isEqualTo("TestCorp");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> specs = (Map<String, Object>) foundAsset.getCustomAttributes().get("specifications");
        assertThat(specs.get("power")).isEqualTo("100W");
    }
}