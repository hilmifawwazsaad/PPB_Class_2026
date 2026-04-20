package com.example.budgetwise.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetwise.data.dao.CategoryDao
import com.example.budgetwise.data.dao.TransactionDao
import com.example.budgetwise.data.entities.CategoryEntity
import com.example.budgetwise.data.entities.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetwise_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed kategori default saat DB pertama dibuat
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.categoryDao()?.let { dao ->
                                    if (dao.getCount() == 0) {
                                        dao.insertAll(DEFAULT_CATEGORIES)
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(name = "Gaji",          iconName = "payments",          colorHex = "#4CAF50", type = "INCOME",  isDefault = true),
            CategoryEntity(name = "Freelance",     iconName = "work",              colorHex = "#009688", type = "INCOME",  isDefault = true),
            CategoryEntity(name = "Makan & Minum", iconName = "restaurant",        colorHex = "#FF5722", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Transportasi",  iconName = "directions_car",    colorHex = "#2196F3", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Belanja",       iconName = "shopping_bag",      colorHex = "#E91E63", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Hiburan",       iconName = "movie",             colorHex = "#9C27B0", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Kesehatan",     iconName = "health_and_safety", colorHex = "#F44336", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Tagihan",       iconName = "receipt",           colorHex = "#FF9800", type = "EXPENSE", isDefault = true),
            CategoryEntity(name = "Lainnya",       iconName = "more_horiz",        colorHex = "#607D8B", type = "BOTH",    isDefault = true)
        )
    }
}