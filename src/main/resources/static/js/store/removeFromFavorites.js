// ✅ УПРОЩЕННАЯ функция удаления из избранного
function removeFromFavorites(storeId) {
    console.log(`➖ Removing store ${storeId} from favorites`);

    if (!isAuthenticated || !jwtToken) {
        showNotification('Необходимо войти в систему', 'error');
        return;
    }

    if (!confirm('Удалить магазин из избранного?')) {
        return;
    }

    // Находим кнопку и показываем состояние загрузки
    const button = document.querySelector(`[data-store-id="${storeId}"]`);
    if (button) {
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    }

    fetch(`/favorites/stores/remove/${storeId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => {
            console.log(`📡 Response status: ${response.status}`);
            if (!response.ok) {
                if (response.status === 401) {
                    showNotification('Сессия истекла, войдите заново', 'error');
                    setTimeout(() => window.location.href = '/login', 2000);
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('📦 Response data:', data);

            if (data.success) {
                showNotification(data.message || 'Магазин удален из избранного', 'success');

                // ✅ ПЛАВНОЕ удаление карточки
                const storeCard = button.closest('.store-card');
                if (storeCard) {
                    storeCard.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                    storeCard.style.opacity = '0';
                    storeCard.style.transform = 'scale(0.8)';

                    setTimeout(() => {
                        storeCard.remove();

                        // Проверяем, остались ли еще магазины
                        const remainingCards = document.querySelectorAll('.store-card');
                        if (remainingCards.length === 0) {
                            // Перезагружаем страницу для показа пустого состояния
                            setTimeout(() => location.reload(), 500);
                        }
                    }, 500);
                }
            } else {
                showNotification(data.message || 'Ошибка удаления из избранного', 'error');
                // Восстанавливаем кнопку при ошибке
                if (button) {
                    button.disabled = false;
                    button.innerHTML = '<i class="fas fa-heart-broken"></i>';
                }
            }
        })
        .catch(error => {
            console.error('💥 Error removing from favorites:', error);
            showNotification('Произошла ошибка при удалении', 'error');

            // Восстанавливаем кнопку при ошибке
            if (button) {
                button.disabled = false;
                button.innerHTML = '<i class="fas fa-heart-broken"></i>';
            }
        });
}
