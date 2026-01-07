# SQLDelight Database Migrations

This directory contains database migration files for the Payroll Desktop application.

## Migration Files

- **1.sqm**: Initial database schema (Version 1)
  - Employee table
  - Client table (without UNIQUE constraint)
  - Match Confirmation table
  - Indexes for performance

- **2.sqm**: Add UNIQUE constraint to ClientEntity (Version 2)
  - Adds `UNIQUE(employee_id, name)` constraint to prevent duplicate client names per employee
  - Uses SQLite table recreation pattern (create temp table, copy data, drop old, rename)
  - Handles existing duplicates gracefully with `INSERT OR IGNORE`

## How Migrations Work

SQLDelight automatically:
1. Detects migration files numbered sequentially (1.sqm, 2.sqm, etc.)
2. Verifies migrations match the current schema
3. Applies migrations in order when database is opened

## Adding New Migrations

When you need to modify the database schema:

1. **Update the main schema** (`PayrollDatabase.sq`) with your changes
2. **Create a new migration file**: `migrations/N.sqm` (where N is the next number)
3. **Write SQL statements** to migrate from version N-1 to N
4. **Run build** to verify: `./gradlew compileKotlinJvm`

### Example: Adding a New Column

```sql
-- 3.sqm: Add 'phone' column to EmployeeEntity
ALTER TABLE EmployeeEntity ADD COLUMN phone TEXT NOT NULL DEFAULT '';
```

### Important Notes for SQLite

- **Cannot add constraints** to existing tables (use table recreation pattern)
- **Cannot modify columns** (use table recreation pattern)
- **Always backup data** before applying migrations
- **Test migrations** with sample data before deploying

## Schema Verification

The build process verifies:
- Migration files match the current schema
- Migrations can be applied in sequence
- No duplicate definitions or conflicts

Verification enabled in `build.gradle.kts`:
```kotlin
sqldelight {
    databases {
        create("PayrollDatabase") {
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}
```

## Current Schema Version

**Version 2** - Latest
- All tables with proper constraints
- UNIQUE constraint on (employee_id, name) in ClientEntity
- Prevents duplicate client names per employee

## Migration History

| Version | Date | Description |
|---------|------|-------------|
| 1 | Initial | Base schema with Employee, Client, and MatchConfirmation tables |
| 2 | 2026-01 | Added UNIQUE constraint to ClientEntity (employee_id, name) |

## Rollback Strategy

SQLDelight doesn't support automatic rollbacks. To rollback:
1. Restore database from backup
2. Remove migration files for versions > target version
3. Update schema to match target version
4. Rebuild application

**Best Practice**: Always backup database before updating the app!