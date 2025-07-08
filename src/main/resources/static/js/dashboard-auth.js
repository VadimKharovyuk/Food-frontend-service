// //
// // ✅ Упрощенная авторизация для SSR дашборда
// class SimpleDashboardAuth {
//     constructor() {
//         this.API_BASE_URL = 'http://localhost:8082';
//         this.init();
//     }
//
//     init() {
//         console.log('🔒 Инициализация простой авторизации дашборда');
//         this.checkAuthentication();
//     }
//
//     // ✅ Получение токена из storage
//     getAuthToken() {
//         try {
//             let token = localStorage.getItem('authToken');
//             if (token) return token;
//
//             token = sessionStorage.getItem('authToken');
//             return token;
//         } catch (error) {
//             console.error('❌ Ошибка получения токена:', error);
//             return null;
//         }
//     }
//
//     // ✅ Получение данных пользователя из storage
//     getUserData() {
//         try {
//             let userData = localStorage.getItem('user');
//             if (userData) return JSON.parse(userData);
//
//             userData = sessionStorage.getItem('user');
//             return userData ? JSON.parse(userData) : null;
//         } catch (error) {
//             console.error('❌ Ошибка получения данных пользователя:', error);
//             return null;
//         }
//     }
//
//     // ✅ Создание Authorization header
//     getAuthHeader() {
//         const token = this.getAuthToken();
//         const tokenType = localStorage.getItem('tokenType') || sessionStorage.getItem('tokenType') || 'Bearer';
//         return token ? `${tokenType} ${token}` : null;
//     }
//
//     // ✅ Простая проверка авторизации
//     async checkAuthentication() {
//         console.log('🔍 Проверка авторизации...');
//
//         const token = this.getAuthToken();
//         const userData = this.getUserData();
//
//         if (!token || !userData) {
//             console.log('❌ Токен или данные пользователя отсутствуют');
//             this.redirectToLogin();
//             return;
//         }
//
//         console.log(`🔑 Найден токен для пользователя: ${userData.email} (${userData.userRole})`);
//
//         // ✅ Быстрая проверка токена через API
//         try {
//             const authHeader = this.getAuthHeader();
//
//             const response = await fetch(`${this.API_BASE_URL}/api/auth/validate-token`, {
//                 method: 'POST',
//                 headers: {
//                     'Authorization': authHeader,
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 const result = await response.json();
//                 if (result.valid) {
//                     console.log('✅ Токен действителен - показываем дашборд');
//                     this.showDashboard(userData);
//                 } else {
//                     console.log('❌ Токен недействителен');
//                     this.redirectToLogin();
//                 }
//             } else {
//                 console.log('❌ Ошибка проверки токена:', response.status);
//                 this.redirectToLogin();
//             }
//
//         } catch (error) {
//             console.error('❌ Ошибка проверки авторизации:', error);
//             this.redirectToLogin();
//         }
//     }
//
//     // ✅ Показ дашборда на основе роли пользователя
//     showDashboard(userData) {
//         console.log(`👤 Показываем дашборд для роли: ${userData.userRole}`);
//
//         // ✅ Обновляем информацию о пользователе в header
//         this.updateUserInfo(userData);
//
//         // ✅ Показываем контент для роли пользователя
//         this.showRoleContent(userData.userRole);
//
//         console.log('✅ Дашборд успешно отображен');
//     }
//
//     // ✅ Обновление информации о пользователе в header
//     updateUserInfo(userData) {
//         const userNameElement = document.getElementById('user-name');
//         const userRoleElement = document.getElementById('user-role');
//
//         if (userNameElement) {
//             userNameElement.textContent = `${userData.firstName} ${userData.lastName}`;
//         }
//
//         if (userRoleElement) {
//             // Получаем красивое название роли
//             const roleNames = {
//                 'BASE_USER': 'Покупатель',
//                 'BUSINESS_USER': 'Владелец магазина',
//                 'COURIER': 'Курьер',
//                 'ADMIN': 'Администратор'
//             };
//             userRoleElement.textContent = roleNames[userData.userRole] || userData.userRole;
//         }
//     }
//
//     // ✅ Показ контента для роли (ИСПРАВЛЕНО)
//     showRoleContent(userRole) {
//         const container = document.getElementById('dashboard-content');
//
//         if (!container) {
//             console.error('❌ Контейнер дашборда не найден');
//             return;
//         }
//
//         // ✅ Получаем шаблон для роли из скрытого контейнера
//         const templateId = this.getTemplateId(userRole);
//
//         // ✅ ИСПРАВЛЕНИЕ: ищем шаблон внутри dashboard-templates
//         const templatesContainer = document.getElementById('dashboard-templates');
//         if (!templatesContainer) {
//             console.error('❌ Контейнер шаблонов не найден');
//             this.showError('Шаблоны дашборда не загружены');
//             return;
//         }
//
//         const template = templatesContainer.querySelector(`#${templateId}`);
//
//         if (!template) {
//             console.error('❌ Шаблон не найден для роли:', userRole, 'templateId:', templateId);
//             console.log('🔍 Доступные шаблоны:', templatesContainer.children);
//             this.showError('Неподдерживаемая роль пользователя');
//             return;
//         }
//
//         console.log('✅ Найден шаблон:', templateId);
//
//         // ✅ Клонируем и показываем шаблон
//         const content = template.cloneNode(true);
//         content.style.display = 'block';
//         content.id = 'active-dashboard';
//
//         // ✅ Показываем только секции для текущей роли
//         const roleSections = content.querySelectorAll(`[data-role="${userRole}"]`);
//         console.log(`🎯 Найдено секций для роли ${userRole}:`, roleSections.length);
//
//         roleSections.forEach(section => {
//             section.classList.add('visible');
//             section.style.display = 'block';
//         });
//
//         // ✅ Скрываем секции других ролей
//         const allSections = content.querySelectorAll('.role-section');
//         allSections.forEach(section => {
//             if (!section.dataset.role || section.dataset.role !== userRole) {
//                 section.style.display = 'none';
//             }
//         });
//
//         // ✅ Очищаем контейнер и вставляем новый контент
//         container.innerHTML = '';
//         container.appendChild(content);
//
//         console.log(`✅ Показан контент для роли: ${userRole}`);
//     }
//
//     // ✅ Получение ID шаблона для роли (ИСПРАВЛЕНО)
//     getTemplateId(userRole) {
//         const templateMap = {
//             'BASE_USER': 'base-user-content',
//             'BUSINESS_USER': 'business-user-content',
//             'COURIER': 'courier-content',
//             'ADMIN': 'admin-content'
//         };
//
//         return templateMap[userRole] || 'base-user-content';
//     }
//
//     // ✅ Редирект на страницу логина
//     redirectToLogin() {
//         console.log('🔄 Перенаправление на страницу логина');
//         window.location.href = '/login?error=unauthorized';
//     }
//
//     // ✅ Показ ошибки
//     showError(message) {
//         const container = document.getElementById('dashboard-content');
//         if (container) {
//             container.innerHTML = `
//                 <div class="error-message">
//                     <div style="font-size: 64px; margin-bottom: 20px;">😞</div>
//                     <h2>Упс! Что-то пошло не так</h2>
//                     <p>${message}</p>
//                     <div class="error-actions">
//                         <a href="/login" class="btn btn-primary">
//                             <i class="fas fa-home"></i> На главную
//                         </a>
//                         <button class="btn btn-secondary" onclick="window.location.reload()">
//                             <i class="fas fa-redo"></i> Обновить
//                         </button>
//                     </div>
//                 </div>
//             `;
//         }
//     }
//
//     // ✅ Очистка данных авторизации (для logout)
//     clearAuthData() {
//         const keys = ['authToken', 'tokenType', 'user', 'rememberMe', 'lastRememberMe'];
//         keys.forEach(key => {
//             localStorage.removeItem(key);
//             sessionStorage.removeItem(key);
//         });
//         console.log('🧹 Данные авторизации очищены');
//     }
// }
//
// // ✅ Инициализация при загрузке страницы
// document.addEventListener('DOMContentLoaded', function() {
//     window.simpleDashboardAuth = new SimpleDashboardAuth();
// });
//
// // ✅ Глобальная функция для logout (можно вызвать из HTML)
// async function logout() {
//     try {
//         console.log('🚪 Выполняется выход через dashboard API...');
//
//         // Очищаем локальные данные
//         if (window.simpleDashboardAuth) {
//             window.simpleDashboardAuth.clearAuthData();
//         }
//
//         // Вызываем API logout
//         const response = await fetch('/dashboard/logout', {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/json'
//             }
//         });
//
//         if (response.ok) {
//             const result = await response.json();
//             console.log('✅ Logout успешен:', result.message);
//         }
//     } catch (error) {
//         console.error('❌ Ошибка при logout:', error);
//     } finally {
//         // В любом случае редиректим на login
//         window.location.href = '/login';
//     }
// }
//
// // ✅ Совместимость со старыми ссылками (если есть в HTML)
// window.dashboardAuth = {
//     refresh: function() {
//         console.log('🔄 Вызван устаревший метод dashboardAuth.refresh() - используем window.location.reload()');
//         window.location.reload();
//     },
//     logout: logout,
//     checkAuth: function() {
//         if (window.simpleDashboardAuth) {
//             return window.simpleDashboardAuth.checkAuthentication();
//         }
//     }
// };

// ✅ Упрощенная авторизация для SSR дашборда
class SimpleDashboardAuth {
    constructor() {
        this.API_BASE_URL = 'http://localhost:8082';
        this.init();
    }

    init() {
        console.log('🔒 Инициализация простой авторизации дашборда');
        this.checkAuthentication();
    }

    // ✅ Получение токена из storage
    getAuthToken() {
        try {
            let token = localStorage.getItem('authToken');
            if (token) return token;

            token = sessionStorage.getItem('authToken');
            return token;
        } catch (error) {
            console.error('❌ Ошибка получения токена:', error);
            return null;
        }
    }

    // ✅ Получение данных пользователя из storage
    getUserData() {
        try {
            let userData = localStorage.getItem('user');
            if (userData) return JSON.parse(userData);

            userData = sessionStorage.getItem('user');
            return userData ? JSON.parse(userData) : null;
        } catch (error) {
            console.error('❌ Ошибка получения данных пользователя:', error);
            return null;
        }
    }

    // ✅ Создание Authorization header
    getAuthHeader() {
        const token = this.getAuthToken();
        const tokenType = localStorage.getItem('tokenType') || sessionStorage.getItem('tokenType') || 'Bearer';
        return token ? `${tokenType} ${token}` : null;
    }

    // ✅ Простая проверка авторизации
    async checkAuthentication() {
        console.log('🔍 Проверка авторизации...');

        const token = this.getAuthToken();
        const userData = this.getUserData();

        if (!token || !userData) {
            console.log('❌ Токен или данные пользователя отсутствуют');
            this.redirectToLogin();
            return;
        }

        console.log(`🔑 Найден токен для пользователя: ${userData.email} (${userData.userRole})`);

        // ✅ Быстрая проверка токена через API
        try {
            const authHeader = this.getAuthHeader();

            const response = await fetch(`${this.API_BASE_URL}/api/auth/validate-token`, {
                method: 'POST',
                headers: {
                    'Authorization': authHeader,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                if (result.valid) {
                    console.log('✅ Токен действителен - показываем дашборд');

                    // ✅ Сохраняем токен в cookie для совместимости с другими страницами
                    document.cookie = `authToken=${token}; path=/; SameSite=Lax; secure=${location.protocol === 'https:'}`;
                    console.log('🍪 Токен сохранён в cookie для совместимости');

                    this.showDashboard(userData);
                } else {
                    console.log('❌ Токен недействителен');
                    this.redirectToLogin();
                }
            } else {
                console.log('❌ Ошибка проверки токена:', response.status);
                this.redirectToLogin();
            }

        } catch (error) {
            console.error('❌ Ошибка проверки авторизации:', error);
            this.redirectToLogin();
        }
    }

    // ✅ Показ дашборда на основе роли пользователя
    showDashboard(userData) {
        console.log(`👤 Показываем дашборд для роли: ${userData.userRole}`);

        // ✅ Обновляем информацию о пользователе в header
        this.updateUserInfo(userData);

        // ✅ Показываем контент для роли пользователя
        this.showRoleContent(userData.userRole);

        console.log('✅ Дашборд успешно отображен');
    }

    // ✅ Обновление информации о пользователе в header
    updateUserInfo(userData) {
        const userNameElement = document.getElementById('user-name');
        const userRoleElement = document.getElementById('user-role');

        if (userNameElement) {
            userNameElement.textContent = `${userData.firstName} ${userData.lastName}`;
        }

        if (userRoleElement) {
            // Получаем красивое название роли
            const roleNames = {
                'BASE_USER': 'Покупатель',
                'BUSINESS_USER': 'Владелец магазина',
                'COURIER': 'Курьер',
                'ADMIN': 'Администратор'
            };
            userRoleElement.textContent = roleNames[userData.userRole] || userData.userRole;
        }
    }

    // ✅ Показ контента для роли (ИСПРАВЛЕНО)
    showRoleContent(userRole) {
        const container = document.getElementById('dashboard-content');

        if (!container) {
            console.error('❌ Контейнер дашборда не найден');
            return;
        }

        // ✅ Получаем шаблон для роли из скрытого контейнера
        const templateId = this.getTemplateId(userRole);

        // ✅ ИСПРАВЛЕНИЕ: ищем шаблон внутри dashboard-templates
        const templatesContainer = document.getElementById('dashboard-templates');
        if (!templatesContainer) {
            console.error('❌ Контейнер шаблонов не найден');
            this.showError('Шаблоны дашборда не загружены');
            return;
        }

        const template = templatesContainer.querySelector(`#${templateId}`);

        if (!template) {
            console.error('❌ Шаблон не найден для роли:', userRole, 'templateId:', templateId);
            console.log('🔍 Доступные шаблоны:', templatesContainer.children);
            this.showError('Неподдерживаемая роль пользователя');
            return;
        }

        console.log('✅ Найден шаблон:', templateId);

        // ✅ Клонируем и показываем шаблон
        const content = template.cloneNode(true);
        content.style.display = 'block';
        content.id = 'active-dashboard';

        // ✅ Показываем только секции для текущей роли
        const roleSections = content.querySelectorAll(`[data-role="${userRole}"]`);
        console.log(`🎯 Найдено секций для роли ${userRole}:`, roleSections.length);

        roleSections.forEach(section => {
            section.classList.add('visible');
            section.style.display = 'block';
        });

        // ✅ Скрываем секции других ролей
        const allSections = content.querySelectorAll('.role-section');
        allSections.forEach(section => {
            if (!section.dataset.role || section.dataset.role !== userRole) {
                section.style.display = 'none';
            }
        });

        // ✅ Очищаем контейнер и вставляем новый контент
        container.innerHTML = '';
        container.appendChild(content);

        console.log(`✅ Показан контент для роли: ${userRole}`);
    }

    // ✅ Получение ID шаблона для роли (ИСПРАВЛЕНО)
    getTemplateId(userRole) {
        const templateMap = {
            'BASE_USER': 'base-user-content',
            'BUSINESS_USER': 'business-user-content',
            'COURIER': 'courier-content',
            'ADMIN': 'admin-content'
        };

        return templateMap[userRole] || 'base-user-content';
    }

    // ✅ Редирект на страницу логина
    redirectToLogin() {
        console.log('🔄 Перенаправление на страницу логина');
        window.location.href = '/login?error=unauthorized';
    }

    // ✅ Показ ошибки
    showError(message) {
        const container = document.getElementById('dashboard-content');
        if (container) {
            container.innerHTML = `
                <div class="error-message">
                    <div style="font-size: 64px; margin-bottom: 20px;">😞</div>
                    <h2>Упс! Что-то пошло не так</h2>
                    <p>${message}</p>
                    <div class="error-actions">
                        <a href="/login" class="btn btn-primary">
                            <i class="fas fa-home"></i> На главную
                        </a>
                        <button class="btn btn-secondary" onclick="window.location.reload()">
                            <i class="fas fa-redo"></i> Обновить
                        </button>
                    </div>
                </div>
            `;
        }
    }

    // ✅ Очистка данных авторизации (для logout)
    clearAuthData() {
        const keys = ['authToken', 'tokenType', 'user', 'rememberMe', 'lastRememberMe'];
        keys.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });
        console.log('🧹 Данные авторизации очищены');
    }
}

// ✅ Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    window.simpleDashboardAuth = new SimpleDashboardAuth();
});

// ✅ Глобальная функция для logout (можно вызвать из HTML)
async function logout() {
    try {
        console.log('🚪 Выполняется выход через dashboard API...');

        // Очищаем локальные данные
        if (window.simpleDashboardAuth) {
            window.simpleDashboardAuth.clearAuthData();
        }

        // Вызываем API logout
        const response = await fetch('/dashboard/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            console.log('✅ Logout успешен:', result.message);
        }
    } catch (error) {
        console.error('❌ Ошибка при logout:', error);
    } finally {
        // В любом случае редиректим на login
        window.location.href = '/login';
    }
}

// ✅ Совместимость со старыми ссылками (если есть в HTML)
window.dashboardAuth = {
    refresh: function() {
        console.log('🔄 Вызван устаревший метод dashboardAuth.refresh() - используем window.location.reload()');
        window.location.reload();
    },
    logout: logout,
    checkAuth: function() {
        if (window.simpleDashboardAuth) {
            return window.simpleDashboardAuth.checkAuthentication();
        }
    }
};