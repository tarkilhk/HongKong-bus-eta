package HongKongBusETA.api

import HongKongBusETA.domain.buseta.BusETAService
import HongKongBusETA.domain.buseta.CityBusETA
import javassist.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

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
            @RequestParam(value="stopId") stopId: Long) : ResponseEntity<CityBusETA> {
        var response : ResponseEntity<CityBusETA>
        try {
            val cityBusEta = BusETAService.getAllETAByStopId(stopId)
            response = ResponseEntity.ok(cityBusEta)
            return response
        }
        catch(e:NotFoundException) {
            return ResponseEntity(CityBusETA(message = "" + e.message, isError = true), HttpStatus.NOT_FOUND)
        }
        catch(e:Exception) {
            return ResponseEntity(CityBusETA(message = "" + e.message, isError = true), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
