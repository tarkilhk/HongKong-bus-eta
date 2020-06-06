package HongKongBusETA.api

import HongKongBusETA.domain.bus.BusStop
import HongKongBusETA.domain.user.UserService
import HongKongBusETA.infrastructure.datapersistence.users.User
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/users")
class UserController(val userService: UserService){

    @GetMapping("favourite-stops")
    fun getFavouriteStopsForUser(@RequestParam(value="userName") name: String):MutableList<BusStop>
    {
        return userService.getFavouriteStops(name = name)
    }

    @PostMapping("favourite-stops")
    fun setFavouriteStopsForUser(@RequestParam(value="userName") name: String,
                                 @RequestParam(value="busStopIds") busStopIds: MutableList<Long>):String
    {
        userService.setFavouriteStopsFor(name, busStopIds)
        return("Done")
    }
}