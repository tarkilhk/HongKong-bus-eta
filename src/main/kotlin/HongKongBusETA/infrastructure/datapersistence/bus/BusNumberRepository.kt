package HongKongBusETA.infrastructure.datapersistence.bus

import org.springframework.data.repository.CrudRepository


interface BusNumberRepository : CrudRepository<BusNumberDao, Long> {
    fun getByBusNumber(busNumber: String): BusNumberDao

}