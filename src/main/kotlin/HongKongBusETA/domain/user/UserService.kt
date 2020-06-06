package HongKongBusETA.domain.user

import HongKongBusETA.domain.bus.BusStop
import HongKongBusETA.infrastructure.datapersistence.bus.BusNumberDao
import HongKongBusETA.infrastructure.datapersistence.bus.BusStopRepository
import HongKongBusETA.infrastructure.datapersistence.users.User
import HongKongBusETA.infrastructure.datapersistence.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: UserRepository, val busStopRepository: BusStopRepository) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun setFavouriteStopsFor(name: String, busStopIds: MutableList<Long>) {
        if (userRepository.existsByName(name)) {
            val user: User = this.getUser(name)!!

            user.favouriteBusStopDaos.clear()

            for(busStopId:Long in busStopIds) {
                if (busStopRepository.existsById(busStopId)) {
                    user.favouriteBusStopDaos.add(busStopRepository.findByIdOrNull(busStopId)!!)
                }
                else {
                    // invalid busStopId
                }
            }
            userRepository.save(user)
        } else {
            // invalid user name
        }
    }

    fun getUser(name: String):User? {
        if (userRepository.existsByName(name)) {
            return(userRepository.findByName(name))
        }
        return null
    }

    fun getFavouriteStops(name: String): MutableList<BusStop> {
        val myUser: User? = this.getUser(name)
        if (myUser == null) {
            return mutableListOf(BusStop(-1, "Unknown user", mutableListOf(),""))
        }
        else {
            val busStopList : MutableList<BusStop> = mutableListOf()
            for (busStop in myUser.favouriteBusStopDaos) {
                busStopList.add(BusStop(busStop))
            }
            return(busStopList)
        }
    }
}