package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.admin.request.ChangeUserRoleRequestDTO;
import com.java.luismiguel.ecommerce_api.api.dto.admin.response.*;
import com.java.luismiguel.ecommerce_api.application.admin.AdminDashboardService;
import com.java.luismiguel.ecommerce_api.application.admin.AdminPanelService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Panel", description = "Administrative endpoints for user and product management, and reports")
public class AdminPanelController {
    private final AdminPanelService adminService;
    private final AdminDashboardService adminDashboardService;

    public AdminPanelController(AdminPanelService adminService, AdminDashboardService adminDashboardService) {
        this.adminService = adminService;
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Get Admin Dashboard", description = "Return aggregated metrics and KPIs for the admin dashboard.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard data returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<DashboardResponseDTO> dashboard() {
        return new ResponseEntity<>(adminDashboardService.getDashboard(), HttpStatus.OK);
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Get Users", description = "Return a paginated list of users with basic information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetUsersResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<Page<GetUsersResponseDTO>> getUsers(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return new ResponseEntity<>(adminService.getAllUsers(pageable), HttpStatus.OK);
    }


    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Get User by ID", description = "Return detailed user information including activity history and related data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetUserDetailsWithHistoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<GetUserDetailsWithHistoryResponseDTO> getUserDetails(
            @PathVariable UUID userId,
            @PageableDefault Pageable pageable
    ) {
        return new ResponseEntity<>(adminService.getUserDetails(userId, pageable), HttpStatus.OK);
    }


    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Change User Role by ID", description = "Change the role of a user (e.g., CUSTOMER <-> ADMIN). The caller must be an ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User role changed (no content)"),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid role payload"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Conflict when changing role", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
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
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Activate User Account by ID", description = "Activate a previously disabled user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User account activated (no content)"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<Void> activeUserAccount(
            @PathVariable UUID userId
    ) {
        adminService.activeUserAccount(userId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/users/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Disable User Account by ID", description = "Disable a user's account to prevent login and access.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User account disabled (no content)"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<Void> disableUserAccount(
            @PathVariable UUID userId
    ) {
        adminService.disableUserAccount(userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/products/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Get Low Stock Products", description = "Return products with stock below the provided threshold. Default threshold is 10.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of low stock products returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAllLowStockProductsDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<List<GetAllLowStockProductsDTO>> lowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold
    ) {
        return new ResponseEntity<>(adminService.getLowStockProducts(threshold), HttpStatus.OK);
    }


    @GetMapping("/reports/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "admin-endpoints")
    @Operation(summary = "Get Total Revenue", description = "Return total revenue and sales metrics for the given period.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales report returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SalesReportDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid date parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return new ResponseEntity<>(adminService.getTotalRevenuePerPeriod(startDate, endDate) , HttpStatus.OK);
    }
}
