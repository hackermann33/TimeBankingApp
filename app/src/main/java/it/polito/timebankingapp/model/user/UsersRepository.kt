package it.polito.timebankingapp.model.user

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.polito.timebankingapp.model.TimeBankingDB

class UsersRepository (application: Application){
    private val usersDao = TimeBankingDB.getDatabase(application).usersDao()

    fun findUser() : LiveData<User> = usersDao.findUser()

    fun addUser(usr: User){
        usersDao.addUser(usr)
    }

    fun editUser(usr: User) {
        usersDao.updateUser(usr)
    }

}