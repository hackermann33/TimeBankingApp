package it.polito.timebankingapp.model.timeslot
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [TimeSlot::class], version = 1)
abstract class TimeSlotDB : RoomDatabase() {
    /**
     * In order to provide the DAO
     *
     **/
    abstract fun timeSlotDao(): TimeSlotDao

    companion object {
        @Volatile //JAVA Compiler will guarantee that after write
        // this variable cpu-cache will be flushed!
        private var INSTANCE: TimeSlotDB? = null

        fun getDatabase(context: Context): TimeSlotDB =
            (
                    INSTANCE ?: synchronized(this) {
                        val i = INSTANCE ?: Room.databaseBuilder(
                            context.applicationContext,
                            TimeSlotDB::class.java,
                            "time_slots"
                        ).build()
                        INSTANCE = i
                        INSTANCE
                    }
                    )!!
    }
}
