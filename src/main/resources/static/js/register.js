// js/register.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Registration page loaded');

    // Загружаем роли при загрузке страницы
    loadUserRoles();

    // Инициализируем обработчики
    initRegistrationForm();
});


// 📋 Загрузка ролей пользователей
async function loadUserRoles() {
    try {
        console.log('📋 Загружаем роли пользователей...');
        const roles = await apiClient.getRoles();

        const roleSelect = document.getElementById('userRole');
        roleSelect.innerHTML = '<option value="">Выберите тип аккаунта</option>';

        roles.forEach(role => {
            const option = document.createElement('option');
            option.value = role;
            option.textContent = getRoleDisplayName(role);
            roleSelect.appendChild(option);
        });

        console.log('✅ Роли загружены:', roles);

    } catch (error) {
        console.error('❌ Ошибка загрузки ролей:', error);
        MessageManager.error('Не удалось загрузить типы аккаунтов');
    }
}

// 👥 Получить отображаемое название роли
function getRoleDisplayName(role) {
    const roleNames = {
        'BASE_USER': '🛒 Покупатель',
        'BUSINESS_USER': '🏪 Владелец магазина',
        'COURIER': '🚚 Курьер',
        'ADMIN': '👑 Администратор'
    };
    return roleNames[role] || role;
}

// 🔧 Инициализация формы регистрации
function initRegistrationForm() {
    const form = document.getElementById('registerForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // Проверка email при изменении
    emailInput.addEventListener('blur', checkEmailAvailability);

    // Проверка совпадения паролей
    confirmPasswordInput.addEventListener('input', checkPasswordMatch);
    passwordInput.addEventListener('input', checkPasswordMatch);

    // Обработка отправки формы
    form.addEventListener('submit', handleRegistration);
}

// ✅ Проверка доступности email
async function checkEmailAvailability() {
    const emailInput = document.getElementById('email');
    const email = emailInput.value.trim();

    if (!email || !isValidEmail(email)) {
        UIUtils.setFieldStatus('email', '', '');
        return;
    }

    try {
        UIUtils.setFieldStatus('email', '🔄 Проверяем...', 'info');

        const response = await apiClient.checkEmailAvailability(email);

        if (response.available) {
            UIUtils.setFieldStatus('email', '✅ Email доступен', 'success');
        } else {
            UIUtils.setFieldStatus('email', '❌ Email уже используется', 'error');
        }

    } catch (error) {
        UIUtils.setFieldStatus('email', '⚠️ Не удалось проверить email', 'error');
    }
}

// 🔐 Проверка совпадения паролей
function checkPasswordMatch() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (confirmPassword === '') {
        UIUtils.setFieldStatus('confirmPassword', '', '');
        return;
    }

    if (password === confirmPassword) {
        UIUtils.setFieldStatus('confirmPassword', '✅ Пароли совпадают', 'success');
    } else {
        UIUtils.setFieldStatus('confirmPassword', '❌ Пароли не совпадают', 'error');
    }
}

// 📝 Обработка регистрации
async function handleRegistration(event) {
    event.preventDefault();

    MessageManager.clear();

    // Получаем данные формы
    const formData = new FormData(event.target);
    const userData = {
        firstName: formData.get('firstName').trim(),
        lastName: formData.get('lastName').trim(),
        email: formData.get('email').trim(),
        password: formData.get('password'),
        userRole: formData.get('userRole')
    };

    // Валидация
    if (!validateRegistrationData(userData)) {
        return;
    }

    // Проверяем совпадение паролей
    const confirmPassword = formData.get('confirmPassword');
    if (userData.password !== confirmPassword) {
        MessageManager.error('Пароли не совпадают');
        return;
    }

    try {
        UIUtils.showLoading('submitBtn');

        console.log('📝 Регистрируем пользователя:', userData.email);

        const response = await apiClient.register(userData);

        if (response.success) {
            MessageManager.success(
                `🎉 Регистрация успешна! Добро пожаловать, ${response.user.firstName}!`
            );

            // Очищаем форму
            event.target.reset();

            // Предлагаем войти
            setTimeout(() => {
                if (confirm('Хотите войти в систему?')) {
                    window.location.href = 'login.html';
                }
            }, 2000);

        } else {
            MessageManager.error(response.message || 'Ошибка регистрации');
        }

    } catch (error) {
        console.error('❌ Ошибка регистрации:', error);
        MessageManager.error('Ошибка регистрации: ' + error.message);

    } finally {
        UIUtils.hideLoading('submitBtn', '📝 Зарегистрироваться');
    }
}

// ✅ Валидация данных регистрации
function validateRegistrationData(userData) {
    const errors = [];

    if (!userData.firstName) {
        errors.push('Введите имя');
    }

    if (!userData.lastName) {
        errors.push('Введите фамилию');
    }

    if (!userData.email) {
        errors.push('Введите email');
    } else if (!isValidEmail(userData.email)) {
        errors.push('Введите корректный email');
    }

    if (!userData.password) {
        errors.push('Введите пароль');
    } else if (userData.password.length < 6) {
        errors.push('Пароль должен содержать минимум 6 символов');
    }

    if (!userData.userRole) {
        errors.push('Выберите тип аккаунта');
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