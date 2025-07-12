/**
 * 🛒 Модуль для работы с корзиной
 * Файл: /js/store/addToCart.js
 */

// ✅ Локальные переменные модуля (не конфликтуют с глобальными)
let cartModuleAuth = {
    isAuthenticated: false,
    jwtToken: null
};

// ✅ Инициализация модуля
function initCartModule(authStatus, token) {
    cartModuleAuth.isAuthenticated = authStatus;
    cartModuleAuth.jwtToken = token;
    console.log('🛒 Cart module initialized:', { authStatus, hasToken: !!token });
}

// ✅ Основная функция добавления в корзину
async function addToCart(productId, storeId, quantity = 1) {
    console.log(`🛒 Adding product ${productId} from store ${storeId} to cart (qty: ${quantity})`);

    // Проверка авторизации
    if (!cartModuleAuth.isAuthenticated || !cartModuleAuth.jwtToken) {
        showCartNotification('Необходимо войти в систему для добавления товаров в корзину', 'error');
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
        return false;
    }

    // Валидация параметров
    if (!productId || !storeId) {
        console.error('❌ Invalid parameters:', { productId, storeId });
        showCartNotification('Ошибка: некорректные данные товара', 'error');
        return false;
    }

    try {
        // Находим кнопку и показываем состояние загрузки
        const button = document.querySelector(`[data-product-id="${productId}"]`);
        const originalContent = setButtonLoading(button);

        console.log('📡 Sending request to add product to cart...');

        // TODO: Заменить на реальный API вызов когда будет готов backend
        const response = await simulateCartRequest(productId, storeId, quantity);

        if (response.success) {
            console.log('✅ Product added to cart successfully');
            showCartNotification(response.message || 'Товар добавлен в корзину!', 'success');

            // Обновляем счетчик корзины
            await updateCartCounter();

            // Анимация успеха
            animateSuccessButton(button);

            return true;
        } else {
            console.error('❌ Failed to add product to cart:', response.message);
            showCartNotification(response.message || 'Ошибка добавления в корзину', 'error');
            return false;
        }

    } catch (error) {
        console.error('💥 Exception while adding to cart:', error);
        showCartNotification('Произошла ошибка при добавлении товара', 'error');
        return false;
    } finally {
        // Восстанавливаем кнопку
        const button = document.querySelector(`[data-product-id="${productId}"]`);
        restoreButton(button);
    }
}

// ✅ Симуляция API запроса (заменить на реальный)
async function simulateCartRequest(productId, storeId, quantity) {
    // Имитируем задержку сети
    await new Promise(resolve => setTimeout(resolve, 1000));

    // Имитируем успешный ответ
    return {
        success: true,
        message: `Товар ${productId} добавлен в корзину!`,
        data: {
            productId,
            storeId,
            quantity,
            cartItemId: Date.now()
        }
    };
}

// ✅ БУДУЩАЯ реальная функция для API
async function addToCartAPI(productId, storeId, quantity) {
    const response = await fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${cartModuleAuth.jwtToken}`,
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            productId: productId,
            storeId: storeId,
            quantity: quantity
        })
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
}

// ✅ Обновление счетчика корзины
async function updateCartCounter() {
    try {
        // TODO: Заменить на реальный API
        const count = Math.floor(Math.random() * 10) + 1; // Симуляция

        const counter = document.querySelector('.cart-counter');
        const badge = document.querySelector('.cart-badge');

        if (counter) {
            counter.textContent = count;
            counter.style.display = count > 0 ? 'inline' : 'none';
        }

        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline' : 'none';
        }

        console.log(`🛒 Cart counter updated: ${count}`);
    } catch (error) {
        console.warn('⚠️ Failed to update cart counter:', error);
    }
}

// ✅ Управление состоянием кнопки
function setButtonLoading(button) {
    if (!button) return null;

    const originalContent = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Добавляем...';
    button.classList.add('loading');

    return originalContent;
}

function restoreButton(button, originalContent = null) {
    if (!button) return;

    button.disabled = false;
    button.classList.remove('loading', 'success');

    if (originalContent) {
        button.innerHTML = originalContent;
    } else {
        // Восстанавливаем стандартный контент
        const isAvailable = !button.hasAttribute('data-unavailable');
        button.innerHTML = isAvailable
            ? '<i class="fas fa-plus"></i> Добавить'
            : '<i class="fas fa-ban"></i> Недоступно';
    }
}

function animateSuccessButton(button) {
    if (!button) return;

    button.classList.add('success');
    button.innerHTML = '<i class="fas fa-check"></i> Добавлено!';

    setTimeout(() => {
        restoreButton(button);
    }, 2000);
}

// ✅ Показ уведомлений
function showCartNotification(message, type = 'info') {
    // Удаляем предыдущие уведомления
    const existingNotifications = document.querySelectorAll('.cart-notification');
    existingNotifications.forEach(n => n.remove());

    // Создаем новое уведомление
    const notification = document.createElement('div');
    notification.className = `cart-notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        max-width: 350px;
        padding: 1rem 1.5rem;
        border-radius: 0.8rem;
        box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        display: flex;
        align-items: center;
        gap: 0.8rem;
        font-weight: 500;
        animation: slideInRight 0.3s ease;
    `;

    // Стили в зависимости от типа
    const styles = {
        success: { bg: '#00d967', color: 'white', icon: 'fa-check-circle' },
        error: { bg: '#dc3545', color: 'white', icon: 'fa-exclamation-triangle' },
        info: { bg: '#0dcaf0', color: 'white', icon: 'fa-info-circle' }
    };

    const style = styles[type] || styles.info;
    notification.style.background = style.bg;
    notification.style.color = style.color;

    notification.innerHTML = `
        <i class="fas ${style.icon}"></i>
        <span>${message}</span>
        <button onclick="this.parentElement.remove()" 
                style="background: none; border: none; color: inherit; font-size: 1.2rem; cursor: pointer; margin-left: auto;">
            <i class="fas fa-times"></i>
        </button>
    `;

    document.body.appendChild(notification);

    // Автоматическое удаление
    setTimeout(() => {
        if (notification.parentElement) {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => notification.remove(), 300);
        }
    }, 4000);
}

// ✅ CSS анимации для уведомлений
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    .loading {
        pointer-events: none;
        opacity: 0.7;
    }
    
    .success {
        background-color: #00d967 !important;
        color: white !important;
    }
`;
document.head.appendChild(style);

// ✅ Автоинициализация если данные доступны
document.addEventListener('DOMContentLoaded', function() {
    // Пытаемся получить данные из глобальных переменных или мета-тегов
    if (typeof window.isAuthenticated !== 'undefined' && typeof window.jwtToken !== 'undefined') {
        initCartModule(window.isAuthenticated, window.jwtToken);
    }

    console.log('🛒 AddToCart module loaded');
});