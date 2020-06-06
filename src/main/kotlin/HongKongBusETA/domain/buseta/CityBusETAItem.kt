package HongKongBusETA.domain.buseta

import java.time.ZonedDateTime

class CityBusETAItem (
        val data_timestamp : String,
        val eta : ZonedDateTime,
        val etaSequence : Int,
        val remark : String
)

