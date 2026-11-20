package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.security.CurrentUser;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.AddressRequest;
import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.MyProfileUpdateRequest;
import com.ums.people.api.dto.PersonResponse;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.PersonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data shared by every kind of person (student, staff, guardian): the addresses, and "my profile" (/me).
 * personId is the id in persons, not the student or staff id (StudentResponse has personId).
 */
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final PersonAddressService personAddressService;

    /// ADDRESSES: birth place, permanent and current ----------------

    /**
     * The person's addresses, with the full text in Khmer and English.
     * <pre>GET /api/v1/persons/7/addresses</pre>
     */
    @GetMapping("/{personId}/addresses")
    @RequirePermission({PeoplePermissions.STUDENT_VIEW, PeoplePermissions.STAFF_VIEW})
    public ApiResponse<List<AddressResponse>> addresses(@PathVariable Long personId) {
        return ApiResponse.ok(personAddressService.getByPerson(personId));
    }

    /**
     * Replaces all the person's addresses with this list: one left out is deleted, an empty list deletes all.
     * <pre>PUT /api/v1/persons/7/addresses  [{"type": "CURRENT", "areaId": 12010101, "houseNo": "12A"},
     *                                      {"type": "PERMANENT", "countryCode": "TH", "addressLine": "Bangkok"}]</pre>
     */
    @PutMapping("/{personId}/addresses")
    @RequirePermission({PeoplePermissions.STUDENT_UPDATE, PeoplePermissions.STAFF_MANAGE, PeoplePermissions.GUARDIAN_MANAGE})
    public ApiResponse<List<AddressResponse>> replaceAddresses(@PathVariable Long personId,
                                                               @Valid @RequestBody List<@Valid AddressRequest> addresses) {
        return ApiResponse.ok(personAddressService.replaceAll(personId, addresses));
    }

    /// MY PROFILE: the logged-in person (student, staff or guardian) ----------------

    /**
     * My own profile: names, contact, addresses. No id in the URL: the user id comes from the token
     * (jwt.getSubject()), so nobody can read someone else's profile. No permission: every logged-in person may.
     * <pre>GET /api/v1/persons/me</pre>
     */
    @GetMapping("/me")
    public ApiResponse<PersonResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(personService.getMyProfile(CurrentUser.userId(jwt)));
    }

    /**
     * I change my own phone, email and current address (names, national ID ... only an admin changes).
     * <pre>PUT /api/v1/persons/me  {"phone": "096 953 0336", "email": "dara@gmail.com",
     *                              "currentAddress": {"type": "CURRENT", "areaId": 1890, "houseNo": "12A"}}</pre>
     */
    @PutMapping("/me")
    public ApiResponse<PersonResponse> updateMyProfile(@Valid @RequestBody MyProfileUpdateRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(personService.updateMyProfile(CurrentUser.userId(jwt), request));
    }

    /// PHOTO: form-data with one field "file" (JPG or PNG, max 2 MB) ----------------

    /**
     * I change my own photo.
     * <pre>PUT /api/v1/persons/me/photo   form-data: file = dara.jpg</pre>
     */
    @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PersonResponse> updateMyPhoto(@RequestPart("file") MultipartFile file,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(personService.updateMyPhoto(CurrentUser.userId(jwt), file));
    }

    /**
     * I remove my own photo.
     * <pre>DELETE /api/v1/persons/me/photo</pre>
     */
    @DeleteMapping("/me/photo")
    public ApiResponse<PersonResponse> deleteMyPhoto(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(personService.deleteMyPhoto(CurrentUser.userId(jwt)));
    }

    /**
     * An admin changes the photo of any person (personId from StudentResponse / StaffResponse).
     * <pre>PUT /api/v1/persons/7/photo   form-data: file = dara.jpg</pre>
     */
    @PutMapping(value = "/{personId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission({PeoplePermissions.STUDENT_UPDATE, PeoplePermissions.STAFF_MANAGE, PeoplePermissions.GUARDIAN_MANAGE})
    public ApiResponse<PersonResponse> updatePhoto(@PathVariable Long personId, @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(personService.updatePhoto(personId, file));
    }

    /**
     * An admin removes the photo of a person.
     * <pre>DELETE /api/v1/persons/7/photo</pre>
     */
    @DeleteMapping("/{personId}/photo")
    @RequirePermission({PeoplePermissions.STUDENT_UPDATE, PeoplePermissions.STAFF_MANAGE, PeoplePermissions.GUARDIAN_MANAGE})
    public ApiResponse<PersonResponse> deletePhoto(@PathVariable Long personId) {
        return ApiResponse.ok(personService.deletePhoto(personId));
    }
}
