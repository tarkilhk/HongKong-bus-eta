package HongKongBusETA.domain.buseta

import HongKongBusETA.domain.bus.BusStop
import HongKongBusETA.domain.bus.BusStopService
import HongKongBusETA.infrastructure.datagovhk.CityBusETAFeignClient
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
class BusETAService(val busStopService: BusStopService, val cityBusETAClient: CityBusETAFeignClient) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getETAByStopIdAndBusNumber(stopId:String, busNumber:String): CityBusETA {
        logger.info("Before feign call to datagovhk with params stopId:'$stopId' and busNumber:'$busNumber'")
        try {
            val citybusETADataResponse = cityBusETAClient.getETA(stopId, busNumber)
            logger.info("Successfully retrieved data from datagovhk")
            return CityBusETA(citybusETADataResponse, busNumber)
        }
        catch (e: FeignException) {
            val errorMessage = "FeignException while trying to retrieve data from datagovhk : [${e.status()}] : ${e.message}"
            logger.error(errorMessage)
            return CityBusETA(message = errorMessage, lastRefreshTime = ZonedDateTime.now())
        }
        catch(e: Exception) {
            logger.error("Exception while trying to retrieve data from datagovhk : ${e.message}")
            return CityBusETA(message = "Exception while trying to retrieve data from datagovhk : ${e.message}", lastRefreshTime = ZonedDateTime.now())
        }
    }

    fun getAllETAByStopId(stopId: Long): CityBusETA {
        val busStop: BusStop = busStopService.getBusStopByBusStopId(stopId)
        val cityBusETA = CityBusETA()
        for (busNumber in busStop.busNumbers) {
            cityBusETA.addETAs(this.getETAByStopIdAndBusNumber(stopId = busStop.officialHKBusStopId, busNumber = busNumber))
        }
        return cityBusETA
    }
}