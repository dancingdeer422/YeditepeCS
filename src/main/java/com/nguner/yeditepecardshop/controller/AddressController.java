package com.nguner.yeditepecardshop.controller;

import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.Address;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@AllArgsConstructor
public class AddressController {
    private final AddressService addressService;

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null ||
                !auth.isAuthenticated() ||
                !(auth.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Not authenticated");
        }
        return ((User) auth.getPrincipal()).getId();
    }

    /** POST   /api/user/addresses */
    @PostMapping
    public ResponseEntity<Address> create(@RequestBody Address payload) throws UserNotFoundException {
        Address created = addressService.add(currentUserId(), payload);
        return ResponseEntity.ok(created);
    }

    /** GET    /api/user/addresses */
    @GetMapping
    public ResponseEntity<List<Address>> list() throws UserNotFoundException{
        return ResponseEntity.ok(addressService.list(currentUserId()));
    }

    /** GET    /api/user/addresses/{idx} */
    @GetMapping("/{idx}")
    public ResponseEntity<Address> getOne(@PathVariable int idx) throws UserNotFoundException{
        return ResponseEntity.ok(addressService.get(currentUserId(), idx));
    }

    /** PUT    /api/user/addresses/{idx} */
    @PutMapping("/{idx}")
    public ResponseEntity<Address> update(
            @PathVariable int idx,
            @RequestBody Address payload
    ) throws UserNotFoundException {
        return ResponseEntity.ok(
                addressService.update(currentUserId(), idx, payload)
        );
    }

    /** DELETE /api/user/addresses/{idx} */
    @DeleteMapping("/{idx}")
    public ResponseEntity<Void> delete(@PathVariable int idx) throws UserNotFoundException{
        addressService.delete(currentUserId(), idx);
        return ResponseEntity.noContent().build();
    }
}

