document.addEventListener('DOMContentLoaded', function () {
    function fetchWithAuth(url, options = {}) {
        const headers = new Headers(options.headers || {});
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
            headers.append('Authorization', 'Bearer ' + accessToken);
        }
        options.headers = headers;

        return fetch(url, options).then(response => {
            if (response.status === 401) {
                return refreshToken().then(() => {
                    const newAccessToken = localStorage.getItem('accessToken');
                    headers.set('Authorization', 'Bearer ' + newAccessToken);
                    return fetch(url, options);
                });
            }
            return response;
        });
    }

    function refreshToken() {
        const refreshToken = localStorage.getItem('refreshToken');
        return fetch('/api/refresh', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ refreshToken: refreshToken })
        })
            .then(response => response.json())
            .then(data => {
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);
            })
            .catch(error => {
                console.error('Error refreshing token:', error);
                logout();
            });
    }

    function logout() {
        const refreshToken = localStorage.getItem('refreshToken');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');

        fetch('/api/user/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ refreshToken: refreshToken })
        }).then(() => {
            window.location.href = '/';
        }).catch(error => {
            console.error('Logout failed:', error);
        });
    }

    window.fetchWithAuth = fetchWithAuth;
    window.logout = logout;

    if (document.getElementById('user-info')) {
        fetchWithAuth('/api/user/info')
            .then(response => response.json())
            .then(data => {
                const userInfoDiv = document.getElementById('user-info');
                userInfoDiv.innerHTML = `<p>사용자: ${data.username}</p>`;
            })
            .catch(error => {
                console.error('Error fetching user info:', error);
                document.getElementById('user-info').innerHTML = `<p>사용자 정보를 가져오지 못했습니다.</p>`;
            });
    }
});