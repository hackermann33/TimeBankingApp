package it.polito.timebankingapp.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.model.user.UsersRepository
import kotlin.concurrent.thread

class ProfileViewModel (application: Application): AndroidViewModel(application) {

    val repo = UsersRepository(application)

    val usr: LiveData<User> = repo.findUser()

    fun addUser(usr: User) {
        thread {
            repo.addUser(usr)
        }
    }

    fun editUser(usr: User){
        thread {
            repo.editUser(usr)
        }
    }

}