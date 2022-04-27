package it.polito.timebankingapp.model.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun findUser() : LiveData<User> /* Not sure it'll work */

    @Insert
    fun addUser(usr: User)

    @Update(entity = User::class)
    fun updateUser(usr: User)

}