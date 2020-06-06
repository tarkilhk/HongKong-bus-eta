package HongKongBusETA

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableFeignClients
@EnableSwagger2
@SpringBootApplication
class HongKongBusEtaApplication

fun main(args: Array<String>) {
	runApplication<HongKongBusEtaApplication>(*args)
}
