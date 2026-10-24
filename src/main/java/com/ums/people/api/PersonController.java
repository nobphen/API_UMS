package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.AddressRequest;
import com.ums.people.api.dto.AddressResponse;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.PersonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final PersonAddressService personAddressService;

    /// ADDRESSES: birth place, permanent and current ----------------

    @GetMapping("/{personId}/addresses")
    @RequirePermission({PeoplePermissions.STUDENT_VIEW, PeoplePermissions.STAFF_VIEW})
    public ApiResponse<List<AddressResponse>> addresses(@PathVariable Long personId) {
        return ApiResponse.ok(personAddressService.getByPerson(personId));
    }

    /** Replaces all the person's addresses with this list. */
    @PutMapping("/{personId}/addresses")
    @RequirePermission({PeoplePermissions.STUDENT_UPDATE, PeoplePermissions.STAFF_MANAGE, PeoplePermissions.GUARDIAN_MANAGE})
    public ApiResponse<List<AddressResponse>> replaceAddresses(@PathVariable Long personId,
                                                               @Valid @RequestBody List<@Valid AddressRequest> addresses) {
        return ApiResponse.ok(personAddressService.replaceAll(personId, addresses));
    }
}
