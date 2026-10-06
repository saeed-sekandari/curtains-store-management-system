package com.royalcurtains.storemanagement.security;

import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class StoreAccessService {

    private final UserRepository userRepository;

    public StoreAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Makes sure the signed-in user can access the requested store.
    public void checkStoreAccess(
            Principal principal,
            String requestedStoreCode) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("User account not found"));

        // Managers are allowed to view both stores.
        if (user.getRole() == Role.MANAGER) {
            return;
        }

        // Employees must have an assigned store.
        if (user.getAssignedStore() == null) {
            throw new AccessDeniedException("No store is assigned to this account");
        }

        boolean sameStore = user.getAssignedStore()
                .getCode()
                .equalsIgnoreCase(requestedStoreCode);

        if (!sameStore) {
            throw new AccessDeniedException(
                    "You cannot access this store");
        }
    }
}