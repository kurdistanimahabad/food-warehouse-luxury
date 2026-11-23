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

    @Query("SELECT * FROM food_items ORDER BY expiryDateMillis ASC")
    fun observeAllItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY timestampMillis DESC LIMIT 100")
    fun observeRecentMovements(): Flow<List<StockMovementEntity>>

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
}
