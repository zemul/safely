/**
 * 认证工具 -- 管理 JWT token
 * 自动拦截 jQuery AJAX 请求附加 Authorization header
 */
(function() {
    var TOKEN_KEY = 'safely_token';

    window.Auth = {
        getToken: function() { return localStorage.getItem(TOKEN_KEY); },
        setToken: function(t) { localStorage.setItem(TOKEN_KEY, t); },
        clear: function() { localStorage.removeItem(TOKEN_KEY); },
        isLoggedIn: function() { return !!this.getToken(); },
        redirectLogin: function() {
            if (location.pathname !== '/login-page.html') {
                location.href = '/login-page.html';
            }
        }
    };

    // 页面非登录页时检查是否有 token
    if (!Auth.isLoggedIn() && location.pathname !== '/login-page.html' && location.pathname !== '/register-page.html') {
        Auth.redirectLogin();
    }

    // 等 jQuery 加载后绑定全局 AJAX hooks
    function bindJQuery() {
        if (typeof $ === 'undefined' && typeof jQuery === 'undefined') {
            setTimeout(bindJQuery, 50);
            return;
        }
        var jq = $ || jQuery;
        jq(document).ajaxSend(function(e, xhr) {
            var token = Auth.getToken();
            if (token) xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        });
        jq(document).ajaxError(function(e, xhr) {
            if (xhr.status === 401) { Auth.clear(); Auth.redirectLogin(); }
        });
    }
    bindJQuery();
})();
