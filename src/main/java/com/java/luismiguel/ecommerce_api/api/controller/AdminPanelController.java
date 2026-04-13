package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.admin.request.ChangeUserRoleRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.*;
import com.java.luismiguel.ecommerce_api.application.admin.AdminService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Panel", description = "")
public class AdminPanelController {
    private final AdminService adminService;

    public AdminPanelController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Admin Dashboard", description = "")
    @ApiResponses()
    public ResponseEntity<DashboardResponseDTO> dashboard() {
        return new ResponseEntity<>(adminService.getDashboard(), HttpStatus.OK);
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Users", description = "")
    @ApiResponses()
    public ResponseEntity<Page<GetUsersResponseDTO>> getUsers(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return new ResponseEntity<>(adminService.getAllUsers(pageable), HttpStatus.OK);
    }


    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get User by ID", description = "")
    @ApiResponses()
    public ResponseEntity<GetUserDetailsWithHistoryResponseDTO> getUserDetails(
            @PathVariable UUID userId,
            @PageableDefault Pageable pageable
    ) {
        return new ResponseEntity<>(adminService.getUserDetails(userId, pageable), HttpStatus.OK);
    }


    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change User Role by ID", description = "")
    @ApiResponses()
    public ResponseEntity<Void> changeUserRole(
            @Valid
            @PathVariable UUID userId,
            @RequestBody ChangeUserRoleRequestDTO changeUserRoleRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        adminService.changeUserRole(userId, changeUserRoleRequestDTO, user);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/users/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate User Account by ID", description = "")
    @ApiResponses()
    public ResponseEntity<Void> activeUserAccount(
            @Valid
            @PathVariable UUID userId
    ) {
        adminService.activeUserAccount(userId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/users/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable User Account by ID", description = "")
    @ApiResponses()
    public ResponseEntity<Void> disableUserAccount(
            @Valid
            @PathVariable UUID userId
    ) {
        adminService.disableUserAccount(userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/products/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Low Stock Products", description = "")
    @ApiResponses()
    public ResponseEntity<Page<GetAllLowStockProductsDTO>> lowStockProducts(
            @Valid
            @RequestParam(defaultValue = "10") Integer threshold,
            @PageableDefault Pageable pageable
    ) {
        return new ResponseEntity<>(adminService.getLowStockProducts(threshold, pageable), HttpStatus.OK);
    }


    @GetMapping("/reports/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Total Revenue", description = "")
    @ApiResponses()
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return new ResponseEntity<>(adminService.getTotalRevenuePerPeriod(startDate, endDate) , HttpStatus.OK);
    }
}
