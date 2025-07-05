class AuthLogin {
    constructor(config = {}) {
        this.API_BASE_URL = config.apiBaseUrl || 'http://localhost:8082';
        this.roleRedirects = config.roleRedirects || {
            'ROLE_ADMIN': '/admin/dashboard',
            'ROLE_MANAGER': '/manager/dashboard',
            'ROLE_USER': '/dashboard',
            'BASE_USER': '/dashboard'
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

    // ✅ ИСПРАВЛЕННАЯ обработка входа с правильным URL
    async handleLogin(e) {
        e.preventDefault();

        const email = document.getElementById('email')?.value?.trim();
        const password = document.getElementById('password')?.value;
        const rememberMe = document.getElementById('rememberMe')?.checked || false;

        console.log(`🔐 Попытка авторизации: ${email} (Remember Me: ${rememberMe})`);

        if (!this.validateForm(email, password)) {
            return;
        }

        this.setLoading(true);

        try {
            // ✅ ПРАВИЛЬНЫЙ URL для AuthController
            const response = await fetch(`${this.API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'include', // ✅ ВАЖНО: включаем cookies
                body: JSON.stringify({
                    email: email,
                    password: password,
                    rememberMe: rememberMe
                })
            });

            console.log(`📡 Ответ статус: ${response.status}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            console.log('📥 Ответ сервера:', data);

            if (data.success) {
                const sessionType = data.rememberMe ? 'Сессия на 30 дней! 🍪' : 'Обычная сессия 🔒';
                this.showSuccess(`Добро пожаловать! ${sessionType}`);

                // ✅ Токен сохранен в cookies сервером
                console.log('🍪 JWT токен установлен в cookies сервером');

                // ✅ Сохраняем пользовательские данные локально
                if (data.user) {
                    const storage = data.rememberMe ? localStorage : sessionStorage;
                    storage.setItem('user', JSON.stringify(data.user));
                    localStorage.setItem('rememberMe', data.rememberMe.toString());
                    localStorage.setItem('lastRememberMe', rememberMe.toString());

                    console.log(`💾 Данные пользователя сохранены в ${data.rememberMe ? 'localStorage' : 'sessionStorage'}`);
                }

                // ✅ Используем redirectUrl из ответа или определяем по роли
                const redirectUrl = data.redirectUrl ||
                    this.roleRedirects[data.user?.userRole] ||
                    '/dashboard';

                console.log(`🎯 Запланирован редирект: ${redirectUrl}`);

                // ✅ Проверяем что cookies действительно установлены
                setTimeout(() => {
                    const jwtCookie = this.getAuthToken();
                    if (jwtCookie) {
                        console.log(`✅ JWT cookie подтвержден (${jwtCookie.length} символов)`);
                        window.location.href = redirectUrl;
                    } else {
                        console.error('❌ JWT cookie не установлен, повторяем попытку...');
                        // Резервный редирект с токеном в параметрах
                        const urlWithToken = new URL(redirectUrl, window.location.origin);
                        urlWithToken.searchParams.set('token', data.token);
                        window.location.href = urlWithToken.toString();
                    }
                }, 1500);

            } else {
                this.showError(data.message || 'Ошибка авторизации');
            }

        } catch (error) {
            console.error('❌ Ошибка авторизации:', error);

            if (error.message.includes('Failed to fetch')) {
                this.showError('Сервер недоступен. Проверьте подключение.');
            } else if (error.message.includes('HTTP 401')) {
                this.showError('Неверные учетные данные');
            } else if (error.message.includes('HTTP 400')) {
                this.showError('Ошибка в данных запроса');
            } else {
                this.showError('Ошибка соединения с сервером');
            }
        } finally {
            this.setLoading(false);
        }
    }

    // ✅ УЛУЧШЕННАЯ проверка существующей авторизации
    checkExistingAuth() {
        try {
            const hasJwtCookie = this.getAuthToken();
            const userData = this.getUserData();

            if (hasJwtCookie) {
                console.log('🍪 Найден JWT cookie');

                if (userData) {
                    console.log(`👤 Найдены данные пользователя: ${userData.email} (${userData.userRole})`);

                    // Можем показать информацию о существующей сессии
                    this.showSuccess(`Добро пожаловать обратно, ${userData.email}!`);
                }
            } else {
                console.log('❌ JWT cookie не найден');
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

    // ✅ УЛУЧШЕННАЯ очистка данных
    clearAuthData() {
        try {
            // Очищаем localStorage
            localStorage.removeItem('user');
            localStorage.removeItem('rememberMe');
            localStorage.removeItem('lastRememberMe');

            // Очищаем sessionStorage
            sessionStorage.removeItem('user');

            console.log('🧹 Локальные данные авторизации очищены');
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

    // ✅ УЛУЧШЕННОЕ получение токена из cookies
    getAuthToken() {
        try {
            const cookies = document.cookie.split(';');
            const jwtCookie = cookies.find(cookie =>
                cookie.trim().startsWith('jwt=')
            );

            if (jwtCookie) {
                const token = jwtCookie.split('=')[1].trim();
                return token.length > 0 ? token : null;
            }

            return null;
        } catch (error) {
            console.error('❌ Ошибка извлечения токена из cookies:', error);
            return null;
        }
    }

    getUserData() {
        try {
            const userData = localStorage.getItem('user') || sessionStorage.getItem('user');
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

    // ✅ ИСПРАВЛЕННЫЙ logout с правильным URL
    async logout() {
        try {
            console.log('🚪 Выполняется выход из системы...');

            // Отправляем запрос на правильный endpoint
            const response = await fetch(`${this.API_BASE_URL}/api/auth/logout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                console.log('✅ Logout успешен:', data.message);
            } else {
                console.warn('⚠️ Ошибка logout на сервере:', response.status);
            }
        } catch (error) {
            console.error('❌ Ошибка при logout:', error);
        } finally {
            // Очищаем локальные данные в любом случае
            this.clearAuthData();

            // Перенаправляем на страницу логина
            window.location.href = '/login';
        }
    }

    // ✅ РАСШИРЕННАЯ отладочная информация
    showDebugInfo() {
        const token = this.getAuthToken();
        const userData = this.getUserData();

        const debugInfo = {
            // Состояние авторизации
            authentication: {
                hasJwtCookie: !!token,
                tokenLength: token ? token.length : 0,
                tokenPreview: token ? `${token.substring(0, 20)}...` : null,
                isAuthenticated: this.isAuthenticated(),
                userData: userData
            },

            // Состояние Remember Me
            rememberMe: {
                current: this.isRememberMe(),
                last: localStorage.getItem('lastRememberMe')
            },

            // Cookies
            cookies: {
                all: document.cookie,
                jwt: this.getAuthToken() ? 'PRESENT' : 'MISSING',
                userRole: this.getCookie('userRole'),
                userEmail: this.getCookie('userEmail')
            },

            // Локальное хранилище
            storage: {
                localStorage: {
                    user: !!localStorage.getItem('user'),
                    rememberMe: localStorage.getItem('rememberMe'),
                    lastRememberMe: localStorage.getItem('lastRememberMe')
                },
                sessionStorage: {
                    user: !!sessionStorage.getItem('user')
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
            rememberMe: this.isRememberMe()
        };

        console.log('🔍 Статус авторизации:', status);
        return status;
    }

    // ✅ Вспомогательный метод для получения конкретного cookie
    getCookie(name) {
        try {
            const cookies = document.cookie.split(';');
            const cookie = cookies.find(c => c.trim().startsWith(`${name}=`));
            return cookie ? cookie.split('=')[1].trim() : null;
        } catch (error) {
            console.error(`❌ Ошибка получения cookie '${name}':`, error);
            return null;
        }
    }

    // ✅ Проверка работоспособности API
    async testConnection() {
        try {
            console.log('🔧 Тестирование подключения к API...');

            const response = await fetch(`${this.API_BASE_URL}/api/auth/login/debug`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    test: true,
                    timestamp: new Date().toISOString(),
                    userAgent: navigator.userAgent
                })
            });

            if (response.ok) {
                const data = await response.json();
                console.log('✅ API доступен:', data);
                return true;
            } else {
                console.error('❌ API недоступен:', response.status);
                return false;
            }
        } catch (error) {
            console.error('❌ Ошибка тестирования API:', error);
            return false;
        }
    }

    // ✅ Принудительная очистка всех данных
    forceReset() {
        console.log('🔄 Принудительная очистка всех данных авторизации...');

        // Очищаем все возможные ключи
        const keysToRemove = ['user', 'rememberMe', 'lastRememberMe', 'authToken', 'tokenType'];

        keysToRemove.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });

        // Очищаем cookies через JavaScript (ограниченно)
        document.cookie = 'jwt=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
        document.cookie = 'userRole=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
        document.cookie = 'userEmail=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';

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
                'ROLE_ADMIN': '/admin/dashboard',
                'ROLE_MANAGER': '/manager/dashboard',
                'ROLE_USER': '/dashboard',
                'BASE_USER': '/dashboard'
            }
        });

        // Глобальные функции для отладки
        window.showAuthDebug = () => window.authLogin.showDebugInfo();
        window.checkAuth = () => window.authLogin.checkAuthStatus();
        window.testAuthAPI = () => window.authLogin.testConnection();
        window.resetAuth = () => window.authLogin.forceReset();

        console.log('🎯 AuthLogin инициализирован. Доступные команды:');
        console.log('  showAuthDebug() - полная отладочная информация');
        console.log('  checkAuth() - проверка статуса авторизации');
        console.log('  testAuthAPI() - тест подключения к API');
        console.log('  resetAuth() - принудительная очистка данных');
    }
});

// Экспорт для использования в других модулях
if (typeof window !== 'undefined') {
    window.AuthLogin = AuthLogin;
}