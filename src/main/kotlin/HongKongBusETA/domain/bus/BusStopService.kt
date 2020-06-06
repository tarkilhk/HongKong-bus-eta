package HongKongBusETA.domain.bus

import HongKongBusETA.infrastructure.datapersistence.bus.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BusStopService(val busNumberRepository: BusNumberRepository, val busStopRepository: BusStopRepository) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun upsertNewBusStop(newBusStop: BusStop): Long {
        if (busStopRepository.existsByName(newBusStop.busStopName)) {
            val updatedBusStop: BusStopDao = busStopRepository.getByName(newBusStop.busStopName)
            val busNumberDaos: MutableList<BusNumberDao> = mutableListOf()
            for (busNumber: String in newBusStop.busNumbers) {
                busNumberDaos.add(busNumberRepository.getByBusNumber(busNumber))
            }
            updatedBusStop.replaceBusNumbers(busNumberDaos)
            logger.info("Before save-update new bus stop in DB")
            busStopRepository.save(updatedBusStop)
            logger.info("After save-update new bus stop in DB")
            return 666
        } else {
            logger.info("Before save new bus stop in DB")
            val busNumberDaos: MutableList<BusNumberDao> = mutableListOf()
            for (busNumber: String in newBusStop.busNumbers) {
                busNumberDaos.add(busNumberRepository.getByBusNumber(busNumber))
            }
            val insertedBusStopId: Long = busStopRepository.save(newBusStop.toDao(busNumberDaos)).busStopId
            logger.info("After save new bus stop in DB")
            return insertedBusStopId
        }
    }

//    fun getBusStopByOfficialHKBusId(stopId: String): BusStop {
//        if (busStopRepository.existsByOfficialHKBusStopId(stopId)) {
//            return BusStop(this.busStopRepository.getByofficialHKBusStopId(stopId))
//        } else {
//            return BusStop(-1, "Unknown Bus Stop", mutableListOf(), "")
//        }
//    }

    fun getBusStopByBusStopId(stopId: Long): BusStop {
        if (busStopRepository.existsById(stopId)) {
            return BusStop(this.busStopRepository.findByIdOrNull(stopId)!!)
        } else {
            return BusStop(-1, "Unknown Bus Stop", mutableListOf(), "")
        }
    }
}