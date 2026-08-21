package com.tilldock.auth.service;

public final class AuthExceptions {

    private AuthExceptions() { }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException() {
            super("Email already registered");
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password");
        }
    }

    public static class AccountNotActiveException extends RuntimeException {
        public AccountNotActiveException() {
            super("Account is not active");
        }
    }

    public static class MerchantNotFoundException extends RuntimeException {
        public MerchantNotFoundException() {
            super("Merchant not found");
        }
    }

    public static class BusinessNotFoundException extends RuntimeException {
        public BusinessNotFoundException() {
            super("Business not found");
        }
    }

    public static class CategoryNotFoundException extends RuntimeException {
        public CategoryNotFoundException() {
            super("Category not found");
        }
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException() {
            super("Product not found");
        }
    }

    public static class DuplicateCategoryNameException extends RuntimeException {
        public DuplicateCategoryNameException() {
            super("A category with this name already exists");
        }
    }

    public static class DuplicateProductSkuException extends RuntimeException {
        public DuplicateProductSkuException() {
            super("A product with this SKU already exists");
        }
    }

    public static class BusinessSetupRequiredException extends RuntimeException {
        public BusinessSetupRequiredException() {
            super("Please set up your business profile first");
        }
    }

    public static class CategoryHasProductsException extends RuntimeException {
        public CategoryHasProductsException() {
            super("Category has products and cannot be deleted");
        }
    }
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException() {
            super("Insufficient stock to complete the operation");
        }
    }

    public static class InvalidQuantityException extends RuntimeException {
        public InvalidQuantityException(String message) {
            super(message);
        }
    }}