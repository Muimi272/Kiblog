(function () {
    var STORAGE_KEY = "kiblog_theme";
    var root = document.documentElement;

    function getSystemTheme() {
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches
            ? "dark"
            : "light";
    }

    function getStoredTheme() {
        var value = localStorage.getItem(STORAGE_KEY);
        return value === "dark" || value === "light" ? value : null;
    }

    function getActiveTheme() {
        return getStoredTheme() || root.getAttribute("data-theme") || getSystemTheme();
    }

    function applyTheme(theme) {
        root.setAttribute("data-theme", theme);
        syncToggleButtons(theme);
    }

    function syncToggleButtons(theme) {
        var nextThemeLabel = theme === "dark" ? "切换到浅色" : "切换到深色";
        document.querySelectorAll("[data-theme-toggle]").forEach(function (button) {
            button.setAttribute("aria-label", nextThemeLabel);
            button.setAttribute("title", nextThemeLabel);

            var label = button.querySelector("[data-theme-label]");
            if (label) {
                label.textContent = theme === "dark" ? "浅色" : "深色";
            }
        });
    }

    function toggleTheme() {
        var nextTheme = getActiveTheme() === "dark" ? "light" : "dark";
        localStorage.setItem(STORAGE_KEY, nextTheme);
        applyTheme(nextTheme);
    }

    document.addEventListener("DOMContentLoaded", function () {
        applyTheme(getActiveTheme());

        document.querySelectorAll("[data-theme-toggle]").forEach(function (button) {
            button.addEventListener("click", toggleTheme);
        });

        if (window.matchMedia) {
            window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", function (event) {
                if (!getStoredTheme()) {
                    applyTheme(event.matches ? "dark" : "light");
                }
            });
        }
    });

    window.KiblogTheme = {
        applyTheme: applyTheme,
        toggleTheme: toggleTheme
    };
})();
