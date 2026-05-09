package com.example.marketplacesiswa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.marketplacesiswa.data.dao.ProductDao
import com.example.marketplacesiswa.data.entities.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class MarketplaceDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: MarketplaceDatabase? = null

        fun getDatabase(context: Context): MarketplaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketplaceDatabase::class.java,
                    "marketplace_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.productDao()
                                    if (dao.getCount() == 0) {
                                        dao.insertAll(DEFAULT_PRODUCTS)
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

        private val DEFAULT_PRODUCTS = listOf(
            Product(
                name = "Tas Laptop 14 Inci",
                description = "Tas pelindung laptop yang elegan dan aman dari benturan",
                price = 150000.0,
                stock = 10,
                category = "Elektronik"
            ),
            Product(
                name = "Buku Catatan Kulit",
                description = "Kertas berkualitas tinggi dengan sampul kulit sintetis",
                price = 85000.0,
                stock = 50,
                category = "Buku & Alat Tulis"
            ),
            Product(
                name = "Botol Minum Termos",
                description = "Menjaga minuman tetap dingin selama 24 jam atau panas 12 jam",
                price = 125000.0,
                stock = 30,
                category = "Rumah Tangga"
            ),
            Product(
                name = "Tas Tote Kanvas",
                description = "Tas ramah lingkungan untuk penggunaan sehari-hari",
                price = 45000.0,
                stock = 100,
                category = "Pakaian & Fashion"
            ),
            Product(
                name = "Mouse Tanpa Kabel",
                description = "Desain ergonomis dengan klik yang senyap dan responsif",
                price = 195000.0,
                stock = 25,
                category = "Elektronik"
            )
        )
    }
}
