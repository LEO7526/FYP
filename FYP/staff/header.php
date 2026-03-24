<?php
?>
<header>
    <div class="hamburger-menu" id="hamburgerMenu">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div class="logo">
        <a href="staffIndex.php">Yummy Restaurant</a>
    </div>
    <nav class="main-nav">
        <a href="MenuManagement.php" class="nav-button insert-items">Menu Management</a>
        <a href="newInventory.php" class="nav-button insert-materials">Inventory</a>
        <a href="bookingList.php" class="nav-button order-list">Reservations</a>
        <a href="salesReport.php" class="nav-button report">Sales Reports</a>
    </nav>

    <div class="user-actions">
        <a href="staffProfile.php" class="profile-btn">Profile</a>
        <a href="../logout.php" class="logout-btn">Log out</a>
    </div>
</header>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const hamburger = document.getElementById('hamburgerMenu');
        const mainNav = document.querySelector('.main-nav');
        const userActions = document.querySelector('.user-actions');

        if (hamburger && mainNav && userActions) {
            hamburger.addEventListener('click', function() {
                hamburger.classList.toggle('active');
                mainNav.classList.toggle('active');
                document.body.style.overflow = mainNav.classList.contains('active') ? 'hidden' : '';
            });

            document.querySelectorAll('.nav-button').forEach(button => {
                button.addEventListener('click', function() {
                    hamburger.classList.remove('active');
                    mainNav.classList.remove('active');
                    document.body.style.overflow = '';
                });
            });

            document.querySelectorAll('.user-actions a').forEach(link => {
                link.addEventListener('click', function() {
                    hamburger.classList.remove('active');
                    mainNav.classList.remove('active');
                    document.body.style.overflow = '';
                });
            });

            document.addEventListener('click', function(e) {
                if (!hamburger.contains(e.target) && !mainNav.contains(e.target) && !userActions.contains(e.target)) {
                    hamburger.classList.remove('active');
                    mainNav.classList.remove('active');
                    document.body.style.overflow = '';
                }
            });

            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape' && mainNav.classList.contains('active')) {
                    hamburger.classList.remove('active');
                    mainNav.classList.remove('active');
                    document.body.style.overflow = '';
                }
            });
        }
    });
</script>