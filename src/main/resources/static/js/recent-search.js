const STORAGE_KEY = "recentCities";
const MAX_CITIES = 5;

document.addEventListener("DOMContentLoaded", () => {

    saveCurrentCity();

    loadRecentCities();

});

function saveCurrentCity() {

    const cityElement = document.querySelector(".hero-city");

    if (!cityElement)
        return;

    const city = cityElement.textContent.trim();

    if (
        city === "" ||
        city === "Search a city"
    )
        return;

    let cities = JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];

    cities = cities.filter(c => c.toLowerCase() !== city.toLowerCase());

    cities.unshift(city);

    if (cities.length > MAX_CITIES)
        cities = cities.slice(0, MAX_CITIES);

    localStorage.setItem(STORAGE_KEY, JSON.stringify(cities));

}

function loadRecentCities() {

    const container = document.getElementById("recentSearchList");

    if (!container)
        return;

    container.innerHTML = "";

    const cities = JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];

    if (cities.length === 0) {

        container.innerHTML =
            `<div class="no-search">
                No recent searches
            </div>`;

        return;
    }

    cities.forEach(city => {

        const item = document.createElement("div");

        item.className = "recent-city";

        item.innerHTML = `
            <div class="recent-left">
                <i class="bi bi-geo-alt-fill"></i>
                <span>${city}</span>
            </div>

            <i class="bi bi-chevron-right"></i>
        `;

        item.onclick = () => {

            window.location.href =
                "/weather?city=" + encodeURIComponent(city);

        };

        container.appendChild(item);

    });

}