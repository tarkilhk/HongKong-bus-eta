package HongKongBusETA.infrastructure.datagovhk

data class CityBusETADto(
        val `data`: List<ETAItemDto>,
//        val generated_timestamp: String,
        val type: String,
        val version: String
)