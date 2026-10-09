package com.royalcurtains.storemanagement.web;

import com.royalcurtains.storemanagement.model.InventoryProduct;
import com.royalcurtains.storemanagement.model.Role;
import com.royalcurtains.storemanagement.model.Store;
import com.royalcurtains.storemanagement.model.User;
import com.royalcurtains.storemanagement.repository.InventoryProductRepository;
import com.royalcurtains.storemanagement.repository.StoreRepository;
import com.royalcurtains.storemanagement.repository.UserRepository;
import com.royalcurtains.storemanagement.security.StoreAccessService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class InventoryProductController {

    private final InventoryProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreAccessService storeAccessService;

    public InventoryProductController(
            InventoryProductRepository productRepository,
            StoreRepository storeRepository,
            UserRepository userRepository,
            StoreAccessService storeAccessService) {

        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeAccessService = storeAccessService;
    }

    // Shows active inventory products for the selected store.
    @GetMapping("/inventory")
    public String inventory(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);
        User currentUser = getCurrentUser(principal);

        checkEmployeeOrManager(currentUser);
        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        model.addAttribute("store", selectedStore);
        model.addAttribute(
                "products",
                productRepository.findByStoreIdAndActiveTrueOrderByProductNameAsc(
                        selectedStore.getId()
                )
        );
        model.addAttribute("currentRole", currentUser.getRole().name());

        return "inventory";
    }

    // Opens the form for adding a product.
    @GetMapping("/inventory/new")
    public String newProduct(
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);
        User currentUser = getCurrentUser(principal);

        checkEmployeeOrManager(currentUser);
        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        model.addAttribute("store", selectedStore);
        model.addAttribute("product", new InventoryProduct());

        return "inventory-form";
    }

    // Saves a new inventory product.
    @Transactional
    @PostMapping("/inventory")
    public String saveProduct(
            @RequestParam String storeCode,
            @RequestParam String productName,
            @RequestParam(required = false) String color,
            @RequestParam BigDecimal meterage,
            @RequestParam(required = false) String location,
            @RequestParam String productCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Store selectedStore = findStore(storeCode);
        User currentUser = getCurrentUser(principal);

        checkEmployeeOrManager(currentUser);
        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (meterage == null
                || meterage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Meterage cannot be negative"
            );
        }

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Product code is required"
            );
        }

        String cleanedCode = productCode.trim();

        if (productRepository.existsByProductCodeIgnoreCase(cleanedCode)) {
            throw new IllegalArgumentException(
                    "That product code is already being used"
            );
        }

        InventoryProduct product = new InventoryProduct();
        product.setProductName(productName.trim());
        product.setColor(cleanValue(color));
        product.setMeterage(meterage);
        product.setLocation(cleanValue(location));
        product.setProductCode(cleanedCode);
        product.setStore(selectedStore);
        product.setAddedBy(currentUser);

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "success",
                "Inventory product was added successfully."
        );

        return "redirect:/inventory?store="
                + selectedStore.getCode();
    }

    // Opens the edit form for an existing product.
    @GetMapping("/inventory/{productId}/edit")
    public String editProduct(
            @PathVariable Long productId,
            @RequestParam String store,
            Model model,
            Principal principal) {

        Store selectedStore = findStore(store);
        User currentUser = getCurrentUser(principal);

        checkEmployeeOrManager(currentUser);
        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        InventoryProduct product = productRepository
                .findByIdAndStoreId(productId, selectedStore.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory product not found"
                        ));

        if (!product.isActive()) {
            throw new IllegalStateException(
                    "This inventory product is no longer active"
            );
        }

        model.addAttribute("store", selectedStore);
        model.addAttribute("product", product);

        return "inventory-edit-form";
    }

    // Saves corrections made by an employee or manager.
    @Transactional
    @PostMapping("/inventory/{productId}/edit")
    public String updateProduct(
            @PathVariable Long productId,
            @RequestParam String storeCode,
            @RequestParam String productName,
            @RequestParam(required = false) String color,
            @RequestParam BigDecimal meterage,
            @RequestParam(required = false) String location,
            @RequestParam String productCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Store selectedStore = findStore(storeCode);
        User currentUser = getCurrentUser(principal);

        checkEmployeeOrManager(currentUser);
        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        InventoryProduct product = productRepository
                .findByIdAndStoreId(productId, selectedStore.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory product not found"
                        ));

        if (!product.isActive()) {
            throw new IllegalStateException(
                    "This inventory product is no longer active"
            );
        }

        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (meterage == null
                || meterage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Meterage cannot be negative"
            );
        }

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Product code is required"
            );
        }

        product.setProductName(productName.trim());
        product.setColor(cleanValue(color));
        product.setMeterage(meterage);
        product.setLocation(cleanValue(location));
        product.setProductCode(productCode.trim());
        product.setLastEditedBy(currentUser);
        product.setLastEditedAt(LocalDateTime.now());

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "success",
                "Inventory product was updated successfully."
        );

        return "redirect:/inventory?store="
                + selectedStore.getCode();
    }

    // Only the manager can deactivate an inventory product.
    @Transactional
    @PostMapping("/inventory/{productId}/deactivate")
    public String deactivateProduct(
            @PathVariable Long productId,
            @RequestParam String storeCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Store selectedStore = findStore(storeCode);
        User currentUser = getCurrentUser(principal);

        if (currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException(
                    "Only the manager can deactivate inventory products"
            );
        }

        storeAccessService.checkStoreAccess(
                principal,
                selectedStore.getCode()
        );

        InventoryProduct product = productRepository
                .findByIdAndStoreId(productId, selectedStore.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventory product not found"
                        ));

        product.setActive(false);
        product.setLastEditedBy(currentUser);
        product.setLastEditedAt(LocalDateTime.now());

        productRepository.save(product);

        redirectAttributes.addFlashAttribute(
                "success",
                "Inventory product was deactivated."
        );

        return "redirect:/inventory?store="
                + selectedStore.getCode();
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private void checkEmployeeOrManager(User user) {
        if (user.getRole() != Role.EMPLOYEE
                && user.getRole() != Role.MANAGER) {

            throw new AccessDeniedException(
                    "Only employees and managers can manage inventory"
            );
        }
    }

    private Store findStore(String storeCode) {
        return storeRepository.findByCode(storeCode.toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("Store not found"));
    }

    private String cleanValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}