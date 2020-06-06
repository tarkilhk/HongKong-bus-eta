package HongKongBusETA.api

import HongKongBusETA.domain.buseta.BusETAService
import HongKongBusETA.domain.buseta.CityBusETA
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController()
@RequestMapping("/bus-eta")
class BusETAController(val BusETAService: BusETAService) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("")
    fun getBusETAByStopIdAndBusNumber(
            @RequestParam(value="stopId") stopId: String,
            @RequestParam(value="busNumber") busNumber:String) : CityBusETA {

        val response = BusETAService.getETAByStopIdAndBusNumber(stopId, busNumber)
        return response
    }

    @GetMapping("bus-stop")
    fun getBusETAByStopId(
            @RequestParam(value="stopId") stopId: Long) : CityBusETA {

        val response = BusETAService.getAllETAByStopId(stopId)
        return response
    }
}
