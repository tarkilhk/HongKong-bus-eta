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
            logger.info("Found busStop with name ${newBusStop.busStopName} to update")

            val updatedBusStopDao: BusStopDao = busStopRepository.getByName(newBusStop.busStopName)
            val busNumberDaos: MutableList<BusNumberDao> = mutableListOf()

            logger.info("Trying to load ${newBusStop.busNumbers.size} busNumbers from DB to replace them in this bus stop")
            for (busNumber: String in newBusStop.busNumbers) {
                // TODO : check if busNumber exists
                busNumberDaos.add(busNumberRepository.getByBusNumber(busNumber))
            }
            logger.info("Loaded ${busNumberDaos.size} bus numbers from DB")

            updatedBusStopDao.replaceBusNumbers(busNumberDaos)
            busStopRepository.save(updatedBusStopDao)
            logger.info("Successfully updated ${newBusStop.busStopName} in DB")
            return updatedBusStopDao.busStopId
        } else {
            logger.info("Creating a new bus stop with name ${newBusStop.busStopName}")
            val busNumberDaos: MutableList<BusNumberDao> = mutableListOf()
            logger.info("Trying to load ${newBusStop.busNumbers.size} busNumbers from DB to add them in this bus stop")
            for (busNumber: String in newBusStop.busNumbers) {
                // TODO : check if busNumber exists
                busNumberDaos.add(busNumberRepository.getByBusNumber(busNumber))
            }
            logger.info("Loaded ${busNumberDaos.size} bus numbers from DB")

            val insertedBusStopId: Long = busStopRepository.save(newBusStop.toDao(busNumberDaos)).busStopId
            logger.info("Successfully added ${newBusStop.busStopName} in DB")
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