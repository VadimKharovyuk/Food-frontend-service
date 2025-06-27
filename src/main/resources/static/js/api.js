// js/api.js
class ApiClient {
    constructor() {
        this.baseURL = window.location.origin; // Frontend Service URL
        // 🍪 Проверяем токен в обоих хранилищах
        this.token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    }

    // 🔧 Базовый метод для всех запросов
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;

        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        // Добавляем токен если есть
        if (this.token) {
            config.headers['Authorization'] = `Bearer ${this.token}`;
        }

        try {
            console.log(`🔄 API Request: ${config.method || 'GET'} ${url}`);

            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || data.error || `HTTP ${response.status}`);
            }

            console.log('✅ API Response:', data);
            return data;

        } catch (error) {
            console.error('❌ API Error:', error);
            throw error;
        }
    }

    // 📋 Получить доступные роли
    async getRoles() {
        return this.request('/api/registration/roles');
    }

    // ✅ Проверить доступность email
    async checkEmailAvailability(email) {
        return this.request(`/api/registration/check-email?email=${encodeURIComponent(email)}`);
    }

    // 📝 Регистрация пользователя
    async register(userData) {
        return this.request('/api/registration/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    }

    // 🔐 Авторизация пользователя с Remember Me
    async login(credentials) {
        const response = await this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify(credentials)
        });

        if (response.success && response.token) {
            // 🍪 Сохраняем токен и данные пользователя с учетом Remember Me
            this.setToken(response.token, credentials.rememberMe);
            this.setUserData(response.user, credentials.rememberMe);
        }

        return response;
    }

    // 🍪 Установить токен с учетом Remember Me
    setToken(token, rememberMe) {
        this.token = token;
        if (rememberMe) {
            // Сохраняем в localStorage (долгое время)
            localStorage.setItem('authToken', token);
            sessionStorage.removeItem('authToken');
            console.log('🍪 Токен сохранен в localStorage (Remember Me)');
        } else {
            // Сохраняем в sessionStorage (до закрытия браузера)
            sessionStorage.setItem('authToken', token);
            localStorage.removeItem('authToken');
            console.log('🔒 Токен сохранен в sessionStorage (обычная сессия)');
        }
    }

    // 👤 Установить данные пользователя
    setUserData(user, rememberMe) {
        const userData = JSON.stringify(user);
        if (rememberMe) {
            localStorage.setItem('currentUser', userData);
            sessionStorage.removeItem('currentUser');
        } else {
            sessionStorage.setItem('currentUser', userData);
            localStorage.removeItem('currentUser');
        }
    }

    // 📊 Получить данные пользователя
    getUserData() {
        const userData = localStorage.getItem('currentUser') || sessionStorage.getItem('currentUser');
        return userData ? JSON.parse(userData) : null;
    }

    // 🔍 Проверить, установлен ли Remember Me
    isRememberMeActive() {
        return !!localStorage.getItem('authToken');
    }

    // 👤 Получить профиль текущего пользователя
    async getProfile() {
        return this.request('/api/auth/me');
    }

    // ✅ Проверить токен
    async validateToken() {
        return this.request('/api/auth/validate-token', {
            method: 'POST'
        });
    }

    // 🚪 Выход с полной очисткой
    async logout() {
        try {
            await this.request('/api/auth/logout', {
                method: 'POST'
            });
        } catch (error) {
            console.warn('Logout API error:', error);
        } finally {
            this.clearTokens();
        }
    }

    // 🧹 Очистка всех токенов и данных
    clearTokens() {
        this.token = null;
        localStorage.removeItem('authToken');
        sessionStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        sessionStorage.removeItem('currentUser');
        console.log('🧹 Все токены и данные очищены');
    }

    // 🧪 Тестовые методы
    async testRegistration() {
        return this.request('/api/registration/test');
    }

    async testAuth() {
        return this.request('/api/auth/test');
    }
}

// Создаем глобальный экземпляр
const apiClient = new ApiClient();

// 💬 Утилиты для показа сообщений
class MessageManager {
    static show(message, type = 'info', containerId = 'messageContainer') {
        const container = document.getElementById(containerId);
        if (!container) {
            console.warn('Message container not found:', containerId);
            return;
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        messageDiv.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()" style="float: right; background: none; border: none; font-size: 1.2rem; cursor: pointer;">&times;</button>
        `;

        container.appendChild(messageDiv);

        // Автоматически удаляем через 5 секунд
        setTimeout(() => {
            if (messageDiv.parentElement) {
                messageDiv.remove();
            }
        }, 5000);
    }

    static clear(containerId = 'messageContainer') {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = '';
        }
    }

    static success(message, containerId) {
        this.show(message, 'success', containerId);
    }

    static error(message, containerId) {
        this.show(message, 'error', containerId);
    }

    static info(message, containerId) {
        this.show(message, 'info', containerId);
    }
}

// 🔄 Утилиты для UI
class UIUtils {
    static showLoading(buttonId) {
        const button = document.getElementById(buttonId);
        if (button) {
            button.disabled = true;
            button.innerHTML += ' <span class="loading"></span>';
        }
    }

    static hideLoading(buttonId, originalText) {
        const button = document.getElementById(buttonId);
        if (button) {
            button.disabled = false;
            button.innerHTML = originalText;
        }
    }

    static setFieldStatus(fieldId, message, type) {
        const statusElement = document.getElementById(fieldId + 'Status');
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.className = `field-status ${type}`;
        }
    }

    static formatDate(dateString) {
        return new Date(dateString).toLocaleString('ru-RU');
    }
}