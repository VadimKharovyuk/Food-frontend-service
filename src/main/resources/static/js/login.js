
// js/login.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Login page loaded');

    // Проверяем, авторизован ли уже пользователь
    checkExistingAuth();

    // Инициализируем форму входа
    initLoginForm();
});

// 🔍 Проверка существующей авторизации
async function checkExistingAuth() {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    const userData = apiClient.getUserData();

    if (token && userData) {
        try {
            // Проверяем валидность токена
            const response = await apiClient.validateToken();

            if (response.valid) {
                console.log('✅ Пользователь уже авторизован');

                // Показываем информацию о типе сессии
                const rememberMeActive = apiClient.isRememberMeActive();
                console.log(`🍪 Remember Me: ${rememberMeActive ? 'Активен' : 'Неактивен'}`);

                showUserProfile(userData, rememberMeActive);
                return;
            }
        } catch (error) {
            console.warn('⚠️ Токен невалиден, очищаем данные');
        }

        // Очищаем невалидные данные
        apiClient.clearTokens();
    }
}

// 🔧 Инициализация формы входа
function initLoginForm() {
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', handleLogin);

    // Восстанавливаем состояние checkbox из localStorage
    const rememberMeCheckbox = document.getElementById('rememberMe');
    const lastRememberMe = localStorage.getItem('lastRememberMe');
    if (lastRememberMe === 'true') {
        rememberMeCheckbox.checked = true;
    }
}

// 🔐 Обработка входа в систему
async function handleLogin(event) {
    event.preventDefault();

    MessageManager.clear();

    // Получаем данные формы
    const formData = new FormData(event.target);
    const credentials = {
        email: formData.get('email').trim(),
        password: formData.get('password'),
        rememberMe: formData.get('rememberMe') === 'on' // 🍪 Читаем checkbox
    };

    // Сохраняем выбор Remember Me для следующего раза
    localStorage.setItem('lastRememberMe', credentials.rememberMe);

    // Валидация
    if (!validateLoginData(credentials)) {
        return;
    }

    try {
        UIUtils.showLoading('loginBtn');

        console.log(`🔐 Авторизуем пользователя: ${credentials.email} (Remember Me: ${credentials.rememberMe})`);

        const response = await apiClient.login(credentials);

        if (response.success) {
            const welcomeMessage = credentials.rememberMe ?
                `🎉 Добро пожаловать, ${response.user.firstName}! (Вы будете запомнены на 7 дней)` :
                `🎉 Добро пожаловать, ${response.user.firstName}!`;

            MessageManager.success(welcomeMessage);

            // Показываем профиль пользователя
            showUserProfile(response.user, credentials.rememberMe);

            // Скрываем форму входа
            document.getElementById('loginForm').style.display = 'none';

        } else {
            MessageManager.error(response.message || 'Ошибка авторизации');
        }

    } catch (error) {
        console.error('❌ Ошибка авторизации:', error);
        MessageManager.error('Ошибка авторизации: ' + error.message);

    } finally {
        UIUtils.hideLoading('loginBtn', '🔐 Войти');
    }
}

// 👤 Показать профиль пользователя
function showUserProfile(user, rememberMeActive = false) {
    const profileDiv = document.getElementById('userProfile');
    const userInfoDiv = document.getElementById('userInfo');

    // Формируем информацию о пользователе
    userInfoDiv.innerHTML = `
        <div class="user-info-item">
            <span class="user-info-label">ID:</span>
            <span class="user-info-value">${user.id}</span>
        </div>
        <div class="user-info-item">
            <span class="user-info-label">Имя:</span>
            <span class="user-info-value">${user.firstName} ${user.lastName}</span>
        </div>
        <div class="user-info-item">
            <span class="user-info-label">Email:</span>
            <span class="user-info-value">${user.email}</span>
        </div>
        <div class="user-info-item">
            <span class="user-info-label">Роль:</span>
            <span class="user-info-value">${user.roleDisplayName}</span>
        </div>
        <div class="user-info-item">
            <span class="user-info-label">Тип сессии:</span>
            <span class="user-info-value">${rememberMeActive ? '🍪 Долгая (7 дней)' : '🔒 Обычная (до закрытия браузера)'}</span>
        </div>
        <div class="user-info-item">
            <span class="user-info-label">Зарегистрирован:</span>
            <span class="user-info-value">${UIUtils.formatDate(user.createdAt)}</span>
        </div>
        <div class="user-info-item">
            <span class="user-info-label">Последнее обновление:</span>
            <span class="user-info-value">${UIUtils.formatDate(user.updatedAt)}</span>
        </div>
    `;

    profileDiv.style.display = 'block';
}

// 🔄 Обновить профиль пользователя
async function refreshProfile() {
    try {
        MessageManager.info('🔄 Обновляем профиль...');

        const response = await apiClient.getProfile();

        if (response.success) {
            const rememberMeActive = apiClient.isRememberMeActive();
            showUserProfile(response.user, rememberMeActive);

            // Обновляем сохраненные данные
            apiClient.setUserData(response.user, rememberMeActive);

            MessageManager.success('✅ Профиль обновлен');
        } else {
            MessageManager.error('Ошибка обновления профиля');
        }

    } catch (error) {
        console.error('❌ Ошибка обновления профиля:', error);
        MessageManager.error('Ошибка обновления профиля: ' + error.message);
    }
}

// 🚪 Выход из системы
async function logout() {
    const rememberMeActive = apiClient.isRememberMeActive();
    const confirmMessage = rememberMeActive ?
        'Вы уверены, что хотите выйти? (Remember Me будет отключен)' :
        'Вы уверены, что хотите выйти?';

    if (!confirm(confirmMessage)) {
        return;
    }

    try {
        await apiClient.logout();

        // Скрываем профиль
        document.getElementById('userProfile').style.display = 'none';

        // Показываем форму входа
        const loginForm = document.getElementById('loginForm');
        loginForm.style.display = 'block';

        // Очищаем форму
        loginForm.reset();

        // Восстанавливаем последний выбор Remember Me
        const lastRememberMe = localStorage.getItem('lastRememberMe');
        if (lastRememberMe === 'true') {
            document.getElementById('rememberMe').checked = true;
        }

        MessageManager.info('👋 Вы вышли из системы');

    } catch (error) {
        console.error('❌ Ошибка выхода:', error);
        MessageManager.error('Ошибка выхода: ' + error.message);
    }
}

// ✅ Валидация данных входа
function validateLoginData(credentials) {
    const errors = [];

    if (!credentials.email) {
        errors.push('Введите email');
    } else if (!isValidEmail(credentials.email)) {
        errors.push('Введите корректный email');
    }

    if (!credentials.password) {
        errors.push('Введите пароль');
    }

    if (errors.length > 0) {
        MessageManager.error('Ошибки валидации:<br>• ' + errors.join('<br>• '));
        return false;
    }

    return true;
}

// 📧 Проверка корректности email
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 🍪 Показать информацию о Remember Me (для отладки)
function showRememberMeInfo() {
    const hasLocalStorageToken = !!localStorage.getItem('authToken');
    const hasSessionStorageToken = !!sessionStorage.getItem('authToken');

    console.log('🍪 Remember Me Status:');
    console.log('- localStorage token:', hasLocalStorageToken);
    console.log('- sessionStorage token:', hasSessionStorageToken);
    console.log('- Remember Me active:', apiClient.isRememberMeActive());
}

// Добавляем функцию в глобальную область для отладки
window.showRememberMeInfo = showRememberMeInfo;