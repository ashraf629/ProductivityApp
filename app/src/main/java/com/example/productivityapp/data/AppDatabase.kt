package com.example.productivityapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimelineEntry::class], version = 1, exportSchema = false)
// - entities: List all your @Entity classes here.
// - version: Start at 1. Increment this number EACH TIME you change the database schema (e.g., add a table, add/remove/change a column).
// - exportSchema: Set to true if you want Room to export the database schema into a folder in your project.
//                 This is useful for version control and complex migrations, but false is fine for simpler projects.
public abstract class AppDatabase : RoomDatabase() {

    abstract fun timelineEntryDao(): TimelineEntryDao // Abstract method for Room to implement

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        // @Volatile annotation ensures that writes to this field are immediately visible to other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            // scope: CoroutineScope // Optional: if you need to perform operations during db creation
        ): AppDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database.
            return INSTANCE ?: synchronized(this) { // synchronized block ensures only one thread can execute this code at a time
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    AppDatabase::class.java,
                    "productivity_database" // Name of your database file
                )
                    // .addCallback(AppDatabaseCallback(scope)) // Optional: Add callback for pre-population or other setup
                    // .fallbackToDestructiveMigration() // Use WITH CAUTION: If migrations are not provided, Room will clear the database on schema version change.
                    // OK for early development, but for production, you need proper migration paths.
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

