// /**
//  * profile-location.js
//  * Модуль для работы с геолокацией пользователя
//  */
//
// import { ProfileUtils } from './profile-utils.js';
// import { ProfileNotifications } from './profile-notifications.js';
//
// export class ProfileLocation {
//     static currentLocationDiv = null;
//     static isGettingLocation = false;
//
//     /**
//      * Инициализация модуля геолокации
//      */
//     static init() {
//         this.currentLocationDiv = document.getElementById('current-location');
//         console.log('📍 ProfileLocation module initialized');
//     }
//
//     /**
//      * Получить текущее местоположение пользователя
//      */
//     static getCurrentLocation() {
//         if (this.isGettingLocation) {
//             ProfileNotifications.warning('Определение местоположения уже выполняется...');
//             return;
//         }
//
//         if (!ProfileUtils.isGeolocationSupported()) {
//             ProfileNotifications.error('Геолокация не поддерживается вашим браузером');
//             return;
//         }
//
//         ProfileUtils.logUserAction('geolocation_request');
//         this.isGettingLocation = true;
//         this.showLoadingState();
//
//         const options = {
//             enableHighAccuracy: true,
//             timeout: 15000,
//             maximumAge: 300000 // 5 минут
//         };
//
//         navigator.geolocation.getCurrentPosition(
//             (position) => this.onLocationSuccess(position),
//             (error) => this.onLocationError(error),
//             options
//         );
//     }
//
//     /**
//      * Обработка успешного получения геолокации
//      * @param {GeolocationPosition} position - Позиция
//      */
//     static onLocationSuccess(position) {
//         this.isGettingLocation = false;
//
//         const lat = position.coords.latitude;
//         const lon = position.coords.longitude;
//         const accuracy = position.coords.accuracy;
//
//         console.log('📍 Location found:', { lat, lon, accuracy });
//         ProfileUtils.logUserAction('geolocation_success', { lat, lon, accuracy });
//
//         this.showLocationFound(lat, lon, accuracy);
//         ProfileNotifications.success('Местоположение определено успешно!');
//     }
//
//     /**
//      * Обработка ошибки получения геолокации
//      * @param {GeolocationPositionError} error - Ошибка
//      */
//     static onLocationError(error) {
//         this.isGettingLocation = false;
//
//         let errorMessage = 'Не удалось определить местоположение';
//         let userMessage = '';
//
//         switch(error.code) {
//             case error.PERMISSION_DENIED:
//                 errorMessage = 'Доступ к геолокации запрещен';
//                 userMessage = 'Разрешите доступ к геолокации в настройках браузера';
//                 break;
//             case error.POSITION_UNAVAILABLE:
//                 errorMessage = 'Местоположение недоступно';
//                 userMessage = 'Проверьте подключение к интернету и повторите попытку';
//                 break;
//             case error.TIMEOUT:
//                 errorMessage = 'Время ожидания истекло';
//                 userMessage = 'Попробуйте еще раз или проверьте настройки геолокации';
//                 break;
//         }
//
//         console.error('📍 Geolocation error:', error);
//         ProfileUtils.logUserAction('geolocation_error', {
//             code: error.code,
//             message: error.message
//         });
//
//         this.showLocationError(errorMessage, userMessage);
//         ProfileNotifications.error(errorMessage);
//     }
//
//     /**
//      * Показать состояние загрузки
//      */
//     static showLoadingState() {
//         if (!this.currentLocationDiv) return;
//
//         this.currentLocationDiv.innerHTML = `
//             <div style="text-align: center; padding: 24px;">
//                 <i class="fas fa-spinner fa-spin" style="font-size: 48px; margin-bottom: 16px; color: var(--primary-green);"></i>
//                 <div style="font-weight: 600; margin-bottom: 8px;">Определяем ваше местоположение...</div>
//                 <div style="font-size: 14px; color: var(--text-gray);">Это может занять несколько секунд</div>
//                 <button class="btn btn-secondary" onclick="ProfileLocation.cancelLocationRequest()" style="margin-top: 16px;">
//                     <i class="fas fa-times"></i> Отменить
//                 </button>
//             </div>
//         `;
//     }
//
//     /**
//      * Показать найденное местоположение
//      * @param {number} lat - Широта
//      * @param {number} lon - Долгота
//      * @param {number} accuracy - Точность в метрах
//      */
//     static showLocationFound(lat, lon, accuracy) {
//         if (!this.currentLocationDiv) return;
//
//         const coordinates = ProfileUtils.formatCoordinates(lat, lon);
//         const accuracyText = accuracy < 100 ? 'Высокая точность' :
//             accuracy < 1000 ? 'Средняя точность' : 'Низкая точность';
//
//         this.currentLocationDiv.innerHTML = `
//             <div style="text-align: left; width: 100%; padding: 20px; background: var(--accent-green); border-radius: var(--border-radius);">
//                 <div style="font-weight: 600; margin-bottom: 12px; color: var(--primary-green);">
//                     <i class="fas fa-map-marker-alt" style="margin-right: 8px;"></i>
//                     Текущее местоположение найдено
//                 </div>
//                 <div style="font-size: 14px; color: var(--text-gray); margin-bottom: 8px;">
//                     <strong>Координаты:</strong> ${coordinates}
//                 </div>
//                 <div style="font-size: 14px; color: var(--text-gray); margin-bottom: 16px;">
//                     <strong>Точность:</strong> ${accuracyText} (±${Math.round(accuracy)}м)
//                 </div>
//                 <div style="display: flex; gap: 8px; flex-wrap: wrap;">
//                     <button class="btn btn-primary" onclick="ProfileLocation.saveCurrentLocation(${lat}, ${lon})">
//                         <i class="fas fa-save"></i> Сохранить
//                     </button>
//                     <button class="btn btn-secondary" onclick="ProfileLocation.getCurrentLocation()">
//                         <i class="fas fa-redo"></i> Обновить
//                     </button>
//                     <button class="btn btn-secondary" onclick="ProfileLocation.showOnMap(${lat}, ${lon})">
//                         <i class="fas fa-map"></i> На карте
//                     </button>
//                 </div>
//             </div>
//         `;
//     }
//
//     /**
//      * Показать ошибку геолокации
//      * @param {string} errorMessage - Сообщение об ошибке
//      * @param {string} userMessage - Сообщение для пользователя
//      */
//     static showLocationError(errorMessage, userMessage) {
//         if (!this.currentLocationDiv) return;
//
//         this.currentLocationDiv.innerHTML = `
//             <div style="text-align: center; padding: 24px;">
//                 <i class="fas fa-exclamation-triangle" style="font-size: 48px; margin-bottom: 16px; color: var(--warning-orange);"></i>
//                 <div style="font-weight: 600; margin-bottom: 8px; color: var(--danger-red);">${errorMessage}</div>
//                 <div style="font-size: 14px; color: var(--text-gray); margin-bottom: 16px;">${userMessage}</div>
//                 <div style="display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;">
//                     <button class="btn btn-primary" onclick="ProfileLocation.getCurrentLocation()">
//                         <i class="fas fa-redo"></i> Попробовать снова
//                     </button>
//                     <button class="btn btn-secondary" onclick="ProfileLocation.showManualInput()">
//                         <i class="fas fa-keyboard"></i> Ввести вручную
//                     </button>
//                 </div>
//             </div>
//         `;
//     }
//
//     /**
//      * Отменить запрос геолокации
//      */
//     static cancelLocationRequest() {
//         this.isGettingLocation = false;
//         ProfileNotifications.info('Определение местоположения отменено');
//
//         if (!this.currentLocationDiv) return;
//
//         this.currentLocationDiv.innerHTML = `
//             <div style="text-align: center; padding: 24px;">
//                 <i class="fas fa-map-marked-alt" style="font-size