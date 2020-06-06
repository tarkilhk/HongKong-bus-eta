package HongKongBusETA.domain.buseta

import HongKongBusETA.infrastructure.datagovhk.CityBusETADto
import java.time.ZonedDateTime


class CityBusETA() {
    var lastRefreshTime : ZonedDateTime
    var message : String
    val ETAs : MutableList<CityBusETAItem>

    init {
        this.lastRefreshTime = ZonedDateTime.now()
        message = ""
        this.ETAs = mutableListOf()
    }

    constructor(cityBusETADto: CityBusETADto) : this() {
        if (cityBusETADto.data.size == 0) {
            this.lastRefreshTime = ZonedDateTime.now()
        }
        else {
            lastRefreshTime = ZonedDateTime.parse(cityBusETADto.data.first().data_timestamp)
            for(etaDataResponse in cityBusETADto.data) {
                this.ETAs.add(CityBusETAItem(data_timestamp = etaDataResponse.data_timestamp, eta = ZonedDateTime.parse(etaDataResponse.eta), etaSequence = etaDataResponse.eta_seq, remark = etaDataResponse.rmk_en))
            }
        }
        if (ETAs.size == 0) {
            message = "No bus"
        }
        else {
            message = "OK"
        }
    }

    fun addETAs(cityBusETA: CityBusETA) {
        lastRefreshTime = cityBusETA.lastRefreshTime
        for(etaDataItem in cityBusETA.ETAs) {
            this.ETAs.add(etaDataItem)
        }
        if (ETAs.size == 0) {
            message = "No bus"
        }
        else {
            message = "OK"
        }
    }

}