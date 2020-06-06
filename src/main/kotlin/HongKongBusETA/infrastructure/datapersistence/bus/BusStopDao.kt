package HongKongBusETA.infrastructure.datapersistence.bus

import HongKongBusETA.infrastructure.datapersistence.users.User
import javax.persistence.*

@Entity
@Table(name = "BUS_STOPS")
data class BusStopDao (
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        val busStopId: Long = -1,

        @Column(unique = true)
        val name: String,

        @Column
        val officialHKBusStopId: String,

        @ManyToMany(cascade = arrayOf(CascadeType.ALL))
        @JoinTable(
                name = "BUS_STOP_NUMBERS",
                joinColumns = arrayOf(JoinColumn(name = "bus_stop_id")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "bus_number_id"))
        )
        val busNumbers : MutableList<BusNumberDao> = mutableListOf<BusNumberDao>(),
//
//        @ManyToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "busStopDaos")
//        val busStopGroupDao: MutableList<BusStopGroupDao> = mutableListOf<BusStopGroupDao>()

        @ManyToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "favouriteBusStopDaos")
        val users: MutableList<User> = mutableListOf()
)
{
        constructor(busNumber: String,busName: String, officialHKBusStopId: String) : this(-1, busName, officialHKBusStopId)
        constructor() : this(-1, "", "0")

        override fun toString(): String {
                return String.format(
                        "Bus stop id=$busStopId : name $name - stop # $officialHKBusStopId")
        }

        fun replaceBusNumbers(busNumbersDaos: MutableList<BusNumberDao>) {
                this.busNumbers.clear()
                for(busNumerDao:BusNumberDao in busNumbersDaos) {
                        this.busNumbers.add(busNumerDao)
                }
        }
}