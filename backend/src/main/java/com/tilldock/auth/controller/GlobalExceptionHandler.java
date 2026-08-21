package com.tilldock.auth.controller;

import com.tilldock.auth.dto.ApiError;
import com.tilldock.auth.security.JwtService;
import com.tilldock.auth.service.AuthExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("validation_failed", "Please complete all required fields correctly.", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformed(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("malformed_request", "The request body is invalid.", null));
    }

    @ExceptionHandler(AuthExceptions.EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleDuplicate(AuthExceptions.EmailAlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("email_already_registered", "This email is already registered.", null));
    }

    @ExceptionHandler(AuthExceptions.InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(AuthExceptions.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("invalid_credentials", "Invalid email or password.", null));
    }

    @ExceptionHandler(AuthExceptions.AccountNotActiveException.class)
    public ResponseEntity<ApiError> handleAccount(AuthExceptions.AccountNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("account_inactive", "Your account is not active.", null));
    }

    @ExceptionHandler(AuthExceptions.MerchantNotFoundException.class)
    public ResponseEntity<ApiError> handleMissing(AuthExceptions.MerchantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("not_found", "Requested resource was not found.", null));
    }

    @ExceptionHandler(AuthExceptions.BusinessNotFoundException.class)
    public ResponseEntity<ApiError> handleBusinessMissing(AuthExceptions.BusinessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("business_not_found", "Please set up your business profile first.", null));
    }

    @ExceptionHandler(AuthExceptions.BusinessSetupRequiredException.class)
    public ResponseEntity<ApiError> handleBusinessSetup(AuthExceptions.BusinessSetupRequiredException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(new ApiError("business_setup_required", "Please set up your business profile first.", null));
    }

    @ExceptionHandler(AuthExceptions.CategoryNotFoundException.class)
    public ResponseEntity<ApiError> handleCategoryMissing(AuthExceptions.CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("category_not_found", "Category not found.", null));
    }

    @ExceptionHandler(AuthExceptions.ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductMissing(AuthExceptions.ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("product_not_found", "Product not found.", null));
    }

    @ExceptionHandler(AuthExceptions.DuplicateCategoryNameException.class)
    public ResponseEntity<ApiError> handleDuplicateCategory(AuthExceptions.DuplicateCategoryNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("duplicate_category", "A category with this name already exists.", null));
    }

    @ExceptionHandler(AuthExceptions.DuplicateProductSkuException.class)
    public ResponseEntity<ApiError> handleDuplicateProduct(AuthExceptions.DuplicateProductSkuException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("duplicate_product_sku", "A product with this SKU already exists.", null));
    }

@ExceptionHandler(AuthExceptions.CategoryHasProductsException.class)
    public ResponseEntity<ApiError> handleCategoryHasProducts(AuthExceptions.CategoryHasProductsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("category_has_products", "Cannot delete a category that has products. Remove or reassign the products first.", null));
    }

    @ExceptionHandler(AuthExceptions.InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(AuthExceptions.InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("insufficient_stock", "Insufficient stock to complete the operation.", null));
    }

    @ExceptionHandler(AuthExceptions.InvalidQuantityException.class)
    public ResponseEntity<ApiError> handleInvalidQuantity(AuthExceptions.InvalidQuantityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("invalid_quantity", ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("invalid_request", ex.getMessage(), null));
    }

    @ExceptionHandler(JwtService.InvalidTokenException.class)
    public ResponseEntity<ApiError> handleBadToken(JwtService.InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("invalid_token", "Your session is invalid. Please sign in again.", null));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiError("method_not_allowed", "HTTP method is not supported for this endpoint.", null));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("not_found", "Requested endpoint was not found.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("server_error", "Something went wrong. Please try again.", null));
    }
}