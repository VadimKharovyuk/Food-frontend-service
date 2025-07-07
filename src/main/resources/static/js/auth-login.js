class AuthLogin {
    constructor(config = {}) {
        this.API_BASE_URL = config.apiBaseUrl || 'http://localhost:8082';
        this.roleRedirects = config.roleRedirects || {
            'ROLE_ADMIN': '/dashboard',
            'ROLE_USER': '/dashboard',
            'ROLE_BUSINESS': '/dashboard',
            'ROLE_COURIER': '/dashboard',
            'BASE_USER': '/dashboard',
            'BUSINESS_USER': '/dashboard',
            'COURIER': '/dashboard',
            'ADMIN': '/dashboard'
        };
        this.init();
    }

    init() {
        this.bindEvents();
        this.checkExistingAuth();
        this.addInputAnimations();
    }

    bindEvents() {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }
        window.togglePassword = () => this.togglePassword();
    }

    togglePassword() {
        const passwordInput = document.getElementById('password');
        const toggleBtn = document.querySelector('.password-toggle');

        if (!passwordInput || !toggleBtn) return;

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            toggleBtn.textContent = '🙈';
        } else {
            passwordInput.type = 'password';
            toggleBtn.textContent = '👁️';
        }
    }

    showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        const successDiv = document.getElementById('successMessage');

        if (!errorDiv) return;

        if (successDiv) successDiv.style.display = 'none';
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';

        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }

    showSuccess(message) {
        const errorDiv = document.getElementById('errorMessage');
        const successDiv = document.getElementById('successMessage');

        if (!successDiv) return;

        if (errorDiv) errorDiv.style.display = 'none';
        successDiv.textContent = message;
        successDiv.style.display = 'block';
    }

    setLoading(loading) {
        const loginBtn = document.getElementById('loginBtn');
        const loadingSpinner = document.getElementById('loadingSpinner');
        const btnText = document.getElementById('btnText');

        if (!loginBtn || !loadingSpinner || !btnText) return;

        if (loading) {
            loginBtn.disabled = true;
            loadingSpinner.style.display = 'inline-block';
            btnText.textContent = 'Вход...';
        } else {
            loginBtn.disabled = false;
            loadingSpinner.style.display = 'none';
            btnText.textContent = '🚀 Войти в систему';
        }
    }

    validateForm(email, password) {
        if (!email || !password) {
            this.showError('Пожалуйста, заполните все поля');
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            this.showError('Введите корректный email адрес');
            return false;
        }

        if (password.length < 3) {
            this.showError('Пароль слишком короткий');
            return false;
        }

        return true;
    }

    // ✅ ИСПРАВЛЕННАЯ обработка входа для нового API
    async handleLogin(e) {
        e.preventDefault();

        // 🔍 ОТЛАДКА - проверяем текущий URL
        console.log('🚀 НАЧАЛО handleLogin - текущий URL:', window.location.href);

        const email = document.getElementById('email')?.value?.trim();
        const password = document.getElementById('password')?.value;
        const rememberMe = document.getElementById('rememberMe')?.checked || false;

        console.log(`🔐 Попытка авторизации: ${email} (Remember Me: ${rememberMe})`);

        if (!this.validateForm(email, password)) {
            return;
        }

        this.setLoading(true);

        try {
            // ✅ Запрос к новому API
            const response = await fetch(`${this.API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                    rememberMe: rememberMe
                })
            });

            console.log(`📡 Ответ статус: ${response.status}`);

            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('Неверный email или пароль');
                } else if (response.status === 400) {
                    throw new Error('Ошибка в данных запроса');
                } else {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
            }

            const data = await response.json();
            console.log('📥 Ответ сервера:', data);

            // ✅ Новый API возвращает AuthResponseDto напрямую
            if (data.token && data.user) {
                const sessionType = data.rememberMe ? 'Сессия на 30 дней! 💾' : 'Обычная сессия 🔒';
                this.showSuccess(`Добро пожаловать, ${data.user.firstName}! ${sessionType}`);

                // ✅ Сохраняем токен в localStorage/sessionStorage
                const storage = data.rememberMe ? localStorage : sessionStorage;

                storage.setItem('authToken', data.token);
                storage.setItem('tokenType', data.type || 'Bearer');
                storage.setItem('user', JSON.stringify(data.user));
                storage.setItem('rememberMe', data.rememberMe.toString());

                // Сохраняем последний выбор Remember Me
                localStorage.setItem('lastRememberMe', rememberMe.toString());

                console.log(`💾 Данные авторизации сохранены в ${data.rememberMe ? 'localStorage' : 'sessionStorage'}`);
                console.log(`🔑 Токен: ${data.token.substring(0, 20)}... (${data.token.length} символов)`);

                // ✅ Определяем редирект - всегда на единый дашборд
                let redirectUrl = '/dashboard';

                // 🔄 Проверяем, если backend вернул редирект с ролью - исправляем его
                if (window.location.pathname.includes('/dashboard/')) {
                    window.history.replaceState(null, '', '/dashboard');
                }

                console.log(`🎯 Запланирован редирект: ${redirectUrl}`);

                // ✅ Небольшая задержка для показа сообщения
                setTimeout(() => {
                    window.location.href = redirectUrl;
                }, 1500);

            } else {
                throw new Error('Неполные данные в ответе сервера');
            }

        } catch (error) {
            console.error('❌ Ошибка авторизации:', error);

            if (error.message.includes('Failed to fetch')) {
                this.showError('Сервер недоступен. Проверьте подключение.');
            } else {
                this.showError(error.message);
            }
        } finally {
            this.setLoading(false);
        }
    }

    // ✅ ОБНОВЛЕННАЯ проверка существующей авторизации
    checkExistingAuth() {
        try {
            const token = this.getAuthToken();
            const userData = this.getUserData();

            if (token && userData) {
                console.log(`🔑 Найден токен авторизации (${token.substring(0, 20)}...)`);
                console.log(`👤 Найдены данные пользователя: ${userData.email} (${userData.userRole})`);

                this.showSuccess(`Добро пожаловать обратно, ${userData.firstName || userData.email}!`);
            } else {
                console.log('❌ Токен авторизации не найден');
            }

            // Восстанавливаем состояние checkbox Remember Me
            const rememberMeCheckbox = document.getElementById('rememberMe');
            if (rememberMeCheckbox) {
                const lastRememberMe = localStorage.getItem('lastRememberMe');
                if (lastRememberMe === 'true') {
                    rememberMeCheckbox.checked = true;
                    console.log('✅ Восстановлено состояние Remember Me');
                }
            }
        } catch (error) {
            console.error('❌ Ошибка проверки авторизации:', error);
        }
    }

    // ✅ ОБНОВЛЕННАЯ очистка данных
    clearAuthData() {
        try {
            // Очищаем localStorage
            const localStorageKeys = ['authToken', 'tokenType', 'user', 'rememberMe', 'lastRememberMe'];
            localStorageKeys.forEach(key => localStorage.removeItem(key));

            // Очищаем sessionStorage
            const sessionStorageKeys = ['authToken', 'tokenType', 'user', 'rememberMe'];
            sessionStorageKeys.forEach(key => sessionStorage.removeItem(key));

            console.log('🧹 Данные авторизации очищены');
        } catch (error) {
            console.error('❌ Ошибка очистки данных:', error);
        }
    }

    addInputAnimations() {
        document.querySelectorAll('input').forEach(input => {
            input.addEventListener('focus', function() {
                const formGroup = this.closest('.form-group');
                if (formGroup) {
                    formGroup.style.transform = 'scale(1.02)';
                    formGroup.style.transition = 'transform 0.2s ease';
                }
            });

            input.addEventListener('blur', function() {
                const formGroup = this.closest('.form-group');
                if (formGroup) {
                    formGroup.style.transform = 'scale(1)';
                }
            });
        });
    }

    // ✅ ОБНОВЛЕННОЕ получение токена из storage
    getAuthToken() {
        try {
            // Проверяем localStorage сначала (для Remember Me)
            let token = localStorage.getItem('authToken');
            if (token) return token;

            // Затем sessionStorage
            token = sessionStorage.getItem('authToken');
            return token;
        } catch (error) {
            console.error('❌ Ошибка получения токена:', error);
            return null;
        }
    }

    getTokenType() {
        try {
            return localStorage.getItem('tokenType') ||
                sessionStorage.getItem('tokenType') ||
                'Bearer';
        } catch (error) {
            console.error('❌ Ошибка получения типа токена:', error);
            return 'Bearer';
        }
    }

    getAuthHeader() {
        const token = this.getAuthToken();
        const tokenType = this.getTokenType();
        return token ? `${tokenType} ${token}` : null;
    }

    getUserData() {
        try {
            // Проверяем localStorage сначала (для Remember Me)
            let userData = localStorage.getItem('user');
            if (userData) return JSON.parse(userData);

            // Затем sessionStorage
            userData = sessionStorage.getItem('user');
            return userData ? JSON.parse(userData) : null;
        } catch (error) {
            console.error('❌ Ошибка получения данных пользователя:', error);
            return null;
        }
    }

    isRememberMe() {
        return localStorage.getItem('rememberMe') === 'true';
    }

    isAuthenticated() {
        const token = this.getAuthToken();
        const userData = this.getUserData();
        return !!(token && userData);
    }

    // ✅ ОБНОВЛЕННЫЙ logout
    async logout() {
        try {
            console.log('🚪 Выполняется выход из системы...');

            // Можно добавить вызов API logout, если он будет нужен
            const authHeader = this.getAuthHeader();
            if (authHeader) {
                const response = await fetch(`${this.API_BASE_URL}/api/auth/logout`, {
                    method: 'POST',
                    headers: {
                        'Authorization': authHeader,
                        'Content-Type': 'application/json'
                    }
                });
            }

            console.log('✅ Logout выполнен');
        } catch (error) {
            console.error('❌ Ошибка при logout:', error);
        } finally {
            // Очищаем локальные данные в любом случае
            this.clearAuthData();

            // Перенаправляем на страницу логина
            window.location.href = '/login';
        }
    }

    // ✅ ОБНОВЛЕННАЯ отладочная информация
    showDebugInfo() {
        const token = this.getAuthToken();
        const userData = this.getUserData();

        const debugInfo = {
            // Состояние авторизации
            authentication: {
                hasToken: !!token,
                tokenLength: token ? token.length : 0,
                tokenPreview: token ? `${token.substring(0, 30)}...` : null,
                tokenType: this.getTokenType(),
                authHeader: this.getAuthHeader() ? `${this.getTokenType()} ${token?.substring(0, 20)}...` : null,
                isAuthenticated: this.isAuthenticated(),
                userData: userData
            },

            // Состояние Remember Me
            rememberMe: {
                current: this.isRememberMe(),
                last: localStorage.getItem('lastRememberMe')
            },

            // Хранилище
            storage: {
                localStorage: {
                    authToken: !!localStorage.getItem('authToken'),
                    tokenType: localStorage.getItem('tokenType'),
                    user: !!localStorage.getItem('user'),
                    rememberMe: localStorage.getItem('rememberMe'),
                    lastRememberMe: localStorage.getItem('lastRememberMe')
                },
                sessionStorage: {
                    authToken: !!sessionStorage.getItem('authToken'),
                    tokenType: sessionStorage.getItem('tokenType'),
                    user: !!sessionStorage.getItem('user'),
                    rememberMe: sessionStorage.getItem('rememberMe')
                }
            },

            // Конфигурация
            config: {
                apiBaseUrl: this.API_BASE_URL,
                roleRedirects: this.roleRedirects
            }
        };

        console.log('🔍 AuthLogin Debug Info:', debugInfo);
        return debugInfo;
    }

    // ✅ Проверка состояния авторизации
    checkAuthStatus() {
        const token = this.getAuthToken();
        const userData = this.getUserData();

        const status = {
            authenticated: this.isAuthenticated(),
            user: userData,
            hasToken: !!token,
            tokenLength: token ? token.length : 0,
            tokenType: this.getTokenType(),
            authHeader: this.getAuthHeader(),
            rememberMe: this.isRememberMe()
        };

        console.log('🔍 Статус авторизации:', status);
        return status;
    }

    // ✅ Проверка работоспособности API
    async testConnection() {
        try {
            console.log('🔧 Тестирование подключения к API...');

            // Тестируем основной endpoint логина
            const response = await fetch(`${this.API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: 'test@example.com',
                    password: 'test'
                })
            });

            console.log(`📡 API ответил со статусом: ${response.status}`);
            return response.status !== 404; // API доступен, даже если логин неверный
        } catch (error) {
            console.error('❌ Ошибка тестирования API:', error);
            return false;
        }
    }

    // ✅ Валидация токена через API
    async validateToken() {
        const authHeader = this.getAuthHeader();
        if (!authHeader) {
            console.log('❌ Токен отсутствует');
            return false;
        }

        try {
            console.log('🔍 Валидация токена через API...');

            const response = await fetch(`${this.API_BASE_URL}/api/auth/validate-token-simple`, {
                method: 'POST',
                headers: {
                    'Authorization': authHeader,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const isValid = await response.json();
                console.log(`✅ Токен ${isValid ? 'действителен' : 'недействителен'}`);
                return isValid;
            } else {
                console.log('❌ Ошибка валидации токена:', response.status);
                return false;
            }
        } catch (error) {
            console.error('❌ Ошибка валидации токена:', error);
            return false;
        }
    }

    // ✅ Принудительная очистка всех данных
    forceReset() {
        console.log('🔄 Принудительная очистка всех данных авторизации...');

        // Очищаем все возможные ключи
        const keysToRemove = [
            'authToken', 'tokenType', 'user', 'rememberMe', 'lastRememberMe',
            // Старые ключи для совместимости
            'jwt', 'userRole', 'userEmail'
        ];

        keysToRemove.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });

        console.log('✅ Принудительная очистка завершена');

        // Перезагружаем страницу
        setTimeout(() => {
            window.location.reload();
        }, 500);
    }
}

// ✅ Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('loginForm')) {
        window.authLogin = new AuthLogin({
            apiBaseUrl: 'http://localhost:8082',
            roleRedirects: {
                'ROLE_ADMIN': '/dashboard',
                'ROLE_USER': '/dashboard',
                'ROLE_BUSINESS': '/dashboard',
                'ROLE_COURIER': '/dashboard',
                'BASE_USER': '/dashboard',
                'BUSINESS_USER': '/dashboard',
                'COURIER': '/dashboard',
                'ADMIN': '/dashboard'
            }
        });

        // Глобальные функции для отладки
        window.showAuthDebug = () => window.authLogin.showDebugInfo();
        window.checkAuth = () => window.authLogin.checkAuthStatus();
        window.testAuthAPI = () => window.authLogin.testConnection();
        window.validateAuthToken = () => window.authLogin.validateToken();
        window.resetAuth = () => window.authLogin.forceReset();

        console.log('🎯 AuthLogin инициализирован. Доступные команды:');
        console.log('  showAuthDebug() - полная отладочная информация');
        console.log('  checkAuth() - проверка статуса авторизации');
        console.log('  testAuthAPI() - тест подключения к API');
        console.log('  validateAuthToken() - валидация токена через API');
        console.log('  resetAuth() - принудительная очистка данных');
    }
});

// Экспорт для использования в других модулях
if (typeof window !== 'undefined') {
    window.AuthLogin = AuthLogin;
}