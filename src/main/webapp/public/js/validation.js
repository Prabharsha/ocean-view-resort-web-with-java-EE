/**
 * Ocean View Resort — Form Validation
 * Client-side validation (server-side is the authoritative check)
 */

(function() {
    'use strict';

    // Email validation
    function isValidEmail(email) {
        return /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/.test(email);
    }

    // Phone validation
    function isValidPhone(phone) {
        return /^[+]?[0-9\-\s]{7,20}$/.test(phone);
    }

    // Add validation styles
    function showError(input, message) {
        clearError(input);
        input.style.borderColor = '#c0392b';
        var errorEl = document.createElement('small');
        errorEl.className = 'field-error';
        errorEl.style.color = '#c0392b';
        errorEl.style.fontSize = '12px';
        errorEl.style.display = 'block';
        errorEl.style.marginTop = '4px';
        errorEl.textContent = message;
        input.parentNode.appendChild(errorEl);
    }

    function clearError(input) {
        input.style.borderColor = '';
        var existing = input.parentNode.querySelector('.field-error');
        if (existing) existing.remove();
    }

    // Validate on blur for email fields
    document.querySelectorAll('input[type="email"]').forEach(function(input) {
        input.addEventListener('blur', function() {
            if (input.value && !isValidEmail(input.value)) {
                showError(input, 'Please enter a valid email address');
            } else {
                clearError(input);
            }
        });
    });

    // Validate on blur for phone/tel fields
    document.querySelectorAll('input[type="tel"]').forEach(function(input) {
        input.addEventListener('blur', function() {
            if (input.value && !isValidPhone(input.value)) {
                showError(input, 'Please enter a valid phone number');
            } else {
                clearError(input);
            }
        });
    });

    // Validate number min on blur
    document.querySelectorAll('input[type="number"][min]').forEach(function(input) {
        input.addEventListener('blur', function() {
            var min = parseFloat(input.getAttribute('min'));
            if (input.value !== '' && parseFloat(input.value) < min) {
                showError(input, 'Value must be at least ' + min);
            } else {
                clearError(input);
            }
        });
    });

    // Prevent form submission if required fields are empty
    document.querySelectorAll('form').forEach(function(form) {
        form.addEventListener('submit', function(e) {
            var valid = true;
            form.querySelectorAll('[required]').forEach(function(input) {
                clearError(input);
                if (!input.value || !input.value.trim()) {
                    showError(input, 'This field is required');
                    valid = false;
                }
            });

            // Check email fields
            form.querySelectorAll('input[type="email"]').forEach(function(input) {
                if (input.value && !isValidEmail(input.value)) {
                    showError(input, 'Please enter a valid email address');
                    valid = false;
                }
            });

            if (!valid) {
                e.preventDefault();
                // Scroll to first error
                var firstError = form.querySelector('.field-error');
                if (firstError) {
                    firstError.parentNode.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    });

})();

