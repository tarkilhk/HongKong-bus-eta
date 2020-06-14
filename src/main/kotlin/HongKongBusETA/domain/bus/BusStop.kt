package HongKongBusETA.domain.bus

import HongKongBusETA.infrastructure.datapersistence.bus.BusNumberDao
import HongKongBusETA.infrastructure.datapersistence.bus.BusStopDao
import java.util.*

data class BusStop(
        var busStopId : Long = -1,
        var busStopName: String = "",
        var busNumbers: MutableList<String> = mutableListOf(),
        var officialHKBusStopId : String = "") {

    constructor(busStopDao: BusStopDao) : this(busStopId = busStopDao.busStopId,busStopName = busStopDao.name, officialHKBusStopId = busStopDao.officialHKBusStopId, busNumbers = mutableListOf<String>()) {
        for (busNumberDao in busStopDao.busNumbers) {
            this.busNumbers.add(busNumberDao.busNumber)
        }
    }

    constructor(officialHKBusStopId: String, busStopName: String, busNumbers: MutableList<String>) : this() {
        this.busStopId = -1
        this.busStopName = busStopName
        this.busNumbers = busNumbers
        this.officialHKBusStopId = officialHKBusStopId
    }

    override fun toString(): String = "Bus Stop Name $busStopName - HK official stop Id $officialHKBusStopId"

    override fun hashCode(): Int {
        return (Objects.hash(busStopName, officialHKBusStopId))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        val that = other as BusStop
        return this.busStopName == that.busStopName &&
                this.officialHKBusStopId == that.officialHKBusStopId
    }

    fun toDao(busNumberDaos: MutableList<BusNumberDao>): BusStopDao {
        return BusStopDao(name = this.busStopName, officialHKBusStopId = this.officialHKBusStopId, busNumbers = busNumberDaos)
    }
}