/**
 * JavaScript для управления избранными магазинами
 * Поддерживает добавление/удаление из избранного с красивыми уведомлениями
 */

class FavoriteStoreManager {
    constructor() {
        this.init();
    }

    init() {
        console.log('🚀 Инициализация FavoriteStoreManager');
        this.setupEventListeners();
        this.createNotificationContainer();
    }

    /**
     * Настройка обработчиков событий
     */
    setupEventListeners() {
        // Обработчики для кнопок добавления в избранное
        document.addEventListener('click', (e) => {
            // Кнопка добавления в избранное
            if (e.target.closest('.add-to-favorites-btn')) {
                e.preventDefault();
                const button = e.target.closest('.add-to-favorites-btn');
                const storeId = button.dataset.storeId;
                if (storeId) {
                    this.addToFavorites(storeId, button);
                }
            }

            // Кнопка удаления из избранного
            if (e.target.closest('.remove-from-favorites-btn')) {
                e.preventDefault();
                const button = e.target.closest('.remove-from-favorites-btn');
                const favoriteId = button.dataset.favoriteId;
                if (favoriteId) {
                    this.removeFromFavorites(favoriteId, button);
                }
            }

            // Иконка сердечка (toggle)
            if (e.target.closest('.favorite-heart-btn')) {
                e.preventDefault();
                const button = e.target.closest('.favorite-heart-btn');
                const storeId = button.dataset.storeId;
                const isFavorite = button.classList.contains('is-favorite');

                if (isFavorite) {
                    const favoriteId = button.dataset.favoriteId;
                    this.removeFromFavorites(favoriteId, button);
                } else {
                    this.addToFavorites(storeId, button);
                }
            }
        });
    }

    /**
     * Добавление магазина в избранное
     */
    async addToFavorites(storeId, buttonElement = null) {
        console.log(`➕ Добавление магазина ${storeId} в избранное`);

        // Отключаем кнопку на время запроса
        if (buttonElement) {
            this.setButtonLoading(buttonElement, true);
        }

        try {
            const response = await fetch(`/favorites/stores/add/${storeId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin'
            });

            const data = await response.json();

            if (data.success) {
                console.log('✅ Магазин успешно добавлен в избранное');

                // Обновляем интерфейс
                if (buttonElement) {
                    this.updateButtonToFavorite(buttonElement, data.data);
                }

                // Показываем уведомление
                this.showNotification('success', data.message || 'Магазин добавлен в избранное');

                // Обновляем счетчик избранного в навигации
                this.updateFavoriteCounter(1);

            } else {
                console.error('❌ Ошибка добавления в избранное:', data.message);

                if (response.status === 401) {
                    this.showNotification('error', 'Необходимо войти в систему');
                    // Перенаправляем на страницу входа через 2 секунды
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                } else {
                    this.showNotification('error', data.message || 'Ошибка добавления в избранное');
                }
            }

        } catch (error) {
            console.error('💥 Исключение при добавлении в избранное:', error);
            this.showNotification('error', 'Произошла ошибка сети');
        } finally {
            // Включаем кнопку обратно
            if (buttonElement) {
                this.setButtonLoading(buttonElement, false);
            }
        }
    }

    /**
     * Удаление магазина из избранного
     */
    async removeFromFavorites(favoriteId, buttonElement = null) {
        console.log(`➖ Удаление магазина из избранного: ${favoriteId}`);

        // Показываем подтверждение
        if (!await this.showConfirmDialog('Удалить магазин из избранного?')) {
            return;
        }

        // Отключаем кнопку на время запроса
        if (buttonElement) {
            this.setButtonLoading(buttonElement, true);
        }

        try {
            const response = await fetch(`/favorites/stores/remove/${favoriteId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'same-origin'
            });

            const data = await response.json();

            if (data.success) {
                console.log('✅ Магазин успешно удален из избранного');

                // Обновляем интерфейс
                if (buttonElement) {
                    this.updateButtonToNormal(buttonElement);
                }

                // Показываем уведомление
                this.showNotification('success', data.message || 'Магазин удален из избранного');

                // Обновляем счетчик избранного в навигации
                this.updateFavoriteCounter(-1);

                // Если мы на странице избранного, удаляем карточку
                if (window.location.pathname.includes('/favorites/stores')) {
                    this.removeStoreCard(favoriteId);
                }

            } else {
                console.error('❌ Ошибка удаления из избранного:', data.message);
                this.showNotification('error', data.message || 'Ошибка удаления из избранного');
            }

        } catch (error) {
            console.error('💥 Исключение при удалении из избранного:', error);
            this.showNotification('error', 'Произошла ошибка сети');
        } finally {
            // Включаем кнопку обратно
            if (buttonElement) {
                this.setButtonLoading(buttonElement, false);
            }
        }
    }

    /**
     * Обновление кнопки в состояние "избранное"
     */
    updateButtonToFavorite(buttonElement, favoriteData = null) {
        if (buttonElement.classList.contains('favorite-heart-btn')) {
            // Иконка сердечка
            buttonElement.classList.add('is-favorite');
            buttonElement.innerHTML = '<i class="fas fa-heart"></i>';
            buttonElement.style.color = '#dc3545';
            if (favoriteData && favoriteData.id) {
                buttonElement.dataset.favoriteId = favoriteData.id;
            }
        } else {
            // Обычная кнопка
            buttonElement.innerHTML = '<i class="fas fa-heart"></i> В избранном';
            buttonElement.classList.remove('btn-outline');
            buttonElement.classList.add('btn-danger');
            buttonElement.classList.add('remove-from-favorites-btn');
            buttonElement.classList.remove('add-to-favorites-btn');
            if (favoriteData && favoriteData.id) {
                buttonElement.dataset.favoriteId = favoriteData.id;
            }
        }
    }

    /**
     * Обновление кнопки в обычное состояние
     */
    updateButtonToNormal(buttonElement) {
        if (buttonElement.classList.contains('favorite-heart-btn')) {
            // Иконка сердечка
            buttonElement.classList.remove('is-favorite');
            buttonElement.innerHTML = '<i class="far fa-heart"></i>';
            buttonElement.style.color = '';
            delete buttonElement.dataset.favoriteId;
        } else {
            // Обычная кнопка
            buttonElement.innerHTML = '<i class="far fa-heart"></i> В избранное';
            buttonElement.classList.add('btn-outline');
            buttonElement.classList.remove('btn-danger');
            buttonElement.classList.add('add-to-favorites-btn');
            buttonElement.classList.remove('remove-from-favorites-btn');
            delete buttonElement.dataset.favoriteId;
        }
    }

    /**
     * Установка состояния загрузки для кнопки
     */
    setButtonLoading(buttonElement, isLoading) {
        if (isLoading) {
            buttonElement.disabled = true;
            buttonElement.style.opacity = '0.6';
            const originalContent = buttonElement.innerHTML;
            buttonElement.dataset.originalContent = originalContent;
            buttonElement.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Загрузка...';
        } else {
            buttonElement.disabled = false;
            buttonElement.style.opacity = '1';
            if (buttonElement.dataset.originalContent) {
                buttonElement.innerHTML = buttonElement.dataset.originalContent;
                delete buttonElement.dataset.originalContent;
            }
        }
    }

    /**
     * Создание контейнера для уведомлений
     */
    createNotificationContainer() {
        if (document.getElementById('notification-container')) return;

        const container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 100px;
            right: 20px;
            z-index: 10000;
            max-width: 400px;
        `;
        document.body.appendChild(container);
    }

    /**
     * Показ уведомления
     */
    showNotification(type, message, duration = 4000) {
        const container = document.getElementById('notification-container');
        if (!container) return;

        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;

        const colors = {
            success: { bg: '#d4edda', border: '#c3e6cb', text: '#155724', icon: 'fas fa-check-circle' },
            error: { bg: '#f8d7da', border: '#f5c6cb', text: '#721c24', icon: 'fas fa-exclamation-circle' },
            warning: { bg: '#fff3cd', border: '#ffeaa7', text: '#856404', icon: 'fas fa-exclamation-triangle' }
        };

        const color = colors[type] || colors.success;

        notification.style.cssText = `
            background: ${color.bg};
            border: 1px solid ${color.border};
            color: ${color.text};
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex;
            align-items: center;
            gap: 10px;
            animation: slideInRight 0.3s ease;
            position: relative;
            cursor: pointer;
        `;

        notification.innerHTML = `
            <i class="${color.icon}"></i>
            <span>${message}</span>
            <button style="
                position: absolute;
                right: 10px;
                top: 50%;
                transform: translateY(-50%);
                background: none;
                border: none;
                color: ${color.text};
                cursor: pointer;
                font-size: 18px;
                opacity: 0.7;
            ">&times;</button>
        `;

        // Добавляем стили анимации
        if (!document.getElementById('notification-styles')) {
            const styles = document.createElement('style');
            styles.id = 'notification-styles';
            styles.textContent = `
                @keyframes slideInRight {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes slideOutRight {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
            `;
            document.head.appendChild(styles);
        }

        container.appendChild(notification);

        // Автоудаление
        const autoRemove = setTimeout(() => {
            this.removeNotification(notification);
        }, duration);

        // Удаление по клику
        notification.addEventListener('click', () => {
            clearTimeout(autoRemove);
            this.removeNotification(notification);
        });
    }

    /**
     * Удаление уведомления
     */
    removeNotification(notification) {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }

    /**
     * Показ диалога подтверждения
     */
    async showConfirmDialog(message) {
        return new Promise((resolve) => {
            // Создаем красивый модальный диалог
            const modal = document.createElement('div');
            modal.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0,0,0,0.5);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 10001;
                animation: fadeIn 0.3s ease;
            `;

            modal.innerHTML = `
                <div style="
                    background: white;
                    padding: 30px;
                    border-radius: 12px;
                    box-shadow: 0 8px 25px rgba(0,0,0,0.3);
                    max-width: 400px;
                    text-align: center;
                    animation: scaleIn 0.3s ease;
                ">
                    <div style="font-size: 48px; color: #ff6b6b; margin-bottom: 20px;">
                        <i class="fas fa-heart-broken"></i>
                    </div>
                    <h3 style="margin-bottom: 15px; color: #333;">${message}</h3>
                    <p style="color: #666; margin-bottom: 25px;">Это действие нельзя отменить</p>
                    <div style="display: flex; gap: 10px; justify-content: center;">
                        <button id="confirm-cancel" style="
                            padding: 10px 20px;
                            border: 2px solid #ddd;
                            background: white;
                            color: #666;
                            border-radius: 6px;
                            cursor: pointer;
                            font-weight: 500;
                        ">Отмена</button>
                        <button id="confirm-ok" style="
                            padding: 10px 20px;
                            border: none;
                            background: #dc3545;
                            color: white;
                            border-radius: 6px;
                            cursor: pointer;
                            font-weight: 500;
                        ">Удалить</button>
                    </div>
                </div>
            `;

            // Добавляем анимации
            if (!document.getElementById('modal-styles')) {
                const styles = document.createElement('style');
                styles.id = 'modal-styles';
                styles.textContent = `
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    @keyframes scaleIn {
                        from { transform: scale(0.7); opacity: 0; }
                        to { transform: scale(1); opacity: 1; }
                    }
                `;
                document.head.appendChild(styles);
            }

            document.body.appendChild(modal);

            // Обработчики кнопок
            modal.querySelector('#confirm-ok').addEventListener('click', () => {
                document.body.removeChild(modal);
                resolve(true);
            });

            modal.querySelector('#confirm-cancel').addEventListener('click', () => {
                document.body.removeChild(modal);
                resolve(false);
            });

            // Закрытие по клику вне диалога
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    document.body.removeChild(modal);
                    resolve(false);
                }
            });
        });
    }

    /**
     * Обновление счетчика избранного в навигации
     */
    updateFavoriteCounter(delta) {
        const counter = document.querySelector('.favorite-count');
        if (counter) {
            const current = parseInt(counter.textContent) || 0;
            const newCount = Math.max(0, current + delta);
            counter.textContent = newCount;

            // Анимация изменения
            counter.style.transform = 'scale(1.2)';
            setTimeout(() => {
                counter.style.transform = 'scale(1)';
            }, 200);
        }
    }

    /**
     * Удаление карточки магазина со страницы избранного
     */
    removeStoreCard(favoriteId) {
        const card = document.querySelector(`[data-favorite-id="${favoriteId}"]`);
        if (card) {
            const storeCard = card.closest('.store-card');
            if (storeCard) {
                storeCard.style.animation = 'fadeOut 0.5s ease';
                setTimeout(() => {
                    storeCard.remove();

                    // Проверяем, остались ли карточки
                    const remainingCards = document.querySelectorAll('.store-card');
                    if (remainingCards.length === 0) {
                        // Показываем пустое состояние
                        location.reload();
                    }
                }, 500);
            }
        }
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    window.favoriteManager = new FavoriteStoreManager();
    console.log('✅ FavoriteStoreManager инициализирован');
});

// Глобальные функции для обратной совместимости
window.addToFavorites = function(storeId, buttonElement) {
    if (window.favoriteManager) {
        window.favoriteManager.addToFavorites(storeId, buttonElement);
    }
};

window.removeFromFavorites = function(favoriteId, buttonElement) {
    if (window.favoriteManager) {
        window.favoriteManager.removeFromFavorites(favoriteId, buttonElement);
    }
};