package HongKongBusETA.api

import HongKongBusETA.domain.bus.BusStop
import HongKongBusETA.domain.bus.BusStopService
import javassist.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bus-stop")
class BusStopController(val busStopService: BusStopService) {

    @PostMapping("")
    fun newBusStop(@RequestParam(value="officialHKBusStopId") busStopId : String,
                   @RequestParam(value="name") name: String,
                   @RequestParam(value="busNumbers") busNumbers: MutableList<String>) : ResponseEntity<String>
    {
        var response:ResponseEntity<String>
        try {
            val newBusStopId = busStopService.upsertNewBusStop(BusStop(officialHKBusStopId = busStopId, busStopName = name, busNumbers = busNumbers))
            response = ResponseEntity("Done : id $newBusStopId", HttpStatus.OK)
        }
        catch(e: NotFoundException) {
            response = ResponseEntity(e.message, HttpStatus.NOT_FOUND)
        }
        catch (e: Exception) {
            response = ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return(response)
    }
}