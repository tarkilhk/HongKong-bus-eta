package HongKongBusETA.infrastructure.datapersistence.bus

import org.springframework.data.repository.CrudRepository


interface BusStopRepository : CrudRepository<BusStopDao, Long> {
    abstract fun existsByName(name: String): Boolean

    abstract fun existsByOfficialHKBusStopId(officialHKBusStopId: String): Boolean

    fun getByName(busStopName: String): BusStopDao

    fun getByofficialHKBusStopId(officialHKBusStopId: String) : BusStopDao
}