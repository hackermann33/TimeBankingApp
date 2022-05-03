package it.polito.timebankingapp.model
import android.content.Context
import androidx.room.*
import it.polito.timebankingapp.model.user.Converters
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.model.user.UserDao


@Database(entities = [ User::class], version = 4)
@TypeConverters(Converters::class)
abstract class TimeBankingDB : RoomDatabase() {

    //abstract fun timeSlotDao(): TimeSlotDao
    abstract fun usersDao(): UserDao

    companion object {
        @Volatile //JAVA Compiler will guarantee that after write
        // this variable cpu-cache will be flushed!
        private var INSTANCE: TimeBankingDB? = null

        fun getDatabase(context: Context): TimeBankingDB =
            (
                    INSTANCE ?: synchronized(this) {
                        val i = INSTANCE ?: Room.databaseBuilder(
                            context.applicationContext,
                            TimeBankingDB::class.java,
                            "time_slots"
                        ).fallbackToDestructiveMigration()
                            .build()
                        INSTANCE = i
                        INSTANCE
                    }
                    )!!
    }

}
