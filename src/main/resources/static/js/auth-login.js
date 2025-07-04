// class AuthLogin {
//     constructor(config = {}) {
//         this.API_BASE_URL = config.apiBaseUrl || 'http://localhost:8082';
//         this.redirectUrl = config.redirectUrl || '/dashboard';
//         this.init();
//     }
//
//     init() {
//         this.bindEvents();
//         this.checkExistingAuth();
//         this.addInputAnimations();
//         this.setupGlobalHeaders(); // Настраиваем глобальные заголовки
//     }
//
//     // Привязка событий
//     bindEvents() {
//         const loginForm = document.getElementById('loginForm');
//         if (loginForm) {
//             loginForm.addEventListener('submit', (e) => this.handleLogin(e));
//         }
//
//         // Привязываем кнопку показа пароля глобально
//         window.togglePassword = () => this.togglePassword();
//     }
//
//     // Переключение видимости пароля
//     togglePassword() {
//         const passwordInput = document.getElementById('password');
//         const toggleBtn = document.querySelector('.password-toggle');
//
//         if (!passwordInput || !toggleBtn) return;
//
//         if (passwordInput.type === 'password') {
//             passwordInput.type = 'text';
//             toggleBtn.textContent = '🙈';
//         } else {
//             passwordInput.type = 'password';
//             toggleBtn.textContent = '👁️';
//         }
//     }
//
//     // Показ сообщения об ошибке
//     showError(message) {
//         const errorDiv = document.getElementById('errorMessage');
//         const successDiv = document.getElementById('successMessage');
//
//         if (!errorDiv) return;
//
//         if (successDiv) successDiv.style.display = 'none';
//         errorDiv.textContent = message;
//         errorDiv.style.display = 'block';
//
//         // Автоскрытие через 5 секунд
//         setTimeout(() => {
//             errorDiv.style.display = 'none';
//         }, 5000);
//     }
//
//     // Показ сообщения об успехе
//     showSuccess(message) {
//         const errorDiv = document.getElementById('errorMessage');
//         const successDiv = document.getElementById('successMessage');
//
//         if (!successDiv) return;
//
//         if (errorDiv) errorDiv.style.display = 'none';
//         successDiv.textContent = message;
//         successDiv.style.display = 'block';
//     }
//
//     // Управление состоянием загрузки
//     setLoading(loading) {
//         const loginBtn = document.getElementById('loginBtn');
//         const loadingSpinner = document.getElementById('loadingSpinner');
//         const btnText = document.getElementById('btnText');
//
//         if (!loginBtn || !loadingSpinner || !btnText) return;
//
//         if (loading) {
//             loginBtn.disabled = true;
//             loadingSpinner.style.display = 'inline-block';
//             btnText.textContent = 'Вход...';
//         } else {
//             loginBtn.disabled = false;
//             loadingSpinner.style.display = 'none';
//             btnText.textContent = 'Войти';
//         }
//     }
//
//     // Валидация формы
//     validateForm(email, password) {
//         if (!email || !password) {
//             this.showError('Пожалуйста, заполните все поля');
//             return false;
//         }
//
//         const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
//         if (!emailRegex.test(email)) {
//             this.showError('Введите корректный email адрес');
//             return false;
//         }
//
//         if (password.length < 3) {
//             this.showError('Пароль слишком короткий');
//             return false;
//         }
//
//         return true;
//     }
//
//     // Обработка отправки формы входа
//     async handleLogin(e) {
//         e.preventDefault();
//
//         const email = document.getElementById('email')?.value?.trim();
//         const password = document.getElementById('password')?.value;
//
//         if (!this.validateForm(email, password)) {
//             return;
//         }
//
//         this.setLoading(true);
//
//         try {
//             const response = await fetch(`${this.API_BASE_URL}/api/auth/login`, {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json',
//                 },
//                 body: JSON.stringify({
//                     email: email,
//                     password: password
//                 })
//             });
//
//             const data = await response.json();
//
//             if (response.ok && data.success) {
//                 this.showSuccess(data.message || 'Успешный вход!');
//
//                 // Сохраняем данные авторизации
//                 this.saveAuthData(data);
//
//                 // ИСПРАВЛЕНО: Используем fetch для отправки токена в заголовках
//                 setTimeout(async () => {
//                     await this.secureDashboardRedirect();
//                 }, 1000);
//
//             } else {
//                 this.showError(data.message || 'Ошибка авторизации');
//             }
//
//         } catch (error) {
//             console.error('Ошибка авторизации:', error);
//             this.showError('Ошибка соединения с сервером');
//         } finally {
//             this.setLoading(false);
//         }
//     }
//
//
//     async secureDashboardRedirect() {
//         const token = localStorage.getItem('authToken');
//
//         if (!token) {
//             window.location.href = '/login?error=no_token';
//             return;
//         }
//
//         try {
//
//             const response = await fetch('/dashboard', {
//                 method: 'POST',
//                 headers: {
//                     'Authorization': `Bearer ${token}`,
//                     'X-Auth-Token': token,
//                     'Content-Type': 'application/json'
//                 },
//                 body: JSON.stringify({
//                     action: 'secure_redirect',
//                     timestamp: new Date().toISOString()
//                 }),
//                 redirect: 'follow'
//             });
//
//             if (response.redirected) {
//
//                 window.location.href = response.url;
//             } else if (response.ok) {
//
//                 const html = await response.text();
//                 if (html.includes('redirect:')) {
//
//                     const redirectMatch = html.match(/redirect:([^\s"']+)/);
//                     if (redirectMatch) {
//                         window.location.href = redirectMatch[1];
//                         return;
//                     }
//                 }
//
//
//                 document.open();
//                 document.write(html);
//                 document.close();
//             } else {
//
//                 console.warn('Secure redirect failed, using fallback');
//                 this.fallbackRedirect();
//             }
//         } catch (error) {
//             console.error('Ошибка secure redirect:', error);
//             this.fallbackRedirect();
//         }
//     }
//
//
//     fallbackRedirect() {
//         const token = localStorage.getItem('authToken');
//         if (token) {
//             window.location.href = `/dashboard?token=${encodeURIComponent(token)}`;
//         } else {
//             window.location.href = '/login?error=no_token';
//         }
//     }
//
//
//     saveAuthData(data) {
//         try {
//             if (data.token) {
//                 localStorage.setItem('authToken', data.token);
//             }
//             if (data.user) {
//                 localStorage.setItem('user', JSON.stringify(data.user));
//             }
//             if (data.type) {
//                 localStorage.setItem('tokenType', data.type);
//             }
//         } catch (error) {
//             console.error('Ошибка сохранения данных авторизации:', error);
//         }
//     }
//
//
//     checkExistingAuth() {
//         try {
//             const token = localStorage.getItem('authToken');
//             if (token) {
//                 console.log('Пользователь уже авторизован');
//
//                 // this.validateToken(token);
//             }
//         } catch (error) {
//             console.error('Ошибка проверки авторизации:', error);
//         }
//     }
//
//
//     async validateToken(token) {
//         try {
//             // ИСПРАВЛЕНО: Используем тот же порт для валидации
//             const response = await fetch(`/api/auth/validate`, {
//                 method: 'GET',
//                 headers: {
//                     'Authorization': `Bearer ${token}`,
//                     'Content-Type': 'application/json',
//                 }
//             });
//
//             if (response.ok) {
//                 // Токен валидный, можно перенаправить
//                 // window.location.href = this.redirectUrl;
//             } else {
//                 // Токен невалидный, удаляем
//                 this.clearAuthData();
//             }
//         } catch (error) {
//             console.error('Ошибка валидации токена:', error);
//         }
//     }
//
//
//
//
//
//     clearAuthData() {
//         try {
//             localStorage.removeItem('authToken');
//             localStorage.removeItem('user');
//             localStorage.removeItem('tokenType');
//         } catch (error) {
//             console.error('Ошибка очистки данных авторизации:', error);
//         }
//     }
//
//
//     addInputAnimations() {
//         document.querySelectorAll('input').forEach(input => {
//             input.addEventListener('focus', function() {
//                 const formGroup = this.closest('.form-group');
//                 if (formGroup) {
//                     formGroup.style.transform = 'scale(1.02)';
//                     formGroup.style.transition = 'transform 0.2s ease';
//                 }
//             });
//
//             input.addEventListener('blur', function() {
//                 const formGroup = this.closest('.form-group');
//                 if (formGroup) {
//                     formGroup.style.transform = 'scale(1)';
//                 }
//             });
//         });
//     }
//
//
//     getAuthToken() {
//         return localStorage.getItem('authToken');
//     }
//
//
//     getUserData() {
//         try {
//             const userData = localStorage.getItem('user');
//             return userData ? JSON.parse(userData) : null;
//         } catch (error) {
//             console.error('Ошибка получения данных пользователя:', error);
//             return null;
//         }
//     }
//
//
//     isAuthenticated() {
//         return !!this.getAuthToken();
//     }
//
//
//     logout() {
//         this.clearAuthData();
//         window.location.href = '/login';
//     }
// }
//
//
// class AuthAPI {
//     constructor(baseUrl = 'http://localhost:8082') {
//         this.baseUrl = baseUrl;
//     }
//
//
//     getAuthHeaders() {
//         const token = localStorage.getItem('authToken');
//         const tokenType = localStorage.getItem('tokenType') || 'Bearer';
//
//         return {
//             'Content-Type': 'application/json',
//             ...(token && { 'Authorization': `${tokenType} ${token}` })
//         };
//     }
//
//
//     async authenticatedRequest(url, options = {}) {
//         const config = {
//             ...options,
//             headers: {
//                 ...this.getAuthHeaders(),
//                 ...options.headers
//             }
//         };
//
//         const response = await fetch(`${this.baseUrl}${url}`, config);
//
//
//         if (response.status === 401) {
//             const authLogin = new AuthLogin();
//             authLogin.clearAuthData();
//             window.location.href = '/login';
//             return null;
//         }
//
//         return response;
//     }
//
//
//     redirectToDashboardWithToken() {
//         const token = localStorage.getItem('authToken');
//         if (token) {
//             // Передаем токен как параметр URL для первичного запроса
//             window.location.href = `/dashboard?token=${encodeURIComponent(token)}`;
//         } else {
//             window.location.href = '/dashboard';
//         }
//     }
//
//     // Альтернативный метод через POST запрос с токеном
//     async redirectToDashboardSecure() {
//         const token = localStorage.getItem('authToken');
//         if (!token) {
//             window.location.href = '/login?error=no_token';
//             return;
//         }
//
//         try {
//
//             const response = await fetch('/dashboard', {
//                 method: 'POST',
//                 headers: {
//                     'Authorization': `Bearer ${token}`,
//                     'Content-Type': 'application/json',
//                     'X-Auth-Token': token
//                 },
//                 body: JSON.stringify({ action: 'dashboard_redirect' })
//             });
//
//             if (response.redirected) {
//                 window.location.href = response.url;
//             } else if (response.ok) {
//                 const result = await response.text();
//                 if (result.includes('redirect:')) {
//                     const redirectUrl = result.replace('redirect:', '');
//                     window.location.href = redirectUrl;
//                 } else {
//                     document.open();
//                     document.write(result);
//                     document.close();
//                 }
//             } else {
//                 window.location.href = '/login?error=dashboard_error';
//             }
//         } catch (error) {
//             console.error('Ошибка перенаправления:', error);
//             // Fallback на обычное перенаправление
//             this.redirectToDashboardWithToken();
//         }
//     }
//
//
//     setupGlobalHeaders() {
//         const token = this.getAuthToken();
//         if (token) {
//
//             const originalFetch = window.fetch;
//             window.fetch = function(url, options = {}) {
//                 const headers = {
//                     'Authorization': `Bearer ${token}`,
//                     'X-Auth-Token': token,
//                     ...options.headers
//                 };
//
//                 return originalFetch(url, {
//                     ...options,
//                     headers
//                 });
//             };
//
//
//             const originalOpen = XMLHttpRequest.prototype.open;
//             XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
//                 originalOpen.call(this, method, url, async, user, password);
//                 this.setRequestHeader('Authorization', `Bearer ${token}`);
//                 this.setRequestHeader('X-Auth-Token', token);
//             };
//         }
//     }
// }
//
//
// document.addEventListener('DOMContentLoaded', function() {
//     // Инициализируем только если мы на странице логина
//     if (document.getElementById('loginForm')) {
//         window.authLogin = new AuthLogin({
//             apiBaseUrl: 'http://localhost:8082',
//             redirectUrl: '/dashboard'
//         });
//     }
// });
//
//
// if (typeof module !== 'undefined' && module.exports) {
//     module.exports = { AuthLogin, AuthAPI };
// }
//
//
// if (typeof window !== 'undefined') {
//     window.AuthLogin = AuthLogin;
//     window.AuthAPI = AuthAPI;
// }


class AuthLogin {
    constructor(config = {}) {
        this.API_BASE_URL = config.apiBaseUrl || 'http://localhost:8082';
        this.redirectUrl = config.redirectUrl || '/dashboard';
        this.init();
    }

    init() {
        this.bindEvents();
        this.checkExistingAuth();
        this.addInputAnimations();
        this.setupGlobalHeaders();
    }

    // Привязка событий
    bindEvents() {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Привязываем кнопку показа пароля глобально
        window.togglePassword = () => this.togglePassword();
    }

    // Переключение видимости пароля
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

    // Показ сообщения об ошибке
    showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        const successDiv = document.getElementById('successMessage');

        if (!errorDiv) return;

        if (successDiv) successDiv.style.display = 'none';
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';

        // Автоскрытие через 5 секунд
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }

    // Показ сообщения об успехе
    showSuccess(message) {
        const errorDiv = document.getElementById('errorMessage');
        const successDiv = document.getElementById('successMessage');

        if (!successDiv) return;

        if (errorDiv) errorDiv.style.display = 'none';
        successDiv.textContent = message;
        successDiv.style.display = 'block';
    }

    // Управление состоянием загрузки
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

    // Валидация формы
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

    // ✅ ИСПРАВЛЕННАЯ обработка отправки формы входа
    async handleLogin(e) {
        e.preventDefault();

        const email = document.getElementById('email')?.value?.trim();
        const password = document.getElementById('password')?.value;
        const rememberMe = document.getElementById('rememberMe')?.checked || false; // ✅ ДОБАВЛЕНО

        console.log(`🔐 Попытка авторизации: ${email} (Remember Me: ${rememberMe})`); // ✅ DEBUG

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
                    rememberMe: rememberMe // ✅ ДОБАВЛЕНО
                })
            });

            const data = await response.json();

            if (response.ok) { // ✅ УБРАЛ && data.success
                // ✅ Показываем сообщение с информацией о сессии
                const sessionType = data.rememberMe ?
                    'Сессия на 7 дней! 🍪' :
                    'Обычная сессия 🔒';

                this.showSuccess(`Добро пожаловать! ${sessionType}`);

                // ✅ Сохраняем данные авторизации с учетом rememberMe
                this.saveAuthData(data);

                // ✅ Сохраняем выбор пользователя для следующего раза
                localStorage.setItem('lastRememberMe', rememberMe.toString());

                setTimeout(async () => {
                    await this.secureDashboardRedirect();
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

    async secureDashboardRedirect() {
        const token = this.getAuthToken(); // ✅ Используем универсальный метод

        if (!token) {
            window.location.href = '/login?error=no_token';
            return;
        }

        try {
            const response = await fetch('/dashboard', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'X-Auth-Token': token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    action: 'secure_redirect',
                    timestamp: new Date().toISOString()
                }),
                redirect: 'follow'
            });

            if (response.redirected) {
                window.location.href = response.url;
            } else if (response.ok) {
                const html = await response.text();
                if (html.includes('redirect:')) {
                    const redirectMatch = html.match(/redirect:([^\s"']+)/);
                    if (redirectMatch) {
                        window.location.href = redirectMatch[1];
                        return;
                    }
                }

                document.open();
                document.write(html);
                document.close();
            } else {
                console.warn('Secure redirect failed, using fallback');
                this.fallbackRedirect();
            }
        } catch (error) {
            console.error('Ошибка secure redirect:', error);
            this.fallbackRedirect();
        }
    }

    fallbackRedirect() {
        const token = this.getAuthToken();
        if (token) {
            window.location.href = `/dashboard?token=${encodeURIComponent(token)}`;
        } else {
            window.location.href = '/login?error=no_token';
        }
    }

    // ✅ ИСПРАВЛЕННОЕ сохранение данных авторизации
    saveAuthData(data) {
        try {
            console.log(`💾 Сохраняем данные авторизации (Remember Me: ${data.rememberMe})`);

            // ✅ Выбираем storage в зависимости от rememberMe
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

            // ✅ Информацию о rememberMe всегда сохраняем в localStorage
            localStorage.setItem('rememberMe', data.rememberMe.toString());

        } catch (error) {
            console.error('❌ Ошибка сохранения данных авторизации:', error);
        }
    }

    // ✅ УЛУЧШЕННАЯ проверка существующей авторизации
    checkExistingAuth() {
        try {
            const token = this.getAuthToken();
            const rememberMeStatus = this.isRememberMe();

            if (token) {
                console.log(`🔍 Найден существующий токен (Remember Me: ${rememberMeStatus})`);

                // ✅ Восстанавливаем состояние checkbox
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

    async validateToken(token) {
        try {
            const response = await fetch(`/api/auth/validate`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                return true;
            } else {
                this.clearAuthData();
                return false;
            }
        } catch (error) {
            console.error('❌ Ошибка валидации токена:', error);
            return false;
        }
    }

    // ✅ УЛУЧШЕННАЯ очистка данных авторизации
    clearAuthData() {
        try {
            // ✅ Очищаем оба storage
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

    // ✅ УНИВЕРСАЛЬНОЕ получение токена
    getAuthToken() {
        return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    }

    // ✅ УНИВЕРСАЛЬНОЕ получение данных пользователя
    getUserData() {
        try {
            const userData = localStorage.getItem('user') || sessionStorage.getItem('user');
            return userData ? JSON.parse(userData) : null;
        } catch (error) {
            console.error('❌ Ошибка получения данных пользователя:', error);
            return null;
        }
    }

    // ✅ ДОБАВЛЕН метод проверки Remember Me
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

    // ✅ ОБНОВЛЕННАЯ настройка глобальных заголовков
    setupGlobalHeaders() {
        const token = this.getAuthToken();
        if (token) {
            const originalFetch = window.fetch;
            window.fetch = function(url, options = {}) {
                const headers = {
                    'Authorization': `Bearer ${token}`,
                    'X-Auth-Token': token,
                    ...options.headers
                };

                return originalFetch(url, {
                    ...options,
                    headers
                });
            };
        }
    }

    // ✅ ДОБАВЛЕНЫ debug методы
    showDebugInfo() {
        console.log('🔍 Debug Info:', {
            hasToken: !!this.getAuthToken(),
            rememberMe: this.isRememberMe(),
            userData: this.getUserData(),
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

// ✅ AuthAPI класс остается без изменений
class AuthAPI {
    constructor(baseUrl = 'http://localhost:8082') {
        this.baseUrl = baseUrl;
    }

    getAuthHeaders() {
        const authLogin = new AuthLogin();
        const token = authLogin.getAuthToken();
        const tokenType = localStorage.getItem('tokenType') || sessionStorage.getItem('tokenType') || 'Bearer';

        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `${tokenType} ${token}` })
        };
    }

    async authenticatedRequest(url, options = {}) {
        const config = {
            ...options,
            headers: {
                ...this.getAuthHeaders(),
                ...options.headers
            }
        };

        const response = await fetch(`${this.baseUrl}${url}`, config);

        if (response.status === 401) {
            const authLogin = new AuthLogin();
            authLogin.clearAuthData();
            window.location.href = '/login';
            return null;
        }

        return response;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('loginForm')) {
        window.authLogin = new AuthLogin({
            apiBaseUrl: 'http://localhost:8082',
            redirectUrl: '/dashboard'
        });

        // ✅ Добавляем debug функции
        window.showAuthDebug = () => window.authLogin.showDebugInfo();

        console.log('🎯 AuthLogin инициализирован. Debug: showAuthDebug()');
    }
});

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { AuthLogin, AuthAPI };
}

if (typeof window !== 'undefined') {
    window.AuthLogin = AuthLogin;
    window.AuthAPI = AuthAPI;
}

