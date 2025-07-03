// === НАВИГАЦИЯ JAVASCRIPT ===
function initNavigation() {
    // Мобильное меню
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const navMenu = document.getElementById('navMenu');

    if (mobileMenuBtn && navMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            this.classList.toggle('active');
            navMenu.classList.toggle('active');
        });

        // Закрытие мобильного меню при клике на ссылку
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                mobileMenuBtn.classList.remove('active');
                navMenu.classList.remove('active');
            });
        });
    }

    // Избранное меню
    const favoriteBtn = document.getElementById('favoriteBtn');
    const favoriteDropdown = document.querySelector('.favorite-dropdown');

    if (favoriteBtn && favoriteDropdown) {
        favoriteBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            favoriteDropdown.classList.toggle('active');
        });

        // Закрытие выпадающего меню при клике вне его
        document.addEventListener('click', function(e) {
            if (!favoriteDropdown.contains(e.target)) {
                favoriteDropdown.classList.remove('active');
            }
        });
    }

    // Активная ссылка в меню
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            if (this.getAttribute('href').startsWith('#')) {
                e.preventDefault();
            }

            // Убираем активный класс у всех ссылок
            document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

            // Добавляем активный класс к текущей ссылке
            this.classList.add('active');
        });
    });

    // Поиск
    const searchInput = document.querySelector('.search-input');
    const searchBtn = document.querySelector('.search-btn');

    if (searchBtn && searchInput) {
        searchBtn.addEventListener('click', function() {
            if (searchInput.value.trim()) {
                console.log('Поиск:', searchInput.value);
                // Здесь добавьте логику поиска
            }
        });

        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && this.value.trim()) {
                console.log('Поиск:', this.value);
                // Здесь добавьте логику поиска
            }
        });
    }

    // Корзина
    const cartBtn = document.getElementById('cartBtn');
    const cartCount = document.querySelector('.cart-count');

    if (cartBtn && cartCount) {
        let count = 0;

        cartBtn.addEventListener('click', function() {
            count++;
            cartCount.textContent = count;
        });
    }

    // Скролл эффект для навигации
    let lastScrollTop = 0;
    const navbar = document.querySelector('.navbar');

    if (navbar) {
        window.addEventListener('scroll', function() {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

            // Добавляем класс при скролле
            if (scrollTop > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }

            lastScrollTop = scrollTop;
        });
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', initNavigation);