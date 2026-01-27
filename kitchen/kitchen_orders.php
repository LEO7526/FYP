<?php
// kitchen_orders.php
// Main page for kitchen order display system
// Handles updating order status from Pending (1) to Done (2)

require_once __DIR__ . '/../conn.php';

// Handle order status update
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && isset($_POST['oid'])) {
    $oid = intval($_POST['oid']);
    $action = $_POST['action'];

    // Set new status based on action
    // Status mapping: 1=Pending, 2=Done, 3=Paid, 4=Cancelled
    if ($action === 'done') {
        $new_status = 2; // Update from Pending (1) to Done (2)
    }

    // Update order status in database
    $stmt = $conn->prepare("UPDATE orders SET ostatus = ? WHERE oid = ?");
    $stmt->bind_param("ii", $new_status, $oid);

    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Order marked as Done']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to update order status']);
    }
    $stmt->close();
    exit;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kitchen Order System</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
<div class="refresh-info">
    <i class="fas fa-sync-alt"></i> Auto-refreshes every 5 seconds
</div>

<div class="orders-container" id="orders-container">
    <!-- Orders will be dynamically loaded here -->
</div>

<script>
    let lastOrderCount = 0;

    // Load orders when page is ready
    document.addEventListener('DOMContentLoaded', function() {
        loadOrders();
        setInterval(loadOrders, 5000); // Refresh every 5 seconds
    });

    // Function to load orders from server
    function loadOrders() {
        fetch('get_orders.php')
            .then(response => response.json())
            .then(data => {
                const container = document.getElementById('orders-container');

                // If no orders, show message
                if (data.orders.length === 0) {
                    container.innerHTML = `
                        <div class="no-orders">
                            <i class="fas fa-concierge-bell"></i>
                            <h2>No Pending Orders</h2>
                            <p>All orders are processed</p>
                        </div>
                    `;
                    lastOrderCount = 0;
                    return;
                }

                // Play notification sound if new orders arrived
                if (data.orders.length > lastOrderCount && lastOrderCount > 0) {
                    playNotificationSound();
                }
                lastOrderCount = data.orders.length;

                // Clear container and render orders
                container.innerHTML = '';
                data.orders.forEach(order => {
                    const orderCard = createOrderCard(order);
                    container.appendChild(orderCard);
                });
            })
            .catch(error => {
                console.error('Error loading orders:', error);
                showErrorMessage('Failed to load orders. Please check connection.');
            });
    }

    // Function to create order card HTML
    function createOrderCard(order) {
        const card = document.createElement('div');
        card.className = 'order-card';
        card.id = 'order-' + order.oid;

        // Status text mapping
        const statusText = {1: 'Pending', 2: 'Done', 3: 'Paid', 4: 'Cancelled'};

        // Order type styling
        const orderTypeText = order.order_type === 'dine_in' ? 'Dine-in' : 'Takeaway';
        const orderTypeClass = order.order_type === 'dine_in' ? 'dine-in' : 'takeaway';

        let itemsHTML = '';

        // Individual items section
        if (order.items && order.items.length > 0) {
            itemsHTML += `<div class="items-section">
                    <div class="section-title"><i class="fas fa-utensils"></i> Individual Items</div>`;

            order.items.forEach(item => {
                itemsHTML += `<div class="item-row">
                        <div>
                            <div class="item-name">${item.item_name}</div>`;

                // Show customizations if any
                if (item.customizations && item.customizations.length > 0) {
                    itemsHTML += `<div class="customization">`;
                    item.customizations.forEach(custom => {
                        itemsHTML += `<div><i class="fas fa-cog"></i> ${custom.group_name}: ${custom.value_name || ''}</div>`;
                    });
                    itemsHTML += `</div>`;
                }

                itemsHTML += `</div>
                        <div class="item-qty">${item.qty}</div>
                    </div>`;
            });

            itemsHTML += `</div>`;
        }

        // Package items section
        if (order.packages && order.packages.length > 0) {
            itemsHTML += `<div class="items-section">
                    <div class="section-title"><i class="fas fa-box"></i> Package Items</div>`;

            order.packages.forEach(pkg => {
                itemsHTML += `<div class="item-row">
                        <div>
                            <div class="item-name">${pkg.package_name} (Package)</div>
                        </div>
                        <div class="item-qty">${pkg.qty}</div>
                    </div>`;
            });

            itemsHTML += `</div>`;
        }

        // Build card HTML
        card.innerHTML = `
                <div class="order-header">
                    <div>
                        <div class="order-id">Order #${order.oid}</div>
                        <div class="order-time">${order.odate} • ${statusText[order.ostatus]}</div>
                    </div>
                    <div>
                        <span class="order-type ${orderTypeClass}">
                            <i class="fas ${order.order_type === 'dine_in' ? 'fa-store' : 'fa-shopping-bag'}"></i>
                            ${orderTypeText}
                            ${order.table_number ? `(Table: ${order.table_number})` : ''}
                        </span>
                    </div>
                </div>

                <div class="order-info">
                    <div>Order Ref: ${order.orderRef}</div>
                </div>

                ${itemsHTML}

                ${order.note ? `<div class="customization">
                    <i class="fas fa-sticky-note"></i> <strong>Note:</strong> ${order.note}
                </div>` : ''}

                <div class="action-buttons">
                    <button class="btn btn-done" onclick="updateOrderStatus(${order.oid}, 'done')">
                        <i class="fas fa-check"></i> Mark as Done
                    </button>
                </div>
            `;

        return card;
    }

    // Function to update order status from Pending (1) to Done (2)
    function updateOrderStatus(oid, action) {
        if (!confirm(`Are you sure you want to mark order #${oid} as Done?`)) {
            return;
        }

        const formData = new FormData();
        formData.append('oid', oid);
        formData.append('action', action);

        fetch('kitchen_orders.php', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showSuccessMessage(data.message);
                    loadOrders(); // Refresh order list
                } else {
                    showErrorMessage(data.message);
                }
            })
            .catch(error => {
                console.error('Error updating order status:', error);
                showErrorMessage('Update failed. Please check connection.');
            });
    }

    // Function to play notification sound for new orders
    function playNotificationSound() {
        try {
            const audio = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-correct-answer-tone-2870.mp3');
            audio.volume = 0.3;
            audio.play();
        } catch (e) {
            console.log('Audio notification not available');
        }
    }

    // Function to show success message
    function showSuccessMessage(message) {
        alert('✓ ' + message);
    }

    // Function to show error message
    function showErrorMessage(message) {
        alert('✗ ' + message);
    }
</script>
</body>
</html>