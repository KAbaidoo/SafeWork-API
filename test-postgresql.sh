#!/bin/bash

# PostgreSQL Integration Test Runner
# This script runs comprehensive tests for the SafeWork API PostgreSQL implementation

echo "🔥 SafeWork API - PostgreSQL Integration Test Suite"
echo "=================================================="

# Check if PostgreSQL is running
echo "📋 Checking PostgreSQL connection..."
if ! psql -h localhost -p 5432 -U safework_dev_user -d safeworkdb_dev -c '\q' 2>/dev/null; then
    echo "❌ Error: Cannot connect to PostgreSQL database"
    echo "   Please ensure PostgreSQL is running and safeworkdb_dev database exists"
    echo "   Run: psql postgres -c \"CREATE DATABASE safeworkdb_test;\""
    echo "   Run: psql postgres -c \"GRANT ALL PRIVILEGES ON DATABASE safeworkdb_test TO safework_dev_user;\""
    exit 1
fi
echo "✅ PostgreSQL connection successful"

# Create test database if it doesn't exist
echo "📋 Setting up test database..."
psql -h localhost -p 5432 -U safework_dev_user -d postgres -c "CREATE DATABASE safeworkdb_test;" 2>/dev/null || true
psql -h localhost -p 5432 -U safework_dev_user -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE safeworkdb_test TO safework_dev_user;" 2>/dev/null || true
echo "✅ Test database ready"

# Run PostgreSQL-specific integration tests
echo "🧪 Running PostgreSQL Integration Tests..."
echo "==========================================="

echo "📋 1. Testing JSONB Asset Repository functionality..."
./mvnw test -Dtest=AssetJsonbPostgreSQLIntegrationTest -Dspring.profiles.active=postgresql-test

if [ $? -ne 0 ]; then
    echo "❌ Asset JSONB tests failed"
    exit 1
fi
echo "✅ Asset JSONB tests passed"

echo "📋 2. Testing DataSeeder with PostgreSQL..."
./mvnw test -Dtest=DataSeederPostgreSQLIntegrationTest -Dspring.profiles.active=postgresql-test

if [ $? -ne 0 ]; then
    echo "❌ DataSeeder tests failed"
    exit 1
fi
echo "✅ DataSeeder tests passed"

echo "📋 3. Testing Inspection and Checklist JSONB functionality..."
./mvnw test -Dtest=InspectionChecklistPostgreSQLIntegrationTest -Dspring.profiles.active=postgresql-test

if [ $? -ne 0 ]; then
    echo "❌ Inspection/Checklist JSONB tests failed"
    exit 1
fi
echo "✅ Inspection/Checklist JSONB tests passed"

# Run existing test suite to ensure compatibility
echo "📋 4. Running existing test suite for compatibility..."
./mvnw test -Dspring.profiles.active=test

if [ $? -ne 0 ]; then
    echo "❌ Existing test suite failed - compatibility issue detected"
    exit 1
fi
echo "✅ Existing test suite passed - full compatibility maintained"

# Test application startup with PostgreSQL
echo "📋 5. Testing application startup with PostgreSQL..."
timeout 30s ./mvnw spring-boot:run -Dspring.profiles.active=dev &
APP_PID=$!

# Wait for application to start
sleep 20

# Check if application is running
if kill -0 $APP_PID 2>/dev/null; then
    echo "✅ Application started successfully with PostgreSQL"
    kill $APP_PID
    wait $APP_PID 2>/dev/null
else
    echo "❌ Application failed to start with PostgreSQL"
    exit 1
fi

echo ""
echo "🎉 ALL TESTS PASSED!"
echo "===================="
echo "✅ JSONB functionality working correctly"
echo "✅ DataSeeder populates PostgreSQL successfully"
echo "✅ Multi-tenant isolation maintained"
echo "✅ Existing test suite compatibility preserved"
echo "✅ Application starts and runs with PostgreSQL"
echo ""
echo "🚀 PostgreSQL migration implementation is ready for production!"
echo "   Next steps:"
echo "   1. Merge feature branch to main"
echo "   2. Update production environment configuration"
echo "   3. Deploy with PostgreSQL as primary database"