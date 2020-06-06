package HongKongBusETA.infrastructure.datapersistence.users

import org.springframework.data.repository.CrudRepository


interface UserRepository : CrudRepository<User, Long> {

    fun findByName(name: String): User

    fun existsByName(name: String): Boolean
}