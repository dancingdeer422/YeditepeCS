package com.nguner.yeditepecardshop.service;

import com.nguner.yeditepecardshop.exception.AddressNotFoundException;
import com.nguner.yeditepecardshop.exception.UserNotFoundException;
import com.nguner.yeditepecardshop.model.Address;
import com.nguner.yeditepecardshop.model.User;
import com.nguner.yeditepecardshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {
    private final UserRepository userRepo;

    @Autowired
    public AddressService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    private User loadUser(String userId) throws UserNotFoundException {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /** CREATE */
    public Address add(String userId, Address addr) throws UserNotFoundException {
        // Set ID if not already set
        if (addr.getId() == null || addr.getId().isEmpty()) {
            addr.setId(java.util.UUID.randomUUID().toString());
        }
        
        User u = loadUser(userId);
        u.getAddresses().add(addr);
        userRepo.save(u);
        return addr;
    }

    /** READ ALL */
    public List<Address> list(String userId) throws UserNotFoundException {
        return loadUser(userId).getAddresses();
    }

    /** READ ONE */
    public Address get(String userId, int idx) throws UserNotFoundException {
        List<Address> addrs = list(userId);
        if (idx < 0 || idx >= addrs.size()) {
            throw new AddressNotFoundException(idx);
        }
        return addrs.get(idx);
    }

    /** UPDATE */
    @Transactional
    public Address update(String userId, int idx, Address updated) throws UserNotFoundException, AddressNotFoundException {
        User u = loadUser(userId);
        List<Address> addrs = u.getAddresses();
        if (idx < 0 || idx >= addrs.size()) {
            throw new AddressNotFoundException(idx);
        }
        updated.setId(addrs.get(idx).getId()); // preserve ID
        addrs.set(idx, updated);
        u.setAddresses(addrs);
        userRepo.save(u);
        return updated;
    }

    /** DELETE */
    @Transactional
    public void delete(String userId, int idx) throws UserNotFoundException, AddressNotFoundException {
        User u = loadUser(userId);
        List<Address> addrs = u.getAddresses();
        if (idx < 0 || idx >= addrs.size()) {
            throw new AddressNotFoundException(idx);
        }
        addrs.remove(idx);
        u.setAddresses(addrs);
        userRepo.save(u);
    }
}

