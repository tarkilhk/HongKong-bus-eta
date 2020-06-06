package HongKongBusETA.infrastructure.datagovhk

import HongKongBusETA.infrastructure.datagovhk.CityBusETADto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(value = "datagovhk", url = "https://rt.data.gov.hk/v1/transport/citybus-nwfb/")
interface CityBusETAFeignClient {
    @GetMapping("/eta/ctb/{stopId}/{busNumber}")
    fun getETA(@PathVariable("stopId") stopId:String, @PathVariable("busNumber") busNumber:String) : CityBusETADto;
}