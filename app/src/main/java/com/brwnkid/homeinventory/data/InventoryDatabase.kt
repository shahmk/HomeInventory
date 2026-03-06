package com.brwnkid.homeinventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Item::class, Location::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var Instance: InventoryDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val time = System.currentTimeMillis()
                // Migrate Locations
                db.execSQL("CREATE TABLE IF NOT EXISTS `locations_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `lastModified` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO locations_new (id, name, lastModified, isDeleted) SELECT CAST(id AS TEXT), name, $time, 0 FROM locations")
                db.execSQL("DROP TABLE locations")
                db.execSQL("ALTER TABLE locations_new RENAME TO locations")

                // Migrate Items
                db.execSQL("CREATE TABLE IF NOT EXISTS `items_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `quantity` INTEGER NOT NULL, `locationId` TEXT NOT NULL, `imageUris` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("INSERT INTO items_new (id, name, description, quantity, locationId, imageUris, sortOrder, lastModified, isDeleted) SELECT CAST(id AS TEXT), name, description, quantity, CAST(locationId AS TEXT), imageUris, sortOrder, $time, 0 FROM items")
                db.execSQL("DROP TABLE items")
                db.execSQL("ALTER TABLE items_new RENAME TO items")
                
                // Keep the items_locationId index if room usually creates it. Room will create it anyway if it is in the schema.
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_locationId` ON `items` (`locationId`)")
            }
        }

        fun getDatabase(context: Context): InventoryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
