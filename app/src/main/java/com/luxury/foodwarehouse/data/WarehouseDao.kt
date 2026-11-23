package com.luxury.foodwarehouse.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM food_items ORDER BY expiryDateMillis ASC")
    fun observeAllItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY timestampMillis DESC LIMIT 200")
    fun observeRecentMovements(): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE food_items SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignItemsCategory(oldCategoryId: Long, newCategoryId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<FoodItemEntity>)

    @Update
    suspend fun updateItem(item: FoodItemEntity)

    @Delete
    suspend fun deleteItem(item: FoodItemEntity)

    @Insert
    suspend fun insertMovement(movement: StockMovementEntity)

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getItemCount(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
