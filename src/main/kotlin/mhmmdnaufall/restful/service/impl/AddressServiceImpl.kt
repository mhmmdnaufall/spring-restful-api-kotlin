package mhmmdnaufall.restful.service.impl

import mhmmdnaufall.restful.entity.Address
import mhmmdnaufall.restful.entity.User
import mhmmdnaufall.restful.model.AddressResponse
import mhmmdnaufall.restful.model.CreateAddressRequest
import mhmmdnaufall.restful.model.UpdateAddressRequest
import mhmmdnaufall.restful.repository.AddressRepository
import mhmmdnaufall.restful.repository.ContactRepository
import mhmmdnaufall.restful.service.AddressService
import mhmmdnaufall.restful.service.ValidationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class AddressServiceImpl(
        private val contactRepository: ContactRepository,
        private val addressRepository: AddressRepository,
        private val validationService: ValidationService
) : AddressService {

    @Transactional
    override fun create(user: User, request: CreateAddressRequest): AddressResponse {
        validationService.validate(request)

        val contact = contactRepository.findFirstByUserAndId(user, request.contactId!!)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, CONTACT_IS_NOT_FOUND_MESSAGE)

        val address = Address(
                id = UUID.randomUUID().toString(),
                contact = contact,
                street = request.street,
                city = request.city,
                province = request.province,
                country = request.country,
                postalCode = request.postalCode
        )

        addressRepository.save(address)

        return toAddressResponse(address)
    }

    @Transactional(readOnly = true)
    override fun get(user: User, contactId: String, addressId: String): AddressResponse {
        val contact = contactRepository.findFirstByUserAndId(user, contactId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, CONTACT_IS_NOT_FOUND_MESSAGE)

        val address = addressRepository.findFirstByContactAndId(contact, addressId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, ADDRESS_IS_NOT_FOUND_MESSAGE)

        return toAddressResponse(address)
    }

    override fun update(user: User, request: UpdateAddressRequest): AddressResponse {
        validationService.validate(request)

        val contact = contactRepository.findFirstByUserAndId(user, request.contactId!!)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, CONTACT_IS_NOT_FOUND_MESSAGE)

        val address = addressRepository.findFirstByContactAndId(contact, request.addressId!!)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, ADDRESS_IS_NOT_FOUND_MESSAGE)


        address.apply {
            street = request.street
            city = request.city
            province = request.province
            country = request.country
            postalCode = request.postalCode
        }
        addressRepository.save(address)

        return toAddressResponse(address)
    }

    @Transactional
    override fun remove(user: User, contactId: String, addressId: String) {

        val contact = contactRepository.findFirstByUserAndId(user, contactId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, CONTACT_IS_NOT_FOUND_MESSAGE)

        val address = addressRepository.findFirstByContactAndId(contact, addressId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, ADDRESS_IS_NOT_FOUND_MESSAGE)

        addressRepository.delete(address)

    }

    @Transactional(readOnly = true)
    override fun list(user: User, contactId: String): List<AddressResponse> {
        val contact = contactRepository.findFirstByUserAndId(user, contactId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, CONTACT_IS_NOT_FOUND_MESSAGE)

        val addresses: List<Address> = addressRepository.findAllByContact(contact)
        return addresses.map(::toAddressResponse)
    }

    private fun toAddressResponse(address: Address): AddressResponse {
        return AddressResponse(
                id = address.id,
                street = address.street,
                city = address.city,
                province = address.province,
                country = address.country,
                postalCode = address.postalCode
        )
    }

    companion object {
        const val CONTACT_IS_NOT_FOUND_MESSAGE = "Contact is not found"
        const val ADDRESS_IS_NOT_FOUND_MESSAGE = "Address is not found"
    }

}