package HongKongBusETA.domain.buseta

import HongKongBusETA.domain.bus.BusStop
import HongKongBusETA.domain.bus.BusStopService
import HongKongBusETA.infrastructure.datagovhk.CityBusETAFeignClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class BusETAService(val busStopService: BusStopService, val cityBusETAClient: CityBusETAFeignClient) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getETAByStopIdAndBusNumber(stopId:String, busNumber:String): CityBusETA {
        logger.info("Before feign call")
        val citybusETADataResponse = cityBusETAClient.getETA(stopId, busNumber)
        logger.info("After feign call")
        return CityBusETA(citybusETADataResponse)
    }

    fun getAllETAByStopId(stopId: Long): CityBusETA {
        val busStop: BusStop = busStopService.getBusStopByBusStopId(stopId)
        val cityBusETA: CityBusETA = CityBusETA()
        for (busNumber in busStop.busNumbers) {
            cityBusETA.addETAs(this.getETAByStopIdAndBusNumber(stopId = busStop.officialHKBusStopId, busNumber = busNumber))
        }
        return cityBusETA
    }
}