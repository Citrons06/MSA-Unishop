function handleResponse(response) {
    if (response.status === 401) {
        // 서버가 유효하지 않은 토큰이라고 응답했다면 로컬 스토리지에서 토큰 삭제
        localStorage.removeItem('token');
        // 사용자를 로그인 페이지로 리다이렉트
        window.location.href = '/login-page';
    }
}