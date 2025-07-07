// // Создайте файл /static/js/dashboard-auth.js
//
// class DashboardAuth {
//     constructor() {
//         this.API_BASE_URL = 'http://localhost:8082';
//         this.init();
//     }
//
//     init() {
//         console.log('🏠 Инициализация DashboardAuth');
//         this.checkAuthentication();
//     }
//
//     // ✅ Получение токена из storage
//     getAuthToken() {
//         try {
//             // Проверяем localStorage сначала (для Remember Me)
//             let token = localStorage.getItem('authToken');
//             if (token) return token;
//
//             // Затем sessionStorage
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
//             // Проверяем localStorage сначала (для Remember Me)
//             let userData = localStorage.getItem('user');
//             if (userData) return JSON.parse(userData);
//
//             // Затем sessionStorage
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
//     // ✅ Проверка авторизации при загрузке дашборда
//     async checkAuthentication() {
//         console.log('🔍 Проверка авторизации на дашборде...');
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
//         console.log(`🔑 Найден токен для пользователя: ${userData.email}`);
//
//         // ✅ Проверяем токен через API
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
//                     console.log('✅ Токен действителен - загружаем дашборд');
//                     this.loadDashboard(userData);
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
//     // ✅ Загрузка дашборда для авторизованного пользователя
//     async loadDashboard(userData) {
//         console.log(`👤 Загружаем дашборд для ${userData.email} (${userData.userRole})`);
//
//         try {
//             // ✅ Получаем конфигурацию дашборда
//             const authHeader = this.getAuthHeader();
//
//             const response = await fetch(`${this.API_BASE_URL}/dashboard/api/config`, {
//                 method: 'GET',
//                 headers: {
//                     'Authorization': authHeader,
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 const config = await response.json();
//                 console.log('✅ Конфигурация дашборда получена:', config);
//
//                 // ✅ Отображаем дашборд
//                 this.renderDashboard(config);
//             } else {
//                 console.error('❌ Ошибка получения конфигурации дашборда:', response.status);
//                 this.showError('Ошибка загрузки дашборда');
//             }
//
//         } catch (error) {
//             console.error('❌ Ошибка загрузки дашборда:', error);
//             this.showError('Ошибка подключения к серверу');
//         }
//     }
//
//     // ✅ Отображение дашборда
//     renderDashboard(config) {
//         const container = document.getElementById('dashboard-container');
//
//         if (!container) {
//             console.error('❌ Контейнер дашборда не найден');
//             return;
//         }
//
//         // ✅ Базовый HTML дашборда
//         container.innerHTML = `
//             <div class="dashboard-header">
//                 <h1>Добро пожаловать, ${config.user.firstName}!</h1>
//                 <div class="user-info">
//                     <span>Роль: ${config.user.roleDisplayName}</span>
//                     <button onclick="dashboardAuth.logout()" class="logout-btn">Выйти</button>
//                 </div>
//             </div>
//             <div class="dashboard-content">
//                 <h2>${config.config.title}</h2>
//                 <div class="dashboard-sections">
//                     ${config.config.sections.map(section => `
//                         <div class="dashboard-section" data-section="${section}">
//                             <h3>${this.getSectionTitle(section)}</h3>
//                             <button onclick="dashboardAuth.loadSection('${section}')">Загрузить</button>
//                             <div class="section-content" id="section-${section}"></div>
//                         </div>
//                     `).join('')}
//                 </div>
//             </div>
//         `;
//
//         console.log('✅ Дашборд отображен');
//     }
//
//     // ✅ Получение названия секции
//     getSectionTitle(section) {
//         const titles = {
//             'analytics': 'Аналитика',
//             'users': 'Пользователи',
//             'restaurants': 'Рестораны',
//             'orders': 'Заказы',
//             'favorites': 'Избранное',
//             'profile': 'Профиль',
//             'deliveries': 'Доставки',
//             'earnings': 'Заработок'
//         };
//         return titles[section] || section;
//     }
//
//     // ✅ Загрузка секции дашборда
//     async loadSection(section) {
//         console.log(`📊 Загружаем секцию: ${section}`);
//
//         try {
//             const authHeader = this.getAuthHeader();
//
//             const response = await fetch(`${this.API_BASE_URL}/dashboard/api/data/${section}`, {
//                 method: 'GET',
//                 headers: {
//                     'Authorization': authHeader,
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 const data = await response.json();
//                 console.log(`✅ Данные секции ${section} получены:`, data);
//
//                 // ✅ Отображаем данные секции
//                 const sectionContainer = document.getElementById(`section-${section}`);
//                 if (sectionContainer) {
//                     sectionContainer.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
//                 }
//             } else {
//                 console.error(`❌ Ошибка загрузки секции ${section}:`, response.status);
//             }
//
//         } catch (error) {
//             console.error(`❌ Ошибка загрузки секции ${section}:`, error);
//         }
//     }
//
//     // ✅ Выход из системы
//     async logout() {
//         console.log('🚪 Выход из системы...');
//
//         try {
//             const authHeader = this.getAuthHeader();
//
//             if (authHeader) {
//                 await fetch(`${this.API_BASE_URL}/api/auth/logout`, {
//                     method: 'POST',
//                     headers: {
//                         'Authorization': authHeader,
//                         'Content-Type': 'application/json'
//                     }
//                 });
//             }
//
//         } catch (error) {
//             console.error('❌ Ошибка при logout:', error);
//         } finally {
//             // ✅ Очищаем данные
//             this.clearAuthData();
//             this.redirectToLogin();
//         }
//     }
//
//     // ✅ Очистка данных авторизации
//     clearAuthData() {
//         const keys = ['authToken', 'tokenType', 'user', 'rememberMe', 'lastRememberMe'];
//         keys.forEach(key => {
//             localStorage.removeItem(key);
//             sessionStorage.removeItem(key);
//         });
//         console.log('🧹 Данные авторизации очищены');
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
//         const container = document.getElementById('dashboard-container');
//         if (container) {
//             container.innerHTML = `
//                 <div class="error-message">
//                     <h2>Ошибка</h2>
//                     <p>${message}</p>
//                     <button onclick="window.location.reload()">Обновить</button>
//                     <button onclick="dashboardAuth.redirectToLogin()">На главную</button>
//                 </div>
//             `;
//         }
//     }
// }
//
// // ✅ Инициализация при загрузке страницы
// document.addEventListener('DOMContentLoaded', function() {
//     window.dashboardAuth = new DashboardAuth();
// });
//
// // ✅ Экспорт для использования
// if (typeof window !== 'undefined') {
//     window.DashboardAuth = DashboardAuth;
// }


// ✅ Dashboard Authentication - QuickFood
// Управление авторизацией и отображением дашборда

class DashboardAuth {
    constructor() {
        this.API_BASE_URL = 'http://localhost:8082';
        this.init();
    }

    init() {
        console.log('🏠 Инициализация DashboardAuth');
        this.checkAuthentication();
    }

    // ✅ Получение токена из storage
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

    // ✅ Получение данных пользователя из storage
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

    // ✅ Создание Authorization header
    getAuthHeader() {
        const token = this.getAuthToken();
        const tokenType = localStorage.getItem('tokenType') || sessionStorage.getItem('tokenType') || 'Bearer';
        return token ? `${tokenType} ${token}` : null;
    }

    // ✅ Проверка авторизации при загрузке дашборда
    async checkAuthentication() {
        console.log('🔍 Проверка авторизации на дашборде...');

        const token = this.getAuthToken();
        const userData = this.getUserData();

        if (!token || !userData) {
            console.log('❌ Токен или данные пользователя отсутствуют');
            this.redirectToLogin();
            return;
        }

        console.log(`🔑 Найден токен для пользователя: ${userData.email}`);

        // ✅ Проверяем токен через API
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
                    console.log('✅ Токен действителен - загружаем дашборд');
                    this.loadDashboard(userData);
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

    // ✅ Загрузка дашборда для авторизованного пользователя
    async loadDashboard(userData) {
        console.log(`👤 Загружаем дашборд для ${userData.email} (${userData.userRole})`);

        try {
            // ✅ Получаем конфигурацию дашборда
            const authHeader = this.getAuthHeader();

            const response = await fetch(`${this.API_BASE_URL}/dashboard/api/config`, {
                method: 'GET',
                headers: {
                    'Authorization': authHeader,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const config = await response.json();
                console.log('✅ Конфигурация дашборда получена:', config);

                // ✅ Отображаем дашборд
                this.renderDashboard(config);
            } else {
                console.error('❌ Ошибка получения конфигурации дашборда:', response.status);
                this.showError('Ошибка загрузки дашборда');
            }

        } catch (error) {
            console.error('❌ Ошибка загрузки дашборда:', error);
            this.showError('Ошибка подключения к серверу');
        }
    }

    // ✅ Отображение дашборда с роль-зависимыми секциями
    renderDashboard(config) {
        const container = document.getElementById('dashboard-content');
        const userRole = config.user.role;

        if (!container) {
            console.error('❌ Контейнер дашборда не найден');
            return;
        }

        // ✅ Обновляем информацию о пользователе в header
        this.updateUserInfo(config.user);

        // ✅ Получаем шаблон для роли пользователя
        const template = this.getRoleTemplate(userRole);

        if (!template) {
            console.error('❌ Шаблон для роли не найден:', userRole);
            this.showError('Неподдерживаемая роль пользователя');
            return;
        }

        // ✅ Клонируем и вставляем шаблон
        const dashboardHtml = template.cloneNode(true);
        dashboardHtml.style.display = 'block';

        // ✅ Показываем только секции для текущей роли
        this.showRoleSections(dashboardHtml, userRole);

        container.innerHTML = '';
        container.appendChild(dashboardHtml);

        console.log(`✅ Дашборд отображен для роли: ${userRole}`);
    }

    // ✅ Обновление информации о пользователе в header
    updateUserInfo(user) {
        const userNameElement = document.getElementById('user-name');
        const userRoleElement = document.getElementById('user-role');

        if (userNameElement) {
            userNameElement.textContent = user.firstName + ' ' + user.lastName;
        }

        if (userRoleElement) {
            userRoleElement.textContent = user.roleDisplayName;
        }
    }

    // ✅ Получение шаблона для роли
    getRoleTemplate(userRole) {
        const templateMap = {
            'BASE_USER': 'base-user-template',
            'BUSINESS_USER': 'business-user-template',
            'COURIER': 'courier-template',
            'ADMIN': 'admin-template'
        };

        const templateId = templateMap[userRole];
        if (!templateId) {
            console.error('❌ Неизвестная роль:', userRole);
            return null;
        }

        const template = document.getElementById(templateId);
        if (!template) {
            console.error('❌ Шаблон не найден:', templateId);
            return null;
        }

        return template;
    }

    // ✅ Показ секций только для определенной роли
    showRoleSections(container, userRole) {
        // Скрываем все секции
        const allSections = container.querySelectorAll('.role-section');
        allSections.forEach(section => {
            section.style.display = 'none';
        });

        // Показываем только секции для текущей роли
        const roleSections = container.querySelectorAll(`[data-role="${userRole}"]`);
        roleSections.forEach(section => {
            section.style.display = 'block';
            // Добавляем анимацию появления
            section.style.animation = 'fadeInUp 0.6s ease forwards';
        });

        console.log(`✅ Показано ${roleSections.length} секций для роли: ${userRole}`);
    }

    // ✅ Получение названия секции
    getSectionTitle(section) {
        const titles = {
            'analytics': 'Аналитика',
            'users': 'Пользователи',
            'restaurants': 'Рестораны',
            'orders': 'Заказы',
            'favorites': 'Избранное',
            'profile': 'Профиль',
            'deliveries': 'Доставки',
            'earnings': 'Заработок'
        };
        return titles[section] || section;
    }

    // ✅ Загрузка секции дашборда с улучшенным отображением
    async loadSection(section) {
        console.log(`📊 Загружаем секцию: ${section}`);

        const contentElement = document.getElementById(`section-${section}`);

        if (!contentElement) {
            console.error(`❌ Контейнер секции ${section} не найден`);
            return;
        }

        // ✅ Показываем индикатор загрузки
        contentElement.innerHTML = `
            <div style="text-align: center; padding: 20px; color: #718096;">
                <i class="fas fa-spinner fa-spin"></i> Загрузка данных...
            </div>
        `;

        try {
            const authHeader = this.getAuthHeader();

            const response = await fetch(`${this.API_BASE_URL}/dashboard/api/data/${section}`, {
                method: 'GET',
                headers: {
                    'Authorization': authHeader,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                console.log(`✅ Данные секции ${section} получены:`, data);

                // ✅ Отображаем данные секции с красивым форматированием
                this.renderSectionData(contentElement, section, data);
            } else {
                console.error(`❌ Ошибка загрузки секции ${section}:`, response.status);
                contentElement.innerHTML = `
                    <div style="text-align: center; padding: 20px; color: #E53E3E;">
                        <i class="fas fa-exclamation-triangle"></i> 
                        Ошибка загрузки данных (${response.status})
                    </div>
                `;
            }

        } catch (error) {
            console.error(`❌ Ошибка загрузки секции ${section}:`, error);
            contentElement.innerHTML = `
                <div style="text-align: center; padding: 20px; color: #E53E3E;">
                    <i class="fas fa-exclamation-triangle"></i> 
                    Ошибка подключения к серверу
                </div>
            `;
        }
    }

    // ✅ Красивое отображение данных секции
    renderSectionData(container, section, data) {
        if (data.status === 'coming_soon') {
            container.innerHTML = `
                <div style="text-align: center; padding: 30px;">
                    <div style="font-size: 48px; margin-bottom: 15px;">🚧</div>
                    <h3 style="color: #4A5568; margin-bottom: 10px;">${data.message}</h3>
                    <p style="color: #718096; font-size: 14px;">
                        Секция будет доступна в ближайшее время
                    </p>
                    <div style="margin-top: 15px; font-size: 12px; color: #A0AEC0;">
                        Загружено: ${new Date(data._meta.loadedAt).toLocaleString('ru-RU')}
                    </div>
                </div>
            `;
        } else {
            // Показываем JSON данные с подсветкой
            container.innerHTML = `
                <div style="background: #1A202C; color: #E2E8F0; padding: 15px; border-radius: 8px; font-family: 'Courier New', monospace; font-size: 13px; line-height: 1.4;">
                    <pre>${JSON.stringify(data, null, 2)}</pre>
                </div>
            `;
        }
    }

    // ✅ Выход из системы
    async logout() {
        console.log('🚪 Выход из системы...');

        try {
            const authHeader = this.getAuthHeader();

            if (authHeader) {
                await fetch(`${this.API_BASE_URL}/api/auth/logout`, {
                    method: 'POST',
                    headers: {
                        'Authorization': authHeader,
                        'Content-Type': 'application/json'
                    }
                });
            }

        } catch (error) {
            console.error('❌ Ошибка при logout:', error);
        } finally {
            // ✅ Очищаем данные
            this.clearAuthData();
            this.redirectToLogin();
        }
    }

    // ✅ Очистка данных авторизации
    clearAuthData() {
        const keys = ['authToken', 'tokenType', 'user', 'rememberMe', 'lastRememberMe'];
        keys.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });
        console.log('🧹 Данные авторизации очищены');
    }

    // ✅ Редирект на страницу логина
    redirectToLogin() {
        console.log('🔄 Перенаправление на страницу логина');
        window.location.href = '/login?error=unauthorized';
    }

    // ✅ Показ ошибки с современным дизайном
    showError(message) {
        const container = document.getElementById('dashboard-content');
        if (container) {
            container.innerHTML = `
                <div class="error-message">
                    <div style="font-size: 64px; margin-bottom: 20px;">😞</div>
                    <h2>Упс! Что-то пошло не так</h2>
                    <p>${message}</p>
                    <div class="error-actions">
                        <button class="btn btn-primary" onclick="window.location.reload()">
                            <i class="fas fa-redo"></i> Обновить страницу
                        </button>
                        <button class="btn btn-secondary" onclick="dashboardAuth.redirectToLogin()">
                            <i class="fas fa-home"></i> На главную
                        </button>
                    </div>
                </div>
            `;
        }
    }
}

// ✅ Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function () {
    window.dashboardAuth = new DashboardAuth();
});

// ✅ Экспорт для использования
if (typeof window !== 'undefined') {
    window.DashboardAuth = DashboardAuth;
}
