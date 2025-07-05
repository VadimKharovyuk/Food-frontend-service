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
        this.setupGlobalHeaders();
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

    // ✅ УЛУЧШЕННАЯ обработка входа с Remember Me и редиректом по ролям
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
            const response = await fetch(`${this.API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                    rememberMe: rememberMe
                })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                const sessionType = data.rememberMe ? 'Сессия на 7 дней! 🍪' : 'Обычная сессия 🔒';
                this.showSuccess(`Добро пожаловать! ${sessionType}`);

                // ✅ Сохраняем данные авторизации
                this.saveAuthData(data);

                // ✅ Сохраняем выбор пользователя для следующего раза
                localStorage.setItem('lastRememberMe', rememberMe.toString());

                // ✅ Получаем роль пользователя и делаем редирект
                const userRole = data.user?.userRole || data.user?.role;
                console.log(`👤 Роль пользователя: ${userRole}`);
                console.log(`🎫 Токен получен: ${data.token ? 'ДА' : 'НЕТ'}`);

                setTimeout(async () => {
                    await this.redirectByRole(userRole);
                }, 1500);

            } else {
                this.showError(data.message || 'Ошибка авторизации');
            }

        } catch (error) {
            console.error('❌ Ошибка авторизации:', error);
            this.showError('Ошибка соединения с сервером');
        } finally {
            this.setLoading(false);
        }
    }

    // ✅ УЛУЧШЕННЫЙ метод редиректа по ролям с передачей токена
    async redirectByRole(userRole) {
        const redirectUrl = this.roleRedirects[userRole] || this.roleRedirects['BASE_USER'];

        console.log(`🎯 Редирект по роли ${userRole} -> ${redirectUrl}`);

        // Показываем пользователю куда перенаправляем
        const roleNames = {
            'ROLE_ADMIN': 'Панель администратора',
            'ROLE_MANAGER': 'Панель менеджера',
            'ROLE_USER': 'Пользовательская панель',
            'BASE_USER': 'Пользовательская панель'
        };

        const roleName = roleNames[userRole] || 'Основная страница';
        this.showSuccess(`Переходим в ${roleName}...`);

        setTimeout(async () => {
            await this.secureDashboardRedirect(redirectUrl);
        }, 800);
    }

    // ✅ БЕЗОПАСНЫЙ редирект на дашборд с токеном
    async secureDashboardRedirect(targetUrl = '/dashboard') {
        const token = this.getAuthToken();

        if (!token) {
            console.error('❌ Токен не найден для редиректа');
            window.location.href = '/login?error=no_token';
            return;
        }

        console.log(`🔐 Выполняем безопасный редирект на ${targetUrl} с токеном`);

        try {
            // Попытка 1: POST запрос с токеном в заголовках
            const response = await fetch(targetUrl, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'X-Auth-Token': token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    action: 'secure_redirect',
                    timestamp: new Date().toISOString(),
                    userAgent: navigator.userAgent
                }),
                redirect: 'follow'
            });

            if (response.redirected) {
                console.log('✅ Сервер выполнил редирект, переходим');
                window.location.href = response.url;
                return;
            }

            if (response.ok) {
                console.log('✅ Получили успешный ответ, обрабатываем');
                const contentType = response.headers.get('content-type');

                if (contentType && contentType.includes('text/html')) {
                    const html = await response.text();

                    // Проверяем на редирект в HTML
                    if (html.includes('redirect:')) {
                        const redirectMatch = html.match(/redirect:([^\s"']+)/);
                        if (redirectMatch) {
                            window.location.href = redirectMatch[1];
                            return;
                        }
                    }

                    // Заменяем содержимое страницы
                    document.open();
                    document.write(html);
                    document.close();

                    // Обновляем URL без перезагрузки
                    history.pushState({}, '', targetUrl);
                    return;
                }
            }

            // Попытка 2: Редирект с токеном в URL параметрах
            console.log('⚠️ POST не сработал, пробуем GET с токеном в параметрах');
            this.fallbackRedirect(targetUrl, token);

        } catch (error) {
            console.error('❌ Ошибка при безопасном редиректе:', error);
            this.fallbackRedirect(targetUrl, token);
        }
    }

    // ✅ РЕЗЕРВНЫЙ метод редиректа
    fallbackRedirect(targetUrl = '/dashboard', token = null) {
        const authToken = token || this.getAuthToken();

        if (authToken) {
            console.log(`🔄 Fallback редирект на ${targetUrl} с токеном в параметрах`);

            // Добавляем токен в URL параметры
            const url = new URL(targetUrl, window.location.origin);
            url.searchParams.set('token', authToken);
            url.searchParams.set('auth', 'true');

            window.location.href = url.toString();
        } else {
            console.error('❌ Токен не найден для fallback редиректа');
            window.location.href = '/login?error=no_token';
        }
    }

    // ✅ ИСПРАВЛЕННОЕ сохранение данных авторизации
    saveAuthData(data) {
        try {
            console.log(`💾 Сохраняем данные авторизации (Remember Me: ${data.rememberMe})`);

            const storage = data.rememberMe ? localStorage : sessionStorage;

            if (data.token) {
                storage.setItem('authToken', data.token);
                console.log(`✅ Токен сохранен в ${data.rememberMe ? 'localStorage' : 'sessionStorage'}`);
            }

            if (data.user) {
                storage.setItem('user', JSON.stringify(data.user));
            }

            if (data.type) {
                storage.setItem('tokenType', data.type);
            }

            // Информацию о rememberMe всегда сохраняем в localStorage
            localStorage.setItem('rememberMe', data.rememberMe.toString());

        } catch (error) {
            console.error('❌ Ошибка сохранения данных авторизации:', error);
        }
    }

    // ✅ УЛУЧШЕННАЯ проверка существующей авторизации
    checkExistingAuth() {
        try {
            const token = this.getAuthToken();

            if (token) {
                const userData = this.getUserData();
                if (userData?.userRole) {
                    console.log(`🔍 Найден существующий токен. Роль: ${userData.userRole}`);
                    // Можно сразу перенаправить авторизованного пользователя
                    // this.redirectByRole(userData.userRole);
                }

                // Восстанавливаем состояние checkbox
                const rememberMeCheckbox = document.getElementById('rememberMe');
                if (rememberMeCheckbox) {
                    const lastRememberMe = localStorage.getItem('lastRememberMe');
                    if (lastRememberMe === 'true') {
                        rememberMeCheckbox.checked = true;
                    }
                }
            }
        } catch (error) {
            console.error('❌ Ошибка проверки авторизации:', error);
        }
    }

    clearAuthData() {
        try {
            localStorage.removeItem('authToken');
            localStorage.removeItem('user');
            localStorage.removeItem('tokenType');
            localStorage.removeItem('rememberMe');

            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('user');
            sessionStorage.removeItem('tokenType');

            console.log('🧹 Данные авторизации очищены');
        } catch (error) {
            console.error('❌ Ошибка очистки данных авторизации:', error);
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

    getAuthToken() {
        return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
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
        return !!this.getAuthToken();
    }

    logout() {
        this.clearAuthData();
        window.location.href = '/login';
    }

    // ✅ УЛУЧШЕННАЯ настройка глобальных заголовков
    setupGlobalHeaders() {
        const token = this.getAuthToken();
        if (token) {
            console.log('🔧 Настраиваем глобальные заголовки с токеном');

            const originalFetch = window.fetch;
            window.fetch = function(url, options = {}) {
                // Добавляем токен во все запросы
                const headers = {
                    'Authorization': `Bearer ${token}`,
                    'X-Auth-Token': token,
                    'X-Requested-With': 'XMLHttpRequest',
                    ...options.headers
                };

                console.log(`📡 Fetch запрос на ${url} с токеном`);

                return originalFetch(url, {
                    ...options,
                    headers
                });
            };

            // Также настраиваем для XMLHttpRequest
            const originalOpen = XMLHttpRequest.prototype.open;
            XMLHttpRequest.prototype.open = function(method, url, ...args) {
                this.addEventListener('loadstart', function() {
                    this.setRequestHeader('Authorization', `Bearer ${token}`);
                    this.setRequestHeader('X-Auth-Token', token);
                    this.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                });
                return originalOpen.apply(this, [method, url, ...args]);
            };
        }
    }

    // ✅ ДОПОЛНИТЕЛЬНЫЙ метод для принудительного редиректа с токеном
    forceRedirectWithToken(targetUrl) {
        const token = this.getAuthToken();
        if (!token) {
            window.location.href = '/login?error=no_token';
            return;
        }

        console.log(`🚀 Принудительный редирект на ${targetUrl} с токеном`);

        // Создаем скрытую форму для POST запроса с токеном
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = targetUrl;
        form.style.display = 'none';

        // Добавляем токен как скрытое поле
        const tokenInput = document.createElement('input');
        tokenInput.type = 'hidden';
        tokenInput.name = 'authToken';
        tokenInput.value = token;
        form.appendChild(tokenInput);

        // Добавляем дополнительные поля
        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'dashboard_access';
        form.appendChild(actionInput);

        document.body.appendChild(form);
        form.submit();
    }

    // ✅ DEBUG методы
    showDebugInfo() {
        console.log('🔍 Debug Info:', {
            hasToken: !!this.getAuthToken(),
            rememberMe: this.isRememberMe(),
            userData: this.getUserData(),
            userRole: this.getUserData()?.userRole,
            redirectUrl: this.roleRedirects[this.getUserData()?.userRole],
            localStorage: {
                authToken: !!localStorage.getItem('authToken'),
                rememberMe: localStorage.getItem('rememberMe'),
                lastRememberMe: localStorage.getItem('lastRememberMe')
            },
            sessionStorage: {
                authToken: !!sessionStorage.getItem('authToken')
            }
        });
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

        window.showAuthDebug = () => window.authLogin.showDebugInfo();
        console.log('🎯 AuthLogin инициализирован. Debug: showAuthDebug()');
    }
});

if (typeof window !== 'undefined') {
    window.AuthLogin = AuthLogin;
}