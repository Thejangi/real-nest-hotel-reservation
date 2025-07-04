/**
 * Customer Interface Utilities
 * Common functions and utilities for the hotel reservation customer-facing interfaces
 * Version: 1.0.0
 */

// ============================================================================
// CONFIGURATION & CONSTANTS
// ============================================================================

const HOTEL_CONFIG = {
    API_BASE_URL: '/api',
    DEFAULT_CURRENCY: 'USD',
    DEFAULT_TIMEZONE: 'America/New_York',
    BOOKING_REF_PREFIX: 'HTL',
    MAX_BOOKING_MONTHS: 12,
    MIN_STAY_NIGHTS: 1,
    MAX_STAY_NIGHTS: 30,
    RATES: {
        TAX_PERCENTAGE: 10,
        SERVICE_CHARGE: 15,
        MODIFICATION_FEE: 25,
        CANCELLATION_HOURS: 24
    },
    ROOM_PRICES: {
        'Standard': 150,
        'Deluxe': 200,
        'Junior Suite': 300,
        'Presidential Suite': 500
    },
    PAYMENT_METHODS: {
        CARD: 'card',
        PAYPAL: 'paypal',
        BANK_TRANSFER: 'bank_transfer',
        CASH: 'cash'
    }
};

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Date and Time Utilities
 */
const DateUtils = {
    /**
     * Get today's date in YYYY-MM-DD format
     */
    getToday: () => new Date().toISOString().split('T')[0],

    /**
     * Get tomorrow's date in YYYY-MM-DD format
     */
    getTomorrow: () => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        return tomorrow.toISOString().split('T')[0];
    },

    /**
     * Add days to a date
     */
    addDays: (date, days) => {
        const result = new Date(date);
        result.setDate(result.getDate() + days);
        return result;
    },

    /**
     * Calculate nights between two dates
     */
    calculateNights: (checkinDate, checkoutDate) => {
        const checkin = new Date(checkinDate);
        const checkout = new Date(checkoutDate);
        const timeDiff = checkout.getTime() - checkin.getTime();
        return Math.ceil(timeDiff / (1000 * 3600 * 24));
    },

    /**
     * Format date for display
     */
    formatDisplayDate: (dateString) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    },

    /**
     * Check if date is in the past
     */
    isPastDate: (dateString) => {
        return new Date(dateString) < new Date();
    },

    /**
     * Get hours until a specific date
     */
    getHoursUntil: (dateString) => {
        const targetDate = new Date(dateString);
        const now = new Date();
        return (targetDate.getTime() - now.getTime()) / (1000 * 60 * 60);
    }
};

/**
 * Validation Utilities
 */
const ValidationUtils = {
    /**
     * Validate email format
     */
    isValidEmail: (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },

    /**
     * Validate phone number format
     */
    isValidPhone: (phone) => {
        const phoneRegex = /^\+?[\d\s\-\(\)]{7,15}$/;
        return phoneRegex.test(phone);
    },

    /**
     * Validate credit card number (basic Luhn algorithm)
     */
    isValidCardNumber: (cardNumber) => {
        const num = cardNumber.replace(/\s/g, '');
        if (!/^\d{13,19}$/.test(num)) return false;

        let sum = 0;
        let isEven = false;

        for (let i = num.length - 1; i >= 0; i--) {
            let digit = parseInt(num[i]);

            if (isEven) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }

            sum += digit;
            isEven = !isEven;
        }

        return sum % 10 === 0;
    },

    /**
     * Validate booking dates
     */
    validateBookingDates: (checkinDate, checkoutDate) => {
        const errors = [];

        if (!checkinDate || !checkoutDate) {
            errors.push('Both check-in and check-out dates are required');
            return errors;
        }

        const checkin = new Date(checkinDate);
        const checkout = new Date(checkoutDate);
        const today = new Date();

        if (checkin < today) {
            errors.push('Check-in date cannot be in the past');
        }

        if (checkout <= checkin) {
            errors.push('Check-out date must be after check-in date');
        }

        const nights = DateUtils.calculateNights(checkinDate, checkoutDate);
        if (nights < HOTEL_CONFIG.MIN_STAY_NIGHTS) {
            errors.push(`Minimum stay is ${HOTEL_CONFIG.MIN_STAY_NIGHTS} night(s)`);
        }

        if (nights > HOTEL_CONFIG.MAX_STAY_NIGHTS) {
            errors.push(`Maximum stay is ${HOTEL_CONFIG.MAX_STAY_NIGHTS} nights`);
        }

        return errors;
    }
};

/**
 * Booking Utilities
 */
const BookingUtils = {
    /**
     * Generate booking reference
     */
    generateBookingReference: () => {
        const timestamp = Date.now().toString().slice(-6);
        return `${HOTEL_CONFIG.BOOKING_REF_PREFIX}${timestamp}`;
    },

    /**
     * Calculate booking total amount
     */
    calculateBookingAmount: (roomType, nights, additionalCharges = {}) => {
        const roomPrice = HOTEL_CONFIG.ROOM_PRICES[roomType] || 0;
        const subtotal = roomPrice * nights;

        // Add additional charges
        const additionalTotal = Object.values(additionalCharges).reduce((sum, charge) => sum + (parseFloat(charge) || 0), 0);

        // Calculate taxes and fees
        const taxes = (subtotal + additionalTotal) * (HOTEL_CONFIG.RATES.TAX_PERCENTAGE / 100);
        const serviceCharge = HOTEL_CONFIG.RATES.SERVICE_CHARGE;

        const total = subtotal + additionalTotal + taxes + serviceCharge;

        return {
            roomPrice: roomPrice,
            subtotal: subtotal,
            additionalCharges: additionalTotal,
            taxes: taxes,
            serviceCharge: serviceCharge,
            total: total
        };
    },

    /**
     * Calculate modification fee
     */
    calculateModificationFee: (checkinDate) => {
        const hoursUntilCheckin = DateUtils.getHoursUntil(checkinDate);
        return hoursUntilCheckin < HOTEL_CONFIG.RATES.CANCELLATION_HOURS ? HOTEL_CONFIG.RATES.MODIFICATION_FEE : 0;
    },

    /**
     * Calculate cancellation fee
     */
    calculateCancellationFee: (checkinDate, totalAmount) => {
        const hoursUntilCheckin = DateUtils.getHoursUntil(checkinDate);
        if (hoursUntilCheckin >= HOTEL_CONFIG.RATES.CANCELLATION_HOURS) {
            return 0; // Free cancellation
        } else {
            return totalAmount * 0.5; // 50% fee
        }
    },

    /**
     * Get booking status color class
     */
    getStatusColorClass: (status) => {
        const statusColors = {
            'Confirmed': 'bg-success',
            'Pending Payment': 'bg-warning text-dark',
            'Cancelled': 'bg-danger',
            'Completed': 'bg-info',
            'Checked In': 'bg-primary',
            'No Show': 'bg-secondary'
        };
        return statusColors[status] || 'bg-secondary';
    }
};

/**
 * UI Utilities
 */
const UIUtils = {
    /**
     * Show loading spinner
     */
    showLoading: (message = 'Loading...') => {
        Swal.fire({
            title: message,
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });
    },

    /**
     * Hide loading spinner
     */
    hideLoading: () => {
        Swal.close();
    },

    /**
     * Show success message
     */
    showSuccess: (title, message, timer = 2000) => {
        return Swal.fire({
            title: title,
            text: message,
            icon: 'success',
            timer: timer,
            showConfirmButton: timer === null
        });
    },

    /**
     * Show error message
     */
    showError: (title, message) => {
        return Swal.fire({
            title: title,
            text: message,
            icon: 'error',
            confirmButtonText: 'OK'
        });
    },

    /**
     * Show confirmation dialog
     */
    showConfirm: (title, message, confirmText = 'Yes', cancelText = 'No') => {
        return Swal.fire({
            title: title,
            text: message,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: confirmText,
            cancelButtonText: cancelText
        });
    },

    /**
     * Format currency amount
     */
    formatCurrency: (amount, currency = HOTEL_CONFIG.DEFAULT_CURRENCY) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency
        }).format(amount);
    },

    /**
     * Format card number for display
     */
    formatCardNumber: (cardNumber) => {
        return cardNumber.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim();
    },

    /**
     * Mask card number for security
     */
    maskCardNumber: (cardNumber) => {
        const cleaned = cardNumber.replace(/\s/g, '');
        const lastFour = cleaned.slice(-4);
        return `**** **** **** ${lastFour}`;
    },

    /**
     * Detect card type from number
     */
    detectCardType: (cardNumber) => {
        const number = cardNumber.replace(/\s/g, '');

        if (/^4/.test(number)) return 'visa';
        if (/^5[1-5]/.test(number) || /^2[2-7]/.test(number)) return 'mastercard';
        if (/^3[47]/.test(number)) return 'amex';
        if (/^6/.test(number)) return 'discover';

        return 'unknown';
    },

    /**
     * Update step indicator
     */
    updateStepIndicator: (currentStep, totalSteps) => {
        for (let i = 1; i <= totalSteps; i++) {
            const stepElement = document.getElementById(`step${i}`);
            if (stepElement) {
                stepElement.classList.remove('active', 'completed');
                if (i < currentStep) {
                    stepElement.classList.add('completed');
                } else if (i === currentStep) {
                    stepElement.classList.add('active');
                }
            }
        }
    }
};

/**
 * Storage Utilities (for demo purposes)
 */
const StorageUtils = {
    /**
     * Save booking to localStorage
     */
    saveBooking: (booking) => {
        const bookings = StorageUtils.getBookings();
        bookings.push({
            ...booking,
            id: Date.now(),
            createdAt: new Date().toISOString()
        });
        localStorage.setItem('customerBookings', JSON.stringify(bookings));
        return bookings[bookings.length - 1];
    },

    /**
     * Get all bookings from localStorage
     */
    getBookings: () => {
        return JSON.parse(localStorage.getItem('customerBookings') || '[]');
    },

    /**
     * Get booking by reference
     */
    getBookingByReference: (reference) => {
        const bookings = StorageUtils.getBookings();
        return bookings.find(booking => booking.reference === reference);
    },

    /**
     * Update booking in localStorage
     */
    updateBooking: (reference, updates) => {
        const bookings = StorageUtils.getBookings();
        const index = bookings.findIndex(booking => booking.reference === reference);

        if (index !== -1) {
            bookings[index] = { ...bookings[index], ...updates, updatedAt: new Date().toISOString() };
            localStorage.setItem('customerBookings', JSON.stringify(bookings));
            return bookings[index];
        }

        return null;
    },

    /**
     * Search bookings by email or reference
     */
    searchBookings: (email, reference) => {
        const bookings = StorageUtils.getBookings();

        return bookings.filter(booking => {
            const emailMatch = email ? booking.guest?.email?.toLowerCase() === email.toLowerCase() : true;
            const refMatch = reference ? booking.reference?.toLowerCase().includes(reference.toLowerCase()) : true;
            return emailMatch && refMatch;
        });
    }
};

/**
 * API Utilities
 */
const APIUtils = {
    /**
     * Base API request function
     */
    request: async (endpoint, options = {}) => {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(`${HOTEL_CONFIG.API_BASE_URL}${endpoint}`, config);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    },

    /**
     * Search available rooms
     */
    searchRooms: async (checkinDate, checkoutDate, adults, children = 0) => {
        return APIUtils.request(`/bookings/search?checkin=${checkinDate}&checkout=${checkoutDate}&adults=${adults}&children=${children}`);
    },

    /**
     * Create new booking
     */
    createBooking: async (bookingData) => {
        return APIUtils.request('/bookings', {
            method: 'POST',
            body: JSON.stringify(bookingData)
        });
    },

    /**
     * Get booking by reference
     */
    getBooking: async (reference) => {
        return APIUtils.request(`/bookings/${reference}`);
    },

    /**
     * Modify booking
     */
    modifyBooking: async (reference, modifications) => {
        return APIUtils.request(`/bookings/${reference}/modify`, {
            method: 'PUT',
            body: JSON.stringify(modifications)
        });
    },

    /**
     * Cancel booking
     */
    cancelBooking: async (reference, reason) => {
        return APIUtils.request(`/bookings/${reference}/cancel`, {
            method: 'POST',
            body: JSON.stringify({ reason })
        });
    },

    /**
     * Process payment
     */
    processPayment: async (paymentData) => {
        return APIUtils.request('/payments/process', {
            method: 'POST',
            body: JSON.stringify(paymentData)
        });
    },

    /**
     * Send email
     */
    sendEmail: async (emailData) => {
        return APIUtils.request('/emails/send', {
            method: 'POST',
            body: JSON.stringify(emailData)
        });
    }
};

/**
 * Form Utilities
 */
const FormUtils = {
    /**
     * Setup form validation
     */
    setupValidation: (formSelector) => {
        const form = document.querySelector(formSelector);
        if (!form) return;

        form.addEventListener('submit', function (e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    },

    /**
     * Setup date inputs with constraints
     */
    setupDateInputs: (checkinSelector, checkoutSelector) => {
        const checkinInput = document.querySelector(checkinSelector);
        const checkoutInput = document.querySelector(checkoutSelector);

        if (!checkinInput || !checkoutInput) return;

        // Set minimum dates
        checkinInput.min = DateUtils.getToday();

        checkinInput.addEventListener('change', function () {
            const checkinDate = new Date(this.value);
            checkinDate.setDate(checkinDate.getDate() + 1);
            checkoutInput.min = checkinDate.toISOString().split('T')[0];

            // Reset checkout if it's before new minimum
            if (checkoutInput.value && new Date(checkoutInput.value) <= new Date(this.value)) {
                checkoutInput.value = checkinDate.toISOString().split('T')[0];
            }
        });
    },

    /**
     * Setup card number formatting
     */
    setupCardFormatting: (cardSelector, expirySelector, cvvSelector) => {
        const cardInput = document.querySelector(cardSelector);
        const expiryInput = document.querySelector(expirySelector);
        const cvvInput = document.querySelector(cvvSelector);

        if (cardInput) {
            cardInput.addEventListener('input', function () {
                let value = this.value.replace(/\s/g, '').replace(/[^0-9]/gi, '');
                let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value;
                this.value = formattedValue;

                // Update card type indicator if exists
                const cardType = UIUtils.detectCardType(value);
                const cardIcon = document.querySelector('.card-type-icon');
                if (cardIcon) {
                    cardIcon.className = `card-type-icon fab fa-cc-${cardType}`;
                }
            });
        }

        if (expiryInput) {
            expiryInput.addEventListener('input', function () {
                let value = this.value.replace(/\D/g, '');
                if (value.length >= 2) {
                    value = value.substring(0, 2) + '/' + value.substring(2, 4);
                }
                this.value = value;
            });
        }

        if (cvvInput) {
            cvvInput.addEventListener('input', function () {
                this.value = this.value.replace(/\D/g, '');
            });
        }
    },

    /**
     * Collect form data as object
     */
    collectFormData: (formSelector) => {
        const form = document.querySelector(formSelector);
        if (!form) return {};

        const formData = new FormData(form);
        const data = {};

        for (let [key, value] of formData.entries()) {
            if (data[key]) {
                if (Array.isArray(data[key])) {
                    data[key].push(value);
                } else {
                    data[key] = [data[key], value];
                }
            } else {
                data[key] = value;
            }
        }

        return data;
    }
};

/**
 * URL Utilities
 */
const URLUtils = {
    /**
     * Get URL parameters as object
     */
    getURLParams: () => {
        const params = new URLSearchParams(window.location.search);
        const result = {};
        for (let [key, value] of params.entries()) {
            result[key] = value;
        }
        return result;
    },

    /**
     * Build URL with parameters
     */
    buildURL: (baseURL, params) => {
        const url = new URL(baseURL, window.location.origin);
        Object.keys(params).forEach(key => {
            if (params[key] !== undefined && params[key] !== null) {
                url.searchParams.append(key, params[key]);
            }
        });
        return url.toString();
    },

    /**
     * Navigate with parameters
     */
    navigateWithParams: (page, params) => {
        const url = URLUtils.buildURL(page, params);
        window.location.href = url;
    }
};

// ============================================================================
// EMAIL TEMPLATES (for demo purposes)
// ============================================================================

const EmailTemplates = {
    bookingConfirmation: (booking) => ({
        subject: `Booking Confirmation - ${booking.reference}`,
        template: 'booking-confirmation',
        data: booking
    }),

    paymentReminder: (booking) => ({
        subject: `Payment Reminder - ${booking.reference}`,
        template: 'payment-reminder',
        data: booking
    }),

    modificationConfirmation: (booking) => ({
        subject: `Booking Modified - ${booking.reference}`,
        template: 'modification-confirmation',
        data: booking
    }),

    cancellationConfirmation: (booking) => ({
        subject: `Booking Cancelled - ${booking.reference}`,
        template: 'cancellation-confirmation',
        data: booking
    })
};

// ============================================================================
// INITIALIZATION
// ============================================================================

/**
 * Initialize common functionality when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', function () {
    // Setup common form functionality
    FormUtils.setupValidation('form');

    // Setup tooltips if Bootstrap is available
    if (typeof bootstrap !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    // Setup date inputs if they exist
    FormUtils.setupDateInputs('#checkinDate, #checkInDate', '#checkoutDate, #checkOutDate');

    // Setup card formatting if payment form exists
    FormUtils.setupCardFormatting('#cardNumber', '#expiryDate', '#cvv');

    console.log('Customer Interface Utilities initialized');
});

// ============================================================================
// EXPORTS (for module environments)
// ============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        HOTEL_CONFIG,
        DateUtils,
        ValidationUtils,
        BookingUtils,
        UIUtils,
        StorageUtils,
        APIUtils,
        FormUtils,
        URLUtils,
        EmailTemplates
    };
}

// Make utilities available globally
window.HotelUtils = {
    HOTEL_CONFIG,
    DateUtils,
    ValidationUtils,
    BookingUtils,
    UIUtils,
    StorageUtils,
    APIUtils,
    FormUtils,
    URLUtils,
    EmailTemplates
};