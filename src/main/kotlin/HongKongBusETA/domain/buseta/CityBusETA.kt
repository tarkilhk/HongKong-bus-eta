package HongKongBusETA.domain.buseta

import HongKongBusETA.infrastructure.datagovhk.CityBusETADto
import java.time.ZonedDateTime


class CityBusETA() {
    var lastRefreshTime: ZonedDateTime = ZonedDateTime.now()
    var message : String  = ""
    val ETAs : MutableList<CityBusETAItem> = mutableListOf()

    constructor(message:String, lastRefreshTime: ZonedDateTime) : this() {
        this.message = message
        this.lastRefreshTime = lastRefreshTime
    }

    constructor(cityBusETADto: CityBusETADto, busNumber : String) : this() {
        if (cityBusETADto.data.size == 0) {
            this.lastRefreshTime = ZonedDateTime.now()
            this.message = "No bus"
        }
        else {
            lastRefreshTime = ZonedDateTime.parse(cityBusETADto.data.first().data_timestamp)
            for(etaDataResponse in cityBusETADto.data) {
                this.ETAs.add(CityBusETAItem(busNumber = busNumber, data_timestamp = etaDataResponse.data_timestamp, eta = ZonedDateTime.parse(etaDataResponse.eta), etaSequence = etaDataResponse.eta_seq, remark = etaDataResponse.rmk_en))
            }
            this.message = "OK"
        }
    }

    fun addETAs(cityBusETA: CityBusETA) {
        lastRefreshTime = cityBusETA.lastRefreshTime
        for(etaDataItem in cityBusETA.ETAs) {
            this.ETAs.add(etaDataItem)
        }
        setRelevantValueToMessage(cityBusETA.message)
    }

    private fun setRelevantValueToMessage(message: String) {
        if (message.equals("")) {
            if (ETAs.size == 0) {
                this.message = "No bus"
            } else {
                this.message = "OK"
            }
        }
        else {
            this.message = message
        }
    }

}