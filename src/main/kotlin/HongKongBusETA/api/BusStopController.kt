package HongKongBusETA.api

import HongKongBusETA.domain.bus.BusStop
import HongKongBusETA.domain.bus.BusStopService
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
                   @RequestParam(value="busNumbers") busNumbers: MutableList<String>) :String
    {
        val newBusStopId = busStopService.upsertNewBusStop(BusStop(officialHKBusStopId = busStopId, busStopName= name, busNumbers = busNumbers))
        return("Done : id $newBusStopId")
    }
}