// Создайте файл /static/js/dashboard-auth.js

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

    // ✅ Отображение дашборда
    renderDashboard(config) {
        const container = document.getElementById('dashboard-container');

        if (!container) {
            console.error('❌ Контейнер дашборда не найден');
            return;
        }

        // ✅ Базовый HTML дашборда
        container.innerHTML = `
            <div class="dashboard-header">
                <h1>Добро пожаловать, ${config.user.firstName}!</h1>
                <div class="user-info">
                    <span>Роль: ${config.user.roleDisplayName}</span>
                    <button onclick="dashboardAuth.logout()" class="logout-btn">Выйти</button>
                </div>
            </div>
            <div class="dashboard-content">
                <h2>${config.config.title}</h2>
                <div class="dashboard-sections">
                    ${config.config.sections.map(section => `
                        <div class="dashboard-section" data-section="${section}">
                            <h3>${this.getSectionTitle(section)}</h3>
                            <button onclick="dashboardAuth.loadSection('${section}')">Загрузить</button>
                            <div class="section-content" id="section-${section}"></div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;

        console.log('✅ Дашборд отображен');
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

    // ✅ Загрузка секции дашборда
    async loadSection(section) {
        console.log(`📊 Загружаем секцию: ${section}`);

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

                // ✅ Отображаем данные секции
                const sectionContainer = document.getElementById(`section-${section}`);
                if (sectionContainer) {
                    sectionContainer.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
                }
            } else {
                console.error(`❌ Ошибка загрузки секции ${section}:`, response.status);
            }

        } catch (error) {
            console.error(`❌ Ошибка загрузки секции ${section}:`, error);
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

    // ✅ Показ ошибки
    showError(message) {
        const container = document.getElementById('dashboard-container');
        if (container) {
            container.innerHTML = `
                <div class="error-message">
                    <h2>Ошибка</h2>
                    <p>${message}</p>
                    <button onclick="window.location.reload()">Обновить</button>
                    <button onclick="dashboardAuth.redirectToLogin()">На главную</button>
                </div>
            `;
        }
    }
}

// ✅ Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    window.dashboardAuth = new DashboardAuth();
});

// ✅ Экспорт для использования
if (typeof window !== 'undefined') {
    window.DashboardAuth = DashboardAuth;
}