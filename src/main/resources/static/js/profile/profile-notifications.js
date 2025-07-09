/**
 * profile-notifications.js
 * Система уведомлений для профиля
 */

export class ProfileNotifications {
    static notifications = new Map();
    static notificationCounter = 0;

    /**
     * Показать уведомление
     * @param {string} message - Текст сообщения
     * @param {string} type - Тип: 'success', 'error', 'warning', 'info'
     * @param {Object} options - Дополнительные опции
     */
    static show(message, type = 'info', options = {}) {
        const config = {
            duration: 4000,
            closable: true,
            position: 'top-right',
            persistent: false,
            ...options
        };

        // Удаляем существующие уведомления если не persistent
        if (!config.persistent) {
            this.removeExisting();
        }

        const notification = this.create(message, type, config);
        this.display(notification, config);

        return notification.id;
    }

    /**
     * Показать уведомление об успехе
     * @param {string} message - Сообщение
     * @param {Object} options - Опции
     */
    static success(message, options = {}) {
        return this.show(message, 'success', options);
    }

    /**
     * Показать уведомление об ошибке
     * @param {string} message - Сообщение
     * @param {Object} options - Опции
     */
    static error(message, options = {}) {
        return this.show(message, 'error', {
            duration: 6000,
            ...options
        });
    }

    /**
     * Показать предупреждение
     * @param {string} message - Сообщение
     * @param {Object} options - Опции
     */
    static warning(message, options = {}) {
        return this.show(message, 'warning', options);
    }

    /**
     * Показать информационное сообщение
     * @param {string} message - Сообщение
     * @param {Object} options - Опции
     */
    static info(message, options = {}) {
        return this.show(message, 'info', options);
    }

    /**
     * Создать элемент уведомления
     * @param {string} message - Сообщение
     * @param {string} type - Тип
     * @param {Object} config - Конфигурация
     * @returns {Object} Объект уведомления
     */
    static create(message, type, config) {
        const id = `notification-${++this.notificationCounter}`;
        const element = document.createElement('div');

        element.id = id;
        element.className = 'profile-notification';
        element.setAttribute('data-type', type);

        // Стили уведомления
        element.style.cssText = this.getNotificationStyles(type, config.position);

        // Содержимое
        element.innerHTML = this.getNotificationHTML(message, type, config.closable, id);

        const notification = {
            id,
            element,
            type,
            message,
            config,
            createdAt: Date.now()
        };

        this.notifications.set(id, notification);
        return notification;
    }

    /**
     * Получить стили для уведомления
     * @param {string} type - Тип
     * @param {string} position - Позиция
     * @returns {string} CSS стили
     */
    static getNotificationStyles(type, position) {
        const colors = {
            success: 'var(--primary-green)',
            error: 'var(--danger-red)',
            warning: 'var(--warning-orange)',
            info: 'var(--info-blue)'
        };

        const positions = {
            'top-right': 'top: 100px; right: 20px;',
            'top-left': 'top: 100px; left: 20px;',
            'bottom-right': 'bottom: 20px; right: 20px;',
            'bottom-left': 'bottom: 20px; left: 20px;',
            'top-center': 'top: 100px; left: 50%; transform: translateX(-50%);'
        };

        return `
            position: fixed;
            ${positions[position] || positions['top-right']}
            z-index: 9999;
            max-width: 400px;
            min-width: 300px;
            padding: 16px 20px;
            border-radius: 12px;
            color: white;
            font-weight: 500;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
            background: ${colors[type] || colors.info};
            transform: translateX(${position.includes('right') ? '100%' : '-100%'});
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.1);
        `;
    }

    /**
     * Получить HTML содержимое уведомления
     * @param {string} message - Сообщение
     * @param {string} type - Тип
     * @param {boolean} closable - Можно ли закрывать
     * @param {string} id - ID уведомления
     * @returns {string} HTML
     */
    static getNotificationHTML(message, type, closable, id) {
        const icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-triangle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };

        const closeButton = closable ?
            `<button onclick="ProfileNotifications.remove('${id}')" 
                     style="background: none; border: none; color: white; font-size: 18px; cursor: pointer; padding: 0; margin-left: auto;">
                <i class="fas fa-times"></i>
             </button>` : '';

        return `
            <div style="display: flex; align-items: center; gap: 12px;">
                <i class="fas ${icons[type] || icons.info}" style="font-size: 20px;"></i>
                <span style="flex: 1; line-height: 1.4;">${message}</span>
                ${closeButton}
            </div>
        `;
    }

    /**
     * Отобразить уведомление
     * @param {Object} notification - Объект уведомления
     * @param {Object} config - Конфигурация
     */
    static display(notification, config) {
        document.body.appendChild(notification.element);

        // Анимация появления
        requestAnimationFrame(() => {
            notification.element.style.transform = 'translateX(0)';
        });

        // Автоматическое удаление
        if (config.duration > 0) {
            setTimeout(() => {
                this.remove(notification.id);
            }, config.duration);
        }

        // Добавляем обработчик клика
        notification.element.addEventListener('click', (e) => {
            if (e.target.closest('button')) return; // Игнорируем клик по кнопке закрытия
            if (config.onClick) {
                config.onClick(notification);
            }
        });
    }

    /**
     * Удалить уведомление
     * @param {string} id - ID уведомления
     */
    static remove(id) {
        const notification = this.notifications.get(id);
        if (!notification) return;

        const element = notification.element;
        const isRightSide = notification.config.position?.includes('right') ?? true;

        // Анимация исчезновения
        element.style.transform = `translateX(${isRightSide ? '100%' : '-100%'})`;
        element.style.opacity = '0';

        setTimeout(() => {
            if (element.parentElement) {
                element.remove();
            }
            this.notifications.delete(id);
        }, 400);
    }

    /**
     * Удалить все существующие уведомления
     */
    static removeExisting() {
        this.notifications.forEach((notification, id) => {
            this.remove(id);
        });
    }

    /**
     * Показать уведомление с подтверждением
     * @param {string} message - Сообщение
     * @param {Function} onConfirm - Callback подтверждения
     * @param {Function} onCancel - Callback отмены
     * @param {Object} options - Опции
     */
    static confirm(message, onConfirm, onCancel = null, options = {}) {
        const config = {
            duration: 0, // Бесконечное время
            closable: false,
            persistent: true,
            ...options
        };

        const confirmHTML = `
            <div style="display: flex; flex-direction: column; gap: 12px;">
                <div style="display: flex; align-items: center; gap: 12px;">
                    <i class="fas fa-question-circle" style="font-size: 20px;"></i>
                    <span style="flex: 1; line-height: 1.4;">${message}</span>
                </div>
                <div style="display: flex; gap: 8px; justify-content: flex-end;">
                    <button class="confirm-btn" style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white; padding: 8px 16px; border-radius: 6px; cursor: pointer;">
                        Да
                    </button>
                    <button class="cancel-btn" style="background: rgba(0,0,0,0.1); border: 1px solid rgba(255,255,255,0.3); color: white; padding: 8px 16px; border-radius: 6px; cursor: pointer;">
                        Отмена
                    </button>
                </div>
            </div>
        `;

        const notification = this.create(confirmHTML, 'warning', config);

        // Переопределяем HTML
        notification.element.innerHTML = confirmHTML;

        // Добавляем обработчики
        const confirmBtn = notification.element.querySelector('.confirm-btn');
        const cancelBtn = notification.element.querySelector('.cancel-btn');

        confirmBtn.addEventListener('click', () => {
            this.remove(notification.id);
            if (onConfirm) onConfirm();
        });

        cancelBtn.addEventListener('click', () => {
            this.remove(notification.id);
            if (onCancel) onCancel();
        });

        this.display(notification, config);
        return notification.id;
    }

    /**
     * Показать прогресс уведомление
     * @param {string} message - Сообщение
     * @param {number} progress - Прогресс (0-100)
     * @param {Object} options - Опции
     */
    static progress(message, progress = 0, options = {}) {
        const config = {
            duration: 0,
            closable: false,
            persistent: true,
            ...options
        };

        const progressHTML = `
            <div style="display: flex; flex-direction: column; gap: 8px;">
                <div style="display: flex; align-items: center; gap: 12px;">
                    <i class="fas fa-sync-alt fa-spin" style="font-size: 16px;"></i>
                    <span style="flex: 1;">${message}</span>
                    <span style="font-size: 14px;">${Math.round(progress)}%</span>
                </div>
                <div style="background: rgba(255,255,255,0.2); border-radius: 10px; height: 4px; overflow: hidden;">
                    <div style="background: rgba(255,255,255,0.8); height: 100%; width: ${progress}%; transition: width 0.3s ease; border-radius: 10px;"></div>
                </div>
            </div>
        `;

        if (options.updateExisting && this.notifications.has(options.updateExisting)) {
            // Обновляем существующее уведомление
            const notification = this.notifications.get(options.updateExisting);
            notification.element.innerHTML = progressHTML;
            return options.updateExisting;
        } else {
            // Создаем новое
            const notification = this.create(progressHTML, 'info', config);
            notification.element.innerHTML = progressHTML;
            this.display(notification, config);
            return notification.id;
        }
    }
}