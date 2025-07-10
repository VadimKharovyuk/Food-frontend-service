/**
 * 📍 profile-location.js - Модуль управления геолокацией с геокодированием
 * Интеграция с REST API /api/location/* и OpenStreetMap Nominatim
 * ПОЛНАЯ ВЕРСИЯ с определением города и страны
 */

// ================================
// 🔐 УТИЛИТЫ АВТОРИЗАЦИИ
// ================================

/**
 * Получение токена авторизации из cookies или localStorage
 */
function getAuthToken() {
    console.log('🔍 Ищем токен авторизации...');

    // 1. Проверяем все cookies
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'authToken') {
            console.log('✅ Найден authToken в cookies');
            return value;
        }
    }

    // 2. Проверяем localStorage
    const tokenFromStorage = localStorage.getItem('authToken');
    if (tokenFromStorage) {
        console.log('✅ Найден authToken в localStorage');
        return tokenFromStorage;
    }

    // 3. Проверяем sessionStorage
    const tokenFromSession = sessionStorage.getItem('authToken');
    if (tokenFromSession) {
        console.log('✅ Найден authToken в sessionStorage');
        return tokenFromSession;
    }

    console.warn('⚠️ JWT токен не найден');
    return null;
}

// ================================
// 🌐 API ИНТЕГРАЦИЯ
// ================================

/**
 * Загрузка текущей геолокации через REST API
 */
async function loadCurrentLocation() {
    try {
        console.log('📍 Загружаем текущую геолокацию через API...');

        const token = getAuthToken();
        const headers = {
            'Content-Type': 'application/json',
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch('/api/location/me', {
            method: 'GET',
            headers: headers
        });

        const result = await response.json();

        if (result.success && result.data && result.data.hasLocation) {
            console.log('✅ Геолокация загружена:', result.data);
            updateLocationDisplay(result.data);
            return result.data;
        } else {
            console.log('ℹ️ Геолокация не установлена');
            return null;
        }

    } catch (error) {
        console.error('❌ Ошибка загрузки геолокации:', error);
        showNotification('Ошибка загрузки геолокации', 'error');
        return null;
    }
}

/**
 * Сохранение координат через REST API с автоматическим геокодированием
 */
async function saveCurrentLocation(lat, lon) {
    try {
        showNotification('Сохраняем координаты...', 'info');

        console.log('💾 Сохраняем координаты:', { lat, lon });

        const token = getAuthToken();
        const headers = {
            'Content-Type': 'application/json',
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // Отправляем координаты - сервер сам определит адрес (autoGeocode: true)
        const response = await fetch('/api/location/me', {
            method: 'PUT',
            headers: headers,
            body: JSON.stringify({
                latitude: lat,
                longitude: lon
            })
        });

        const result = await response.json();

        if (result.success) {
            console.log('✅ Координаты сохранены:', result.data);
            showNotification('Местоположение успешно сохранено!', 'success');

            // Перезагружаем страницу чтобы показать сохраненную геолокацию
            setTimeout(() => {
                window.location.reload();
            }, 2000);

        } else {
            throw new Error(result.message || 'Ошибка сохранения координат');
        }

    } catch (error) {
        console.error('❌ Ошибка сохранения координат:', error);
        showNotification('Ошибка: ' + error.message, 'error');
    }
}

/**
 * Очистка геолокации через REST API
 */
async function clearLocation() {
    if (!confirm('Удалить сохраненную геолокацию?')) {
        return;
    }

    try {
        showNotification('Очищаем геолокацию...', 'info');

        const token = getAuthToken();
        const headers = {
            'Content-Type': 'application/json',
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch('/api/location/me', {
            method: 'DELETE',
            headers: headers
        });

        const result = await response.json();

        if (result.success) {
            console.log('✅ Геолокация очищена');
            showNotification('Геолокация очищена!', 'success');

            // Перезагружаем страницу
            setTimeout(() => {
                window.location.reload();
            }, 2000);

        } else {
            throw new Error(result.message || 'Ошибка очистки геолокации');
        }

    } catch (error) {
        console.error('❌ Ошибка очистки геолокации:', error);
        showNotification('Ошибка: ' + error.message, 'error');
    }
}

// ================================
// 🌍 БРАУЗЕРНАЯ ГЕОЛОКАЦИЯ С ГЕОКОДИРОВАНИЕМ
// ================================

/**
 * Получение текущего местоположения через браузер с определением адреса
 */
function getCurrentLocation() {
    const locationDiv = document.getElementById('current-location');

    if (!navigator.geolocation) {
        showNotification('Геолокация не поддерживается вашим браузером', 'error');
        return;
    }

    // Показываем загрузку
    locationDiv.innerHTML = createLoadingLocationHTML();

    console.log('🌍 Запрашиваем геолокацию у браузера...');

    navigator.geolocation.getCurrentPosition(
        async function(position) {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            const accuracy = position.coords.accuracy;

            console.log('✅ Геолокация получена:', { lat, lon, accuracy });

            // Показываем загрузку определения адреса
            locationDiv.innerHTML = createGeocodingLocationHTML(lat, lon, accuracy);

            // Пытаемся определить адрес по координатам
            try {
                const address = await getAddressByCoordinates(lat, lon);
                console.log('📍 Адрес определен:', address);

                // Показываем результат с адресом
                locationDiv.innerHTML = createFoundLocationWithAddressHTML(lat, lon, accuracy, address);
            } catch (error) {
                console.warn('⚠️ Не удалось определить адрес:', error);

                // Показываем результат без адреса
                locationDiv.innerHTML = createFoundLocationHTML(lat, lon, accuracy);
            }
        },
        function(error) {
            console.error('❌ Ошибка геолокации:', error);

            let errorMessage = getGeolocationErrorMessage(error);
            locationDiv.innerHTML = createErrorLocationHTML(errorMessage);

            showNotification(errorMessage, 'error');
        },
        {
            enableHighAccuracy: true,
            timeout: 15000,
            maximumAge: 600000 // 10 минут кеш
        }
    );
}

/**
 * Получение адреса по координатам через OpenStreetMap Nominatim API
 * Возвращает объект в формате, совместимом с сервером
 */
async function getAddressByCoordinates(lat, lon) {
    try {
        console.log('🔍 Определяем адрес по координатам:', lat, lon);

        const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=16&addressdetails=1&accept-language=ru`,
            {
                headers: {
                    'User-Agent': 'QuickFood-WebApp/1.0'
                }
            }
        );

        if (!response.ok) {
            throw new Error('Ошибка запроса к геокодингу');
        }

        const data = await response.json();
        console.log('🌍 Ответ от Nominatim:', data);

        if (data && data.display_name && data.address) {
            const addr = data.address;

            // Определяем город (в порядке приоритета)
            const city = addr.city || addr.town || addr.village || addr.municipality || addr.county;

            // Определяем страну
            const country = addr.country;

            // Формируем короткий адрес как "Город, Страна"
            let shortAddress = '';
            if (city && country) {
                shortAddress = `${city}, ${country}`;
            } else if (city) {
                shortAddress = city;
            } else if (country) {
                shortAddress = country;
            } else {
                // Если ничего не найдено, берем из display_name
                const parts = data.display_name.split(',');
                shortAddress = parts.slice(0, 2).join(',').trim();
            }

            return {
                full: data.display_name,
                short: shortAddress,
                city: city || '',
                country: country || '',
                street: addr.road || '',
                house_number: addr.house_number || '',
                // Дополнительные поля для отладки
                formattedCoordinates: `${lat.toFixed(6)}, ${lon.toFixed(6)}`,
                hasLocation: true
            };
        } else {
            throw new Error('Адрес не найден в ответе');
        }
    } catch (error) {
        console.error('❌ Ошибка геокодирования:', error);
        throw error;
    }
}

/**
 * Получение сообщения об ошибке геолокации
 */
function getGeolocationErrorMessage(error) {
    switch(error.code) {
        case error.PERMISSION_DENIED:
            return 'Доступ к геолокации запрещен. Разрешите доступ в настройках браузера.';
        case error.POSITION_UNAVAILABLE:
            return 'Местоположение недоступно. Проверьте подключение к интернету.';
        case error.TIMEOUT:
            return 'Время ожидания истекло. Попробуйте еще раз.';
        default:
            return 'Не удалось определить местоположение.';
    }
}

// ================================
// 🎨 HTML ГЕНЕРАТОРЫ
// ================================

/**
 * HTML для состояния загрузки
 */
function createLoadingLocationHTML() {
    return `
        <div style="text-align: center; padding: 32px; background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); border-radius: 12px; border: 2px dashed #dee2e6;">
            <i class="fas fa-spinner fa-spin" style="font-size: 48px; margin-bottom: 16px; color: #28a745;"></i>
            <div style="font-size: 16px; margin-bottom: 8px; font-weight: 600;">Определяем ваше местоположение...</div>
            <div style="font-size: 14px; color: #6c757d;">Это может занять несколько секунд</div>
        </div>
    `;
}

/**
 * HTML для состояния определения адреса
 */
function createGeocodingLocationHTML(lat, lon, accuracy) {
    return `
        <div style="background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%); padding: 20px; border-radius: 12px; border: 2px solid #ffc107;">
            <div style="font-weight: 600; margin-bottom: 16px; text-align: center;">
                <i class="fas fa-search-location" style="color: #856404; margin-right: 8px;"></i>
                Определяем название места...
            </div>
            
            <!-- Отображение координат -->
            <div style="background: rgba(255,255,255,0.8); padding: 16px; border-radius: 8px; margin-bottom: 16px; text-align: center;">
                <div style="font-size: 16px; font-weight: 600; margin-bottom: 8px; color: #856404;">
                    📍 ${lat.toFixed(6)}, ${lon.toFixed(6)}
                </div>
                <div style="font-size: 14px; color: #666;">
                    🎯 Точность: ~${Math.round(accuracy)} метров
                </div>
            </div>
            
            <!-- Индикатор загрузки -->
            <div style="text-align: center; padding: 20px;">
                <i class="fas fa-spinner fa-spin" style="font-size: 24px; margin-bottom: 12px; color: #856404;"></i>
                <div style="font-size: 14px; color: #856404;">Получаем информацию о местоположении...</div>
            </div>
        </div>
    `;
}

/**
 * HTML для найденного местоположения С АДРЕСОМ
 */
function createFoundLocationWithAddressHTML(lat, lon, accuracy, address) {
    return `
        <div style="background: linear-gradient(135deg, #e8f5e8 0%, #f0f8f0 100%); padding: 20px; border-radius: 12px; border: 2px solid #28a745;">
            <div style="font-weight: 600; margin-bottom: 16px; text-align: center;">
                <i class="fas fa-map-marker-alt" style="color: #28a745; margin-right: 8px;"></i>
                Местоположение успешно определено!
            </div>
            
            <!-- Информация о месте -->
            <div style="background: rgba(255,255,255,0.9); padding: 20px; border-radius: 12px; margin-bottom: 20px; border: 2px solid #28a745;">
                <div style="text-align: center; margin-bottom: 16px;">
                    <i class="fas fa-location-arrow" style="font-size: 48px; margin-bottom: 12px; color: #28a745;"></i>
                </div>
                
                <!-- Название места -->
                <div style="text-align: center; margin-bottom: 16px; padding: 12px; background: rgba(40, 167, 69, 0.1); border-radius: 8px;">
                    <div style="font-size: 20px; font-weight: 700; color: #155724; margin-bottom: 4px;">
                        🏠 ${address.short}
                    </div>
                    ${address.city ? `<div style="font-size: 14px; color: #666; margin-bottom: 2px;">Город: ${address.city}</div>` : ''}
                    ${address.country ? `<div style="font-size: 14px; color: #666; margin-bottom: 2px;">Страна: ${address.country}</div>` : ''}
                    ${address.full !== address.short ? `<div style="font-size: 11px; color: #999; margin-top: 8px; font-style: italic;">Полный адрес: ${address.full}</div>` : ''}
                </div>
                
                <!-- Координаты -->
                <div style="text-align: center; margin-bottom: 12px;">
                    <div style="font-size: 16px; font-weight: 600; color: #155724; margin-bottom: 4px;">
                        📍 ${lat.toFixed(6)}, ${lon.toFixed(6)}
                    </div>
                    <div style="font-size: 14px; color: #666;">
                        🎯 Точность: ~${Math.round(accuracy)} метров
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <div style="font-size: 14px; color: #28a745; font-weight: 500;">
                        ✅ Готово к сохранению
                    </div>
                </div>
            </div>
            
            <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                <button class="btn btn-primary" onclick="saveCurrentLocation(${lat}, ${lon})" 
                        style="flex: 1; min-width: 140px; background: #28a745; color: white; padding: 16px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: 600;">
                    <i class="fas fa-save" style="margin-right: 8px;"></i>Сохранить местоположение
                </button>
                <button class="btn btn-secondary" onclick="getCurrentLocation()" 
                        style="flex: 0 0 auto; background: #6c757d; color: white; padding: 16px; border: none; border-radius: 8px; cursor: pointer;">
                    <i class="fas fa-redo"></i> Обновить
                </button>
            </div>
        </div>
    `;
}

/**
 * HTML для найденного местоположения (простое отображение без адреса - fallback)
 */
function createFoundLocationHTML(lat, lon, accuracy) {
    return `
        <div style="background: linear-gradient(135deg, #e8f5e8 0%, #f0f8f0 100%); padding: 20px; border-radius: 12px; border: 2px solid #28a745;">
            <div style="font-weight: 600; margin-bottom: 16px; text-align: center;">
                <i class="fas fa-map-marker-alt" style="color: #28a745; margin-right: 8px;"></i>
                Местоположение определено!
            </div>
            
            <!-- Простое отображение координат -->
            <div style="background: rgba(255,255,255,0.8); padding: 24px; border-radius: 12px; margin-bottom: 20px; text-align: center; border: 2px solid #28a745;">
                <i class="fas fa-location-arrow" style="font-size: 48px; margin-bottom: 16px; color: #28a745;"></i>
                <div style="font-size: 18px; font-weight: 600; margin-bottom: 8px; color: #155724;">
                    📍 ${lat.toFixed(6)}, ${lon.toFixed(6)}
                </div>
                <div style="font-size: 16px; margin-bottom: 12px; color: #666;">
                    🎯 Точность: ~${Math.round(accuracy)} метров
                </div>
                <div style="font-size: 12px; color: #856404; font-style: italic;">
                    ⚠️ Название места не определено
                </div>
                <div style="font-size: 14px; color: #28a745; font-weight: 500; margin-top: 8px;">
                    ✅ Готово к сохранению
                </div>
            </div>
            
            <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                <button class="btn btn-primary" onclick="saveCurrentLocation(${lat}, ${lon})" 
                        style="flex: 1; min-width: 140px; background: #28a745; color: white; padding: 16px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: 600;">
                    <i class="fas fa-save" style="margin-right: 8px;"></i>Сохранить местоположение
                </button>
                <button class="btn btn-secondary" onclick="getCurrentLocation()" 
                        style="flex: 0 0 auto; background: #6c757d; color: white; padding: 16px; border: none; border-radius: 8px; cursor: pointer;">
                    <i class="fas fa-redo"></i> Обновить
                </button>
            </div>
        </div>
    `;
}

/**
 * HTML для ошибки геолокации
 */
function createErrorLocationHTML(errorMessage) {
    return `
        <div style="text-align: center; padding: 32px; background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%); border-radius: 12px; border: 2px solid #fc8181;">
            <i class="fas fa-exclamation-triangle" style="font-size: 48px; margin-bottom: 16px; color: #e53e3e;"></i>
            <div style="margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #742a2a;">${errorMessage}</div>
            <div style="display: flex; gap: 12px; justify-content: center; flex-wrap: wrap;">
                <button class="btn btn-secondary" onclick="getCurrentLocation()" style="background: #6c757d; color: white; padding: 12px; border: none; border-radius: 8px; cursor: pointer;">
                    <i class="fas fa-redo"></i> Попробовать снова
                </button>
                <button class="btn btn-outline" onclick="addAddressManually()" style="background: transparent; border: 2px solid #28a745; color: #28a745; padding: 12px; border-radius: 8px; cursor: pointer;">
                    <i class="fas fa-edit"></i> Ввести вручную
                </button>
            </div>
        </div>
    `;
}

// ================================
// 🖼️ УПРАВЛЕНИЕ ОТОБРАЖЕНИЕМ
// ================================

/**
 * Обновление отображения сохраненной геолокации
 */
function updateLocationDisplay(locationData) {
    console.log('🎨 Обновляем отображение геолокации:', locationData);

    if (locationData.hasLocation) {
        // Показываем блок сохраненной геолокации
        const savedLocationDiv = document.querySelector('.saved-location');
        if (savedLocationDiv) {
            savedLocationDiv.style.display = 'block';
        }

        // Обновляем кнопку определения местоположения
        const getCurrentBtn = document.querySelector('button[onclick="getCurrentLocation()"]');
        if (getCurrentBtn && !getCurrentBtn.closest('.saved-location')) {
            getCurrentBtn.innerHTML = '<i class="fas fa-crosshairs" style="margin-right: 8px;"></i>Обновить местоположение';
        }
    } else {
        // Скрываем блок сохраненной геолокации
        const savedLocationDiv = document.querySelector('.saved-location');
        if (savedLocationDiv) {
            savedLocationDiv.style.display = 'none';
        }
    }
}

// ================================
// 📝 УПРАВЛЕНИЕ АДРЕСАМИ
// ================================

/**
 * Ручной ввод адреса
 */
function addAddressManually() {
    const street = prompt('Введите улицу и номер дома:');
    if (!street || !street.trim()) return;

    const city = prompt('Введите город:');
    if (!city || !city.trim()) return;

    const country = prompt('Введите страну:') || 'Россия';

    // Сохраняем через REST API
    saveAddressLocation(street.trim(), city.trim(), country.trim());
}

/**
 * Сохранение адреса через REST API
 */
async function saveAddressLocation(street, city, country) {
    try {
        showNotification('Сохраняем адрес...', 'info');

        console.log('🏠 Сохраняем адрес:', { street, city, country });

        const response = await fetch('/api/location/address', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                street: street,
                city: city,
                country: country
            })
        });

        const result = await response.json();

        if (result.success) {
            console.log('✅ Адрес сохранен:', result.data);
            showNotification('Адрес успешно сохранен!', 'success');

            // Перезагружаем страницу для обновления всех данных
            setTimeout(() => {
                window.location.reload();
            }, 2000);

        } else {
            throw new Error(result.message || 'Ошибка сохранения адреса');
        }

    } catch (error) {
        console.error('❌ Ошибка сохранения адреса:', error);
        showNotification('Ошибка: ' + error.message, 'error');
    }
}

/**
 * Обновленная функция добавления адреса (из основного профиля)
 */
function addAddress() {
    addAddressManually();
}

// ================================
// 🚀 ИНИЦИАЛИЗАЦИЯ
// ================================

/**
 * Инициализация модуля геолокации
 */
function initializeLocationModule() {
    console.log('📍 Инициализация модуля геолокации (с геокодированием)...');

    // Загружаем текущую геолокацию при загрузке страницы
    loadCurrentLocation();

    console.log('✅ Модуль геолокации инициализирован');
}

// ================================
// 🌐 ЭКСПОРТ ДЛЯ ГЛОБАЛЬНОГО ИСПОЛЬЗОВАНИЯ
// ================================

// Делаем функции доступными глобально для onclick handlers
window.getCurrentLocation = getCurrentLocation;
window.saveCurrentLocation = saveCurrentLocation;
window.clearLocation = clearLocation;
window.addAddress = addAddress;
window.addAddressManually = addAddressManually;
window.loadCurrentLocation = loadCurrentLocation;
window.getAddressByCoordinates = getAddressByCoordinates;

// Автоинициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', initializeLocationModule);