/**
 * Ocean View Resort — Main JavaScript
 * Vanilla JS — no jQuery
 */

// ── Shared confirm modal ──────────────────────────────────────────────────────
(function () {
    'use strict';

    var modal   = document.getElementById('confirmModal');
    var msgEl   = document.getElementById('confirmModalMessage');
    var okBtn   = document.getElementById('confirmModalOk');
    var cancelBtn = document.getElementById('confirmModalCancel');
    var closeBtn  = document.getElementById('confirmModalClose');

    if (!modal) return; // modal not present on this page (e.g. public pages)

    var pendingAction = null; // function to call on confirm

    function openModal(message, onConfirm) {
        msgEl.textContent = message;
        pendingAction = onConfirm;
        modal.classList.add('open');
        okBtn.focus();
    }

    function closeModal() {
        modal.classList.remove('open');
        pendingAction = null;
    }

    okBtn.addEventListener('click', function () {
        var action = pendingAction;
        closeModal();
        if (action) action();
    });

    cancelBtn.addEventListener('click', closeModal);
    closeBtn.addEventListener('click', closeModal);

    // Close on backdrop click
    modal.addEventListener('click', function (e) {
        if (e.target === modal) closeModal();
    });

    // Close on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('open')) closeModal();
    });

    // 1. Replace native confirm() for all [data-confirm] elements
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            e.preventDefault();
            var message = el.dataset.confirm || 'Are you sure?';
            openModal(message, function () {
                // Re-dispatch as a trusted-like click without the confirm guard,
                // or — for form submit buttons — submit the parent form directly.
                el.removeAttribute('data-confirm');
                if (el.form) {
                    el.form.submit();
                } else {
                    el.click();
                }
                el.setAttribute('data-confirm', message); // restore for SPA nav
            });
        });
    });

    // Expose globally for dynamic content (e.g. injected via AJAX)
    window.showConfirmModal = openModal;
})();

// 2. Auto-dismiss alerts after 4 seconds
document.querySelectorAll('.alert').forEach(function(el) {
    setTimeout(function() {
        el.style.opacity = '0';
        setTimeout(function() { el.style.display = 'none'; }, 500);
    }, 4000);
});

// 3. Sidebar toggle for mobile
var sidebarToggle = document.querySelector('.sidebar-toggle');
if (sidebarToggle) {
    sidebarToggle.addEventListener('click', function() {
        var sidebar = document.querySelector('.sidebar');
        if (sidebar) {
            sidebar.classList.toggle('collapsed');
        }
    });
}

// 4. Date range validation — disable check-out dates before check-in
var checkIn  = document.getElementById('checkIn');
var checkOut = document.getElementById('checkOut');
if (checkIn && checkOut) {
    checkIn.addEventListener('change', function() {
        checkOut.min = checkIn.value;
        if (checkOut.value && checkOut.value <= checkIn.value) {
            checkOut.value = '';
        }
        updateTotal();
    });
    checkOut.addEventListener('change', updateTotal);
}

// 5. Auto-calculate total amount via AJAX when room + dates change
var roomSelect = document.getElementById('roomId');
if (roomSelect) {
    roomSelect.addEventListener('change', updateTotal);
}

function updateTotal() {
    var roomId = document.getElementById('roomId') ? document.getElementById('roomId').value : null;
    var cIn    = checkIn ? checkIn.value : null;
    var cOut   = checkOut ? checkOut.value : null;

    if (!roomId || !cIn || !cOut || cOut <= cIn) return;

    var nights = Math.round((new Date(cOut) - new Date(cIn)) / 86400000);
    if (nights <= 0) return;

    fetch(contextPath + '/rooms?action=rate&id=' + roomId)
        .then(function(r) { return r.json(); })
        .then(function(d) {
            var total = (nights * d.rate).toFixed(2);
            var el = document.getElementById('totalAmount');
            if (el) {
                el.textContent = 'LKR ' + parseFloat(total).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            }
        })
        .catch(function(err) {
            console.error('Rate fetch error:', err);
        });
}

// 6. Payment method helper text toggle
document.querySelectorAll('input[name="paymentMethod"]').forEach(function(radio) {
    radio.addEventListener('change', function() {
        var helpText = document.getElementById('refHelp');
        if (helpText) {
            helpText.textContent =
                radio.value === 'CASH'
                    ? 'Enter the cash receipt number given to the guest'
                    : 'Enter the transaction ID shown on the card terminal';
        }
    });
});

// 7. Active sidebar link highlighting
(function() {
    var currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(function(link) {
        var href = link.getAttribute('href');
        if (href && currentPath.indexOf(href) !== -1) {
            link.classList.add('active');
        }
    });
})();

