package HongKongBusETA.infrastructure.datapersistence.bus

import javax.persistence.*

@Entity
@Table(name = "BUS_NUMBERS")
data class BusNumberDao (
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        val busNumberId: Long = -1,

        @Column(unique = true)
        val busNumber: String,

        @ManyToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "busNumbers")
        val busStopDaos: MutableList<BusStopDao> = mutableListOf<BusStopDao>()
)
{
        constructor() : this(-1, "")

        override fun toString(): String {
                return String.format(
                        "Bus number id=$busNumberId : bus # $busNumber")
        }
}