/**
 * profile-utils.js
 * Утилиты для работы с токенами и API запросами
 */

export class ProfileUtils {
    /**
     * Получение информации о токене из localStorage/sessionStorage/cookie
     * @returns {Object|null} Объект с токеном или null
     */
    static getTokenInfo() {
        // Пробуем получить из localStorage
        let token = localStorage.getItem('authToken');
        if (token) {
            return { token: token, source: 'localStorage' };
        }

        // Пробуем получить из sessionStorage
        token = sessionStorage.getItem('authToken');
        if (token) {
            return { token: token, source: 'sessionStorage' };
        }

        // Пробуем получить из cookie
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'authToken') {
                return { token: value, source: 'cookie' };
            }
        }

        return null;
    }

    /**
     * Универсальный AJAX клиент с авторизацией
     * @param {string} url - URL для запроса
     * @param {Object} options - Опции fetch
     * @returns {Promise} Promise с ответом
     */
    static async makeAuthenticatedRequest(url, options = {}) {
        const tokenInfo = this.getTokenInfo();

        if (!tokenInfo || !tokenInfo.token) {
            throw new Error('Токен авторизации не найден');
        }

        // Добавляем заголовки авторизации
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${tokenInfo.token}`,
            'X-Requested-With': 'XMLHttpRequest',
            ...options.headers
        };

        const requestOptions = {
            ...options,
            headers
        };

        console.log(`🚀 API Request: ${options.method || 'GET'} ${url}`);

        try {
            const response = await fetch(url, requestOptions);

            if (!response.ok) {
                if (response.status === 401) {
                    this.handleUnauthorized();
                    throw new Error('Сессия истекла. Необходимо войти заново.');
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ API Success: ${url}`, data);
            return data;

        } catch (error) {
            console.error(`❌ API Error: ${url}`, error);
            throw error;
        }
    }

    /**
     * Обработка неавторизованного доступа
     */
    static handleUnauthorized() {
        console.warn('🔐 Обнаружена ошибка авторизации - очищаем данные');

        // Очищаем все данные авторизации
        const keys = ['authToken', 'tokenType', 'user', 'rememberMe'];
        keys.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });

        // Очищаем cookie
        document.cookie = 'authToken=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';

        // Перенаправляем на логин через небольшую задержку
        setTimeout(() => {
            window.location.href = '/login?error=session_expired';
        }, 1500);
    }

    /**
     * Получение данных из формы
     * @param {HTMLFormElement} form - Форма
     * @returns {Object} Данные формы
     */
    static getFormData(form) {
        const formData = new FormData(form);
        const data = {};

        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }

        return data;
    }

    /**
     * Валидация email
     * @param {string} email - Email для проверки
     * @returns {boolean} Результат валидации
     */
    static isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    /**
     * Валидация телефона
     * @param {string} phone - Телефон для проверки
     * @returns {boolean} Результат валидации
     */
    static isValidPhone(phone) {
        const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
        return phoneRegex.test(phone.replace(/[\s\-\(\)]/g, ''));
    }

    /**
     * Форматирование координат
     * @param {number} lat - Широта
     * @param {number} lon - Долгота
     * @param {number} precision - Точность (знаков после запятой)
     * @returns {string} Отформатированные координаты
     */
    static formatCoordinates(lat, lon, precision = 6) {
        return `${lat.toFixed(precision)}, ${lon.toFixed(precision)}`;
    }

    /**
     * Добавление класса loading к кнопке
     * @param {HTMLElement} button - Кнопка
     * @param {string} loadingText - Текст загрузки
     */
    static setButtonLoading(button, loadingText = 'Загрузка...') {
        if (button.dataset.originalContent) return; // Уже в состоянии загрузки

        button.dataset.originalContent = button.innerHTML;
        button.disabled = true;
        button.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${loadingText}`;
    }

    /**
     * Снятие состояния loading с кнопки
     * @param {HTMLElement} button - Кнопка
     */
    static removeButtonLoading(button) {
        if (!button.dataset.originalContent) return;

        button.disabled = false;
        button.innerHTML = button.dataset.originalContent;
        delete button.dataset.originalContent;
    }

    /**
     * Дебаунс функция
     * @param {Function} func - Функция для дебаунса
     * @param {number} wait - Время ожидания в мс
     * @returns {Function} Дебаунсированная функция
     */
    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    /**
     * Проверка поддержки геолокации
     * @returns {boolean} Поддерживается ли геолокация
     */
    static isGeolocationSupported() {
        return 'geolocation' in navigator;
    }

    /**
     * Логирование действий пользователя
     * @param {string} action - Действие
     * @param {Object} data - Дополнительные данные
     */
    static logUserAction(action, data = {}) {
        console.log(`👤 User Action: ${action}`, {
            timestamp: new Date().toISOString(),
            url: window.location.pathname,
            ...data
        });
    }
}