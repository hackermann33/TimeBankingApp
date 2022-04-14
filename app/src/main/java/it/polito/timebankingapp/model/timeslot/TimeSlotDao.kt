package it.polito.timebankingapp.model.timeslot

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimeSlotDao {

    @Query("SELECT * FROM time_slots")
    fun findAll() : LiveData<List<TimeSlot>>

    @Query("SELECT count() from time_slots")
    fun count(): LiveData<Int>

    @Insert
    fun addTimeSlot(ts: TimeSlot)

    @Update
    fun updateTimeSlot(ts: TimeSlot)

    /*can't do w/ a delete, because I don't have an Item (but a String) */
    /*@Query("DELETE FROM time_slots WHERE name = :name")
    fun removeItemsWithName(name: String)
*/
    @Query("DELETE FROM time_slots")
    fun removeAll()

}